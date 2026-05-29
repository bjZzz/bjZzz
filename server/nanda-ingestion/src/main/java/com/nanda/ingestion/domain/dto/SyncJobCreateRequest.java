package com.nanda.ingestion.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SyncJobCreateRequest {

    @NotNull
    private Long sourceId;

    @NotBlank
    private String scheduleType;

    private String cronExpr;
}
