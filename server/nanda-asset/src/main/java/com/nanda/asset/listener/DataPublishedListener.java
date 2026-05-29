package com.nanda.asset.listener;

import com.nanda.common.event.DataPublishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DataPublishedListener {

    private final DataPublishedHandler dataPublishedHandler;

    @EventListener
    public void onDataPublished(DataPublishedEvent event) {
        dataPublishedHandler.handle(event.getEmpiId(), event.getSpecialtyType(),
                event.getRecordId(), event.getOrgId());
    }
}
