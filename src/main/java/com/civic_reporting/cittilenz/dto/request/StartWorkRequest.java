package com.civic_reporting.cittilenz.dto.request;

import jakarta.validation.constraints.NotNull;

public class StartWorkRequest {

    @NotNull
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}