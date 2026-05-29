package com.nanda.ingestion.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class DataSourceCreateRequest {

    @NotBlank
    private String sourceCode;

    @NotBlank
    private String sourceName;

    @NotBlank
    private String protocol;

    @NotBlank
    private String configJson;
}
