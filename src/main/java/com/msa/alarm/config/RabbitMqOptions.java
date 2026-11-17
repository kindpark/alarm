package com.msa.alarm.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RabbitMqOptions {
    public final String section = "RabbitMq";

    private String host = "localhost";
    private int port = 5672;
    private String user = "guest";
    private String password = "guest";

    /** API_SPEC §9.1 */
    private String exchange = "sns.events";
    private String queue = "notification.events";

    /** 처리하지 못한 메시지가 가는 곳 (dead-letter) */
    private String deadLetterExchange = "sns.events.dlx";
    private String deadLetterQueue = "notification.events.dlq";
}
