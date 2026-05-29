package com.nanda.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StagingBatchReceivedEvent extends ApplicationEvent {

    private final Long batchId;
    private final Long orgId;
    private final int recordCount;

    public StagingBatchReceivedEvent(Object source, Long batchId, Long orgId, int recordCount) {
        super(source);
        this.batchId = batchId;
        this.orgId = orgId;
        this.recordCount = recordCount;
    }
}
