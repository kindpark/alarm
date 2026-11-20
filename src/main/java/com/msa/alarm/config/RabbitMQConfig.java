package com.msa.alarm.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
public class RabbitMQConfig {
    // 이름 상수
    public static final String QUEUE = "notification.events";
    // DLX
    public static final String DLX = "sns.events.dlx";
    // DLQ
    public static final String DLQ = "notification.events.dlq";
    // 이 서비스가 받을 이벤트. NotificationService의 처리목록도 이걸 쓰게하면 한곳만 고치면 된다.
    public static final List<String> ROUTING_KEYS =
            List.of("follow.created",
                    "reply.created",
                    "like.created",
                    "repost.created",
                    "quote.created",
                    "user.deleted");

    public static final String EXCHANGE_NAME = "sns.events";


    // Excharge 빈. C# 부분과 완벽히 같아야함
    @Bean
    public TopicExchange snsEventExcharge(){
        // C#쪽 초안보고 짜는중인데 이것과 동일해야함
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // JSON 메시지 변환. 이게 없으면 Rabbit 템플릿이 자바 직렬화로 객체를 보내기에 필수
    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper){
        return new JacksonJsonMessageConverter(jsonMapper);
    }
    // 큐 알림 바인딩
    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DLX).build();
    }
    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX);
    }
    //
    @Bean
    public Queue deadLetterQueue(){
        return QueueBuilder.durable(DLQ).build();
    }
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }
    //
    @Bean
    public Declarables notificationBinding(Queue notificationQueue,
                                          TopicExchange snsEventExcharge){
        // 라우팅 키마다 sns.event -> notification.events 바인딩 하나씩 만든다.
        return new Declarables(
                ROUTING_KEYS.stream()
                        .map(key -> BindingBuilder
                                .bind(notificationQueue).to(snsEventExcharge)
                                .with(key))
                        .toList());
    }
}
