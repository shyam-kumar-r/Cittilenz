package com.civic_reporting.cittilenz.dto.response;

public class AiResponse {

    private String issue;
    private Double confidence;

    // 🔥 NEW FIELDS
    private Integer issueTypeId;
    private Boolean autoSelected;

    public AiResponse() {}

    public AiResponse(String issue, Double confidence) {
        this.issue = issue;
        this.confidence = confidence;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Integer getIssueTypeId() {
        return issueTypeId;
    }

    public void setIssueTypeId(Integer issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public Boolean getAutoSelected() {
        return autoSelected;
    }

    public void setAutoSelected(Boolean autoSelected) {
        this.autoSelected = autoSelected;
    }
}