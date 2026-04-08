package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.entity.IssueType;

public class IssueTypeMapper {

    public static IssueTypeResponse toResponse(IssueType it) {

        IssueTypeResponse r = new IssueTypeResponse();

        // 🔹 Basic fields
        r.setId(it.getId());
        r.setName(it.getName());

        // 🔥 AI alignment fields
        r.setNormalizedName(
                it.getName() != null ? it.getName().toLowerCase() : null
        );

        r.setDisplayName(formatDisplayName(it.getName()));

        // 🔹 Department
        if (it.getDepartment() != null) {
            r.setDepartmentId(it.getDepartment().getId());
            r.setDepartmentName(it.getDepartment().getName());
        }

        // 🔹 Additional fields
        r.setSlaHours(it.getSlaHours());
        r.setPriority(it.getPriority() != null ? it.getPriority().name() : null);
        r.setActive(it.isActive());
        r.setDescription(it.getDescription());

        return r;
    }

    // 🔥 Clean UI formatting
    private static String formatDisplayName(String name) {
        if (name == null) return null;

        String formatted = name.replace("_", " ").toLowerCase();

        String[] words = formatted.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }

        return result.toString().trim();
    }
}