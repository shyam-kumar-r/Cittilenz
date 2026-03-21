package com.civic_reporting.cittilenz.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

}