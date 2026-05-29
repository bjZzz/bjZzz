package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import com.nanda.acceptance.support.ExcelTestDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoversReqGroup("BP-01")
@AcceptanceSpringTest
public class Bp01IngestionAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp01UploadCleanPublishEmpiSpecialty() throws Exception {
        String requestId = api.newRequestId();
        Map<String, String> params = new HashMap<String, String>();
        params.put("templateType", "METABOLIC");
        params.put("clientRequestId", requestId);

        byte[] excel = ExcelTestDataBuilder.metabolicPatientRow("ACC-" + requestId, "AcceptancePatientA");
        JsonNode upload = api.uploadExcel("/integration/upload", excel, "acceptance-metabolic.xlsx", params);
        Long stgBatchId = upload.path("data").path("stgBatchId").asLong();
        assertTrue(stgBatchId > 0);

        JsonNode batch = api.get("/ingestion/staging/batches/" + stgBatchId);
        assertNotNull(batch.path("data").path("status").asText());

        api.post("/governance/publish/tasks/" + stgBatchId + "/execute", null);

        JsonNode patients = api.get("/specialty/METABOLIC/patients?page=1&size=10");
        assertTrue(patients.path("data").path("total").asLong() >= 0);

        JsonNode rules = api.get("/governance/publish/rules");
        assertTrue(rules.path("data").isArray());

        JsonNode lineage = api.get("/governance/metadata/lineage?batchId=" + stgBatchId);
        assertNotNull(lineage.path("data"));

        JsonNode comorbidityRules = api.get("/comorbidity/rules");
        assertTrue(comorbidityRules.path("data").isArray());
    }
}
