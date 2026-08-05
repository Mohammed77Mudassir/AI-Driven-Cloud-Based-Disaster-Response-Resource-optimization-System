package com.disaster.service;

public interface SMSProvider {
    boolean sendSMS(String to, String message);
}
