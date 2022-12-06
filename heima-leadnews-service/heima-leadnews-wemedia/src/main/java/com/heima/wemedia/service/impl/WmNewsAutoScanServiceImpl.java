package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.tencent.cloud.util.TencentContentSecurity;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自媒体文章审核
 */
@Slf4j
@Service
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Resource
    private WmNewsMapper wmNewsMapper;
    @Resource
    private TencentContentSecurity tencentContentSecurity;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private IArticleClient articleClient;
    @Resource
    private WmChannelMapper wmChannelMapper;
    @Resource
    private WmUserMapper wmUserMapper;
    @Resource
    private WmSensitiveMapper wmSensitiveMapper;
    @Resource
    private Tess4jClient tess4jClient;

    /**
     * 自媒体文章审核
     * @param id 自媒体文章ID
     */
    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        log.info("自媒体文章审核 : {}", id);
        // 查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null){
            throw new RuntimeException("文章不存在");
        }
        // 状态为待审核
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            // 从内容中提取纯文本和图片
            Map<String, Object> textAndImages = handleTextAndImage(wmNews);

            //自管理的敏感词过滤
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            log.info("自管理的敏感词过滤 result : {}", isSensitive);
            if(!isSensitive) return;

            // 审核文本内容
            boolean isTextScan = handleTextScan(textAndImages.get("content") + wmNews.getTitle(), wmNews);
            // 审核失败
            if (!isTextScan){
                return;
            }
            // 审核图片
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isImageScan){
                return;
            }
            // 审核成功 保存app端相关文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            log.info("审核成功 保存app端相关文章数据 responseResult : {}", responseResult.getData());
            if (!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl 文章内容审核, 保存APP端文章相关数据失败!");
            }
            // 回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, (short) 9, "审核成功");
        }

    }

    /**
     * 自管理的敏感词审核
     * @param content 文章文本内容
     * @param wmNews 文章实体
     * @return 审核结果
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag = true;

        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size() >0){
            updateWmNews(wmNews,(short) 2,"当前文章中存在违规内容"+map);
            flag = false;
        }

        return flag;
    }

    /**
     * 保存APP端相关文章数据
     * @param wmNews 文章实体
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, articleDto);
        articleDto.setLayout(wmNews.getType());
        // 渠道名称设置
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null){
            articleDto.setChannelName(wmChannel.getName());
        }
        // 作者信息设置
        articleDto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmNews != null){
            articleDto.setAuthorName(wmUser.getName());
        }
        // 文章ID设置
        if (wmNews.getArticleId() != null){
            articleDto.setId(wmNews.getArticleId());
        }

        articleDto.setCreatedTime(new Date());
        ResponseResult responseResult = articleClient.saveArticle(articleDto);

        return responseResult;
    }

    /**
     * 审核图片
     * @param images 图片URL集合
     * @param wmNews 文章实体
     * @return 审核结果
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        if (images == null || images.size() == 0){
            return true;
        }

        // 存储图片审核结果
        List<Map<String, String>> mapList = new ArrayList<>();
        // 下载图片
        images = images.stream().distinct().collect(Collectors.toList());
        for (String url : images) {
            byte[] bytes = fileStorageService.downLoadFile(url);

            try{
                // 识别图片的文字
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage read = ImageIO.read(in);
                String result = tess4jClient.doOCR(read);
                // 图片识别文字审核
                boolean b = handleSensitiveScan(result, wmNews);
                if (!b){
                    return false;
                }

                // 图片字节数组加密
                String fileContent = Base64.getEncoder().encodeToString(bytes);
                Map<String, String> resultMap = tencentContentSecurity.imageModeration(fileContent);
                mapList.add(resultMap);
            } catch (Exception exception){
                exception.printStackTrace();
            }



        }

        // 循环判断每张图片审核结果
        for (Map map : mapList) {
            if (map != null){
                if (map.get("suggestion").equals("Block")){
                    // 审核失败
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "文章图片存在违规");
                }
                if (map.get("suggestion").equals("Review")){
                    // 建议人工复核
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "文章图片存在不确定项");
                }
                if (map.get("suggestion").equals("Pass")){
                    // 审核通过
                    wmNews.setStatus((short) 2);
                }

            }
        }
        return flag;
    }

    /**
     * 审核纯文本内容
     * @param content 文本内容
     * @param wmNews 文章实体
     * @return 审核结果
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;
        if (StringUtils.isBlank(content)){
            return true;
        }
        Map<String, String> resultMap = tencentContentSecurity.textModeration(content);
        if (resultMap != null){
            if (resultMap.get("suggestion").equals("Block")){
                // 审核失败
                flag = false;
                updateWmNews(wmNews, (short) 2, "文章内容存在违规");
            }
            if (resultMap.get("suggestion").equals("Review")){
                // 建议人工复核
                flag = false;
                updateWmNews(wmNews, (short) 3, "文章内容存在不确定项");
            }
            if (resultMap.get("suggestion").equals("Pass")){
                // 审核通过
                wmNews.setStatus((short) 2);
            }

        }
        return flag;
    }

    /**
     * 修改文章内容
     * @param wmNews 文章实体
     * @param status 文章状态
     * @param reason 审核失败原因
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 从文章内容中提取文本和图片
     * 提取文章封面图片
     * @param wmNews 文章实体
     * @return
     */
    private  Map<String, Object> handleTextAndImage(WmNews wmNews) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();
        // 分别获取文章文本内容和图片
        if (StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map: maps) {
                if (map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }
        // 获取文章封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("images", images);

        return resultMap;
    }


}
