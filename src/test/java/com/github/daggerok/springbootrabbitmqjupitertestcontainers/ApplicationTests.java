package com.github.daggerok.springbootrabbitmqjupitertestcontainers;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.Resource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@Testcontainers
@DisplayName("Integration tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ApplicationTests {

  @Container
  static RabbitMQContainer rabbitMQContainer =
      new RabbitMQContainer("rabbitmq:3.8.16-management-alpine")
          .withExposedPorts(5672/*, 15672*/);

  @DynamicPropertySource
  static void registerActualRabbitMqPortGivenByTestContainer(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
  }

  @Resource
  MyAppProps props;

  @Resource
  RabbitTemplate rabbitTemplate;

  @Test
  void should_send_and_receive_converting_object() {
    // given
    var payload = Map.of("Hello", "World");
    // when
    rabbitTemplate.convertAndSend(props.getTopicExchangeName(), props.getRoutingKey(), payload);
    log.info("sent payload: {}", payload);
    // then
    var res = rabbitTemplate.receiveAndConvert(
        props.getQueueName(), 3456, new ParameterizedTypeReference<Map<String, String>>() {}
    );
    // and
    log.info("received message: {}", res);
    assertThat(res).isEqualTo(payload);
  }
}
