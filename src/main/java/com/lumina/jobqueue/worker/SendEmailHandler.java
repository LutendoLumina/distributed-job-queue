package com.lumina.jobqueue.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SendEmailHandler implements JobHandler {

    private static final Logger log = LoggerFactory.getLogger(SendEmailHandler.class);
    private final ObjectMapper objectMapper;

    public SendEmailHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String type() {
        return "SEND_EMAIL";
    }

    @Override
    public void handle(String payload) throws Exception {
        JsonNode data = objectMapper.readTree(payload);
        Thread.sleep(500); // pretend sending takes time

        if (data.path("fail").asBoolean(false)) {
            throw new RuntimeException("Simulated email server error");
        }
        log.info("Sent email to {}", data.path("to").asString());
    }
}