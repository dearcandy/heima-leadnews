package com.heima.kafka.simple;

import org.apache.kafka.clients.consumer.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 消费者
 */
public class ConsumerQuickStart {

    public static void main(String[] args) {
        // kafka 连接配置信息
        Properties props = new Properties();

        // 设置连接地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "121.4.65.89:9092");
        // key和value的反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        // 消费者组
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group2");

        // 创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 订阅主题
        consumer.subscribe(Collections.singletonList("itcast-topic-out"));

        while (true){
            // 拉取消息
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
            consumerRecords.forEach(item->{
                System.out.println(item.key());
                System.out.println(item.value());
            });
        }

    }

}