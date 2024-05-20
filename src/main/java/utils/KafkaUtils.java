package utils;

import constants.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KafkaUtils {
    private static Properties getConsumerProperties() {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Credentials.getInstance().bootstrapServer());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "client_auto");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group_auto");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    public static String getMessageFromTopic(String topic, String containsText) {
        String result;
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(getConsumerProperties());
        try {
            consumer.subscribe(Collections.singletonList(topic));
            result = waitForRecord(consumer, containsText, topic).value();
        } finally {
            consumer.unsubscribe();
            consumer.close();
        }
        return result;
    }

    private static ConsumerRecord<String, String> waitForRecord(KafkaConsumer<String, String> consumer, String containsText, String topic) {
        AtomicReference<ConsumerRecord<String, String>> atomicRecord = new AtomicReference<>();
        WaitingUtils.waitUntil(5 * 60, 10, 5,
                String.format("Ожидание появления сообщения, которое содержит текст \"%s\" в топике \"%s\"", topic, containsText), () -> {
//                    consumer.seekToBeginning(consumer.assignment());
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                    log.info("===Получение сообщений из топика===");
                    for (ConsumerRecord<String, String> record : records) {
                        log.info(record.value());
                        if (record.value().contains(containsText)) {
                            log.info("===Сообщение найдено===");
                            atomicRecord.set(record);
                            return true;
                        }
                    }
                    log.info("===Сообщение не найдено в топике===");
                    return false;
                });
        return Optional.of(atomicRecord.get()).orElseThrow(NullPointerException::new);
    }
}
