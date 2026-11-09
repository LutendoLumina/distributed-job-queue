package com.lumina.jobqueue.worker;

public interface JobHandler {
    String type();                              // which job type I handle, e.g. "SEND_EMAIL"
    void handle(String payload) throws Exception;
}