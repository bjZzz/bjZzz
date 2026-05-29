package com.nanda.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.List;

@Getter
public class ComorbidityViewRefreshEvent extends ApplicationEvent {

    private final Long empiId;
    private final List<Long> ruleIds;
    private final Long orgId;

    public ComorbidityViewRefreshEvent(Object source, Long empiId, List<Long> ruleIds, Long orgId) {
        super(source);
        this.empiId = empiId;
        this.ruleIds = ruleIds != null ? ruleIds : Collections.<Long>emptyList();
        this.orgId = orgId;
    }
}
