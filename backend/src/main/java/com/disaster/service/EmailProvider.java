package com.disaster.service;

public interface EmailProvider {
    boolean sendEmail(String to, String subject, String body);
}
