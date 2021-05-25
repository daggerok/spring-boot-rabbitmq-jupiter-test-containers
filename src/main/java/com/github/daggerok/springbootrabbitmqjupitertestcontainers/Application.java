package com.github.daggerok.springbootrabbitmqjupitertestcontainers;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SmartMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Value
@ConstructorBinding
@ConfigurationProperties(prefix = "my-app")
class MyAppProps {
  String topicExchangeName;
  String routingKey;
  String queueName;
  boolean durable;
  boolean exclusive;
  boolean autoDelete;
}

@Configuration
@RequiredArgsConstructor
class RabbitMQConfig {

  private final MyAppProps props;

  @Bean
  Queue queue() {
    return new Queue(props.getQueueName(), props.isDurable(), props.isExclusive(), props.isAutoDelete());
  }

  @Bean
  TopicExchange exchange() {
    return new TopicExchange(props.getTopicExchangeName(), props.isDurable(), props.isAutoDelete());
  }

  @Bean
  Binding binding(Queue queue, TopicExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(props.getRoutingKey());
  }

  @Bean
  public SmartMessageConverter rabbitMqConverter() {
    return new Jackson2JsonMessageConverter();
  }
}

@EnableConfigurationProperties(MyAppProps.class)
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
