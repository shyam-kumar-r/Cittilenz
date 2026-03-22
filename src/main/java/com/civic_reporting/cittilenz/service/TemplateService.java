package com.civic_reporting.cittilenz.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class TemplateService {

    private final TemplateEngine templateEngine;

    public TemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(String templateName, Map<String, Object> data) {

        Context context = new Context();
        context.setVariables(data);

        return templateEngine.process(templateName, context);
    }
}