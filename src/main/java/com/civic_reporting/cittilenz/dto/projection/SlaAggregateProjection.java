package com.civic_reporting.cittilenz.dto.projection;

public interface SlaAggregateProjection {

    Long getTotal();
    Long getAssigned();
    Long getInProgress();
    Long getResolved();
    Long getEscalated();

    Long getSoftBreached();
    Long getHardBreached();
    Long getSupervisorRequired();

    Double getAvgAckMinutes();
    Double getAvgResolutionMinutes();

    Long getEscalatedOnce();
    Long getReassignedOnce();
}