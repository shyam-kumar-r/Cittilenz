package com.civic_reporting.cittilenz.service;

public interface NotificationService {

	void notifyUser(Integer userId, String title, String message, String type, Integer issueId);
}