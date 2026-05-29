package com.nanda.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IndexSyncRequiredEvent extends ApplicationEvent {

    private final Long empiId;
    private final String docType;
    private final Long orgId;

    public IndexSyncRequiredEvent(Object source, Long empiId, String docType, Long orgId) {
        super(source);
        this.empiId = empiId;
        this.docType = docType;
        this.orgId = orgId;
    }
}
