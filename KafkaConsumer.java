package es.custom.kafka.consumers;

import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.custom.elasticsearch.repository.data.DeviceDataRepository;
import es.custom.elasticsearch.repository.system.AlertRepository;
import es.custom.model.Alert;
import es.custom.model.data.DeviceData;
import es.custom.service.analyzer.DeviceDataAnalyzerService;


@Service
public class KafkaConsumer {

	private Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
	
	@Autowired
	private SimpMessagingTemplate streaming;
	
	@Autowired
	private DeviceDataRepository dataRepository;
	
	@Autowired
	private AlertRepository alertRepository;
	
	@Autowired
	private DeviceDataAnalyzerService analyzer;
	
	@Value("${streaming.topic.live}")
	private String pathLive;
	
	@Value("${streaming.topic.test}")
	private String pathTest;
	
	@Value("${streaming.topic.alert}")
	private String pathAlert;
	
	private ObjectMapper mapper;
	
	@PostConstruct
	public void init() {
		this.mapper = new ObjectMapper();
	}
	
	@KafkaListener(topics="${kafka.topic.live}")
	public void consumeLive(@Payload JsonNode message) {
		logger.info("[Kafka]["+ pathLive +"] " + message.toString());
		CompletableFuture.runAsync(() -> streaming.convertAndSend(pathLive, message.get("data")));
		CompletableFuture.runAsync(() -> dataRepository.save(mapper.convertValue(message, DeviceData.class)));
		CompletableFuture.runAsync(() -> analyzer.analyze(mapper.convertValue(message, DeviceData.class)));
	}
	
	@KafkaListener(topics="${kafka.topic.test}")
	public void consumeTest(@Payload JsonNode message) {
		logger.info("[Kafka]["+ pathTest +"] " + message.toString());
		CompletableFuture.runAsync(() -> streaming.convertAndSend(pathTest, message.get("data")));
	}
	
	@KafkaListener(topics="${kafka.topic.alert}")
	public void consumeAlert(@Payload JsonNode message) {
		logger.info("[Kafka]["+ pathAlert +"] " + message.toString());
		CompletableFuture.runAsync(() -> streaming.convertAndSend(pathAlert, message));
		CompletableFuture.runAsync(() -> alertRepository.save(mapper.convertValue(message, Alert.class)));
	}
	
}
