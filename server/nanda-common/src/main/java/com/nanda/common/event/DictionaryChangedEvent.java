package com.nanda.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.List;

@Getter
public class DictionaryChangedEvent extends ApplicationEvent {

    private final String dictType;
    private final List<String> fieldCodes;
    private final Long orgId;

    public DictionaryChangedEvent(Object source, String dictType, List<String> fieldCodes, Long orgId) {
        super(source);
        this.dictType = dictType;
        this.fieldCodes = fieldCodes != null ? fieldCodes : Collections.<String>emptyList();
        this.orgId = orgId;
    }
}
