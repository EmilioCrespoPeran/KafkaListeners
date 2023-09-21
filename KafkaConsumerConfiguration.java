package es.custom.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.kafka.common.serialization.StringDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfiguration {

	@Value("${kafka.servers}")
	private String servers;
	
	@Value("${kafka.consumer-group}")
	private String consumerGroup;
	
	
	@Bean
	public ConsumerFactory<String, JsonNode> consumerFactory(){
		JsonDeserializer<JsonNode> deserializer = new JsonDeserializer<>(JsonNode.class);
	    deserializer.setRemoveTypeHeaders(false);
	    deserializer.addTrustedPackages("*");
	    deserializer.setUseTypeMapperForKey(true);

	    Map<String,Object> config = new HashMap<String,Object>();
	    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
	    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
	    config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
	    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Get latest message
	    
		return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
	}
	
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, JsonNode>> kafkaListenerContainerFactory(){
		ConcurrentKafkaListenerContainerFactory<String, JsonNode> factory = new ConcurrentKafkaListenerContainerFactory();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}
}
