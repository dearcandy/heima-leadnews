package com.heima.kafka.simple;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * 流式处理
 */
public class KafkaStreamQuickStart {

    public static void main(String[] args) {
        // kafka配置信息
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "121.4.65.89:9092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams.quickstart");

        // stream 构建器
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // 流式计算
        streamProcessor(streamsBuilder);

        // 创建kafkaStream对象
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        // 开启流式计算
        kafkaStreams.start();
    }

    /**
     * 流式计算
     * @param streamsBuilder
     */
    private static void streamProcessor(StreamsBuilder streamsBuilder) {
        // 创建Kstream对象 同时指定从哪个topic接收消息
        KStream<String, String> stream = streamsBuilder.stream("itcast-topic-input");
        /**
         * 处理消息的value
         */
        stream.flatMapValues(new ValueMapper<String, Iterable<String>>() {
            @Override
            public Iterable<String> apply(String value) {
                String[] valueArray = value.split(" ");
                return Arrays.asList(valueArray);
            }
        })
                // 按照value进行聚合处理
                .groupBy((key, value) ->value)
                // 时间窗口 十秒聚合一次
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                // 统计单词的个数
                .count()
                // 转换为Stream对象
                .toStream().map((key, value) ->{
                    System.out.println("key : " + key + ", value : " + value);
                    return new KeyValue<>(key.toString(), value.toString());
                })
                // 发送消息
                .to("itcast-topic-out");

    }
}
