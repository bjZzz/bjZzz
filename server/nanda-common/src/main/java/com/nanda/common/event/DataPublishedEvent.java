package com.nanda.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DataPublishedEvent extends ApplicationEvent {

    private final Long empiId;
    private final String specialtyType;
    private final Long recordId;
    private final Long orgId;

    public DataPublishedEvent(Object source, Long empiId, String specialtyType, Long recordId, Long orgId) {
        super(source);
        this.empiId = empiId;
        this.specialtyType = specialtyType;
        this.recordId = recordId;
        this.orgId = orgId;
    }
}
