package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@CoversReqGroup("BP-09")
@AcceptanceSpringTest
public class Bp09KnowledgePatient360AcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp09KnowledgeCockpitPatient360() throws Exception {
        Map<String, Object> doc = new HashMap<String, Object>();
        doc.put("title", "Acceptance Guideline");
        doc.put("docType", "GUIDELINE");
        doc.put("fileRef", "acceptance://guideline/1");
        doc.put("authors", Arrays.asList("Acceptance Author"));
        doc.put("tags", Arrays.asList("acceptance", "diabetes"));
        JsonNode imported = api.post("/knowledge/documents", doc);
        Long docId = imported.path("data").path("id").asLong();

        api.get("/knowledge/documents/" + docId);
        api.get("/knowledge/documents?keyword=Acceptance&page=1&size=5");

        api.get("/specialty/cockpit/summary");
        api.get("/specialty/METABOLIC/overview");
        api.get("/specialty/METABOLIC/patients/1/360");
        api.get("/specialty/METABOLIC/patients/1/timeline");
        api.get("/empi/patients/1/timeline");

        JsonNode lineage = api.get("/governance/metadata/lineage?empiId=1");
        assertNotNull(lineage.path("data"));
    }
}
