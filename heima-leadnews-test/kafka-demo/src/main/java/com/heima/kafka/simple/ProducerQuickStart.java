package com.heima.kafka.simple;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 生产者
 */
public class ProducerQuickStart {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // kafka 连接配置信息
        Properties props = new Properties();

        // 设置连接地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "121.4.65.89:9092");
        // key和value的序列化器
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "1z4");

        // 创建kafka生产者
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // 发送消息
        for (int i = 0; i < 5; i++){
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("itcast-topic-input", "hello kafka");
            producer.send(producerRecord);
        }

        // 关闭消息通道 必须关闭
        producer.close();
    }

}