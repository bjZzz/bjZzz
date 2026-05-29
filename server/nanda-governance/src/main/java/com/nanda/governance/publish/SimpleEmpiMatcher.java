package com.nanda.governance.publish;

import com.nanda.common.crypto.CryptoService;
import com.nanda.common.util.IdGenerator;
import com.nanda.governance.cleaning.CleanedRecord;
import com.nanda.governance.publish.entity.EmpiIdentifier;
import com.nanda.governance.publish.entity.EmpiMaster;
import com.nanda.governance.publish.mapper.EmpiIdentifierMapper;
import com.nanda.governance.publish.mapper.EmpiMasterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimpleEmpiMatcher {

    private static final String ID_TYPE = "SOURCE_REF";

    private final EmpiMasterMapper empiMasterMapper;
    private final EmpiIdentifierMapper empiIdentifierMapper;
    private final CryptoService cryptoService;

    public Long match(CleanedRecord record, Long orgId) {
        if (Boolean.TRUE.equals(record.getPayload().get("_dedup_skipped"))) {
            record.setSkipped(true);
            return null;
        }
        String sourceRef = record.getSourceRef();
        String idHash = cryptoService.hashForIndex(sourceRef, "empi");
        Long existing = empiMasterMapper.findEmpiIdByHash(ID_TYPE, idHash);
        if (existing != null) {
            record.setEmpiId(existing);
            return existing;
        }
        Map<String, Object> payload = record.getPayload();
        EmpiMaster master = new EmpiMaster();
        master.setId(IdGenerator.nextId());
        master.setDisplayName(stringVal(payload.get("name"), stringVal(payload.get("displayName"), sourceRef)));
        master.setGender(stringVal(payload.get("gender"), null));
        master.setMergeStatus("ACTIVE");
        master.setMatchConfidence(new BigDecimal("1.0000"));
        master.setOrgId(orgId);
        master.setCreatedAt(LocalDateTime.now());
        master.setUpdatedAt(LocalDateTime.now());
        master.setDeleted(0);
        empiMasterMapper.insert(master);

        EmpiIdentifier identifier = new EmpiIdentifier();
        identifier.setId(IdGenerator.nextId());
        identifier.setEmpiId(master.getId());
        identifier.setIdType(ID_TYPE);
        identifier.setIdValueEnc(sourceRef);
        identifier.setIdHash(idHash);
        identifier.setSourceSystem("STAGING");
        identifier.setIsPrimary(1);
        empiIdentifierMapper.insert(identifier);

        record.setEmpiId(master.getId());
        return master.getId();
    }

    private String stringVal(Object primary, String fallback) {
        if (primary != null && !String.valueOf(primary).isEmpty()) {
            return String.valueOf(primary);
        }
        return fallback;
    }
}
