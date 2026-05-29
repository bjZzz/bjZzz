package com.nanda.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CleaningCompletedEvent extends ApplicationEvent {

    private final Long batchId;
    private final Long orgId;
    private final int readyCount;

    public CleaningCompletedEvent(Object source, Long batchId, Long orgId, int readyCount) {
        super(source);
        this.batchId = batchId;
        this.orgId = orgId;
        this.readyCount = readyCount;
    }
}
