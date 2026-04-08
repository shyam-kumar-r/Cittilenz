package com.civic_reporting.cittilenz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiMappingService {

    private static final Logger log = LoggerFactory.getLogger(AiMappingService.class);

    private static final Map<String, Integer> AI_TO_DB_MAP = new HashMap<>();

    static {
        // 🚧 Roads
        AI_TO_DB_MAP.put("pothole", 1);
        AI_TO_DB_MAP.put("road_damage", 3);

        // 🗑 Waste
        AI_TO_DB_MAP.put("garbage", 7);

        // 🚦 Traffic
        AI_TO_DB_MAP.put("road_sign", 24);
    }

    public Integer mapToIssueTypeId(String aiLabel) {

        if (aiLabel == null || aiLabel.trim().isEmpty()) {
            return null;
        }

        String key = aiLabel.toLowerCase().trim();

        Integer issueTypeId = AI_TO_DB_MAP.get(key);

        if (issueTypeId == null) {
            log.warn("⚠️ AI label not mapped: {}", aiLabel);
        }

        return issueTypeId;
    }
}