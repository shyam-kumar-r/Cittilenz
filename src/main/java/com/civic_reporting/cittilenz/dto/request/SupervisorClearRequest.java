package com.civic_reporting.cittilenz.dto.request;

public class SupervisorClearRequest {
    private Long version;
    private String remarks;
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
    
    
}