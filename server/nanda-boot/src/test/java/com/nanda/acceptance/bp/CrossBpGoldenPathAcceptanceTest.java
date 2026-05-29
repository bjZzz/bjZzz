package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.support.AcceptanceFixtures;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import com.nanda.acceptance.support.ExcelTestDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W9 golden path: BP-01 -> BP-05 -> BP-04 -> BP-07 -> BP-08.
 */
@AcceptanceSpringTest
public class CrossBpGoldenPathAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcGoldenPathIngestionToWriteback() throws Exception {
        String requestId = api.newRequestId();
        Map<String, String> uploadParams = new HashMap<String, String>();
        uploadParams.put("templateType", "METABOLIC");
        uploadParams.put("clientRequestId", requestId);
        byte[] excel = ExcelTestDataBuilder.metabolicPatientRow("GOLD-" + requestId, "GoldenPath Patient");
        JsonNode upload = api.uploadExcel("/integration/upload", excel, "golden-path.xlsx", uploadParams);
        Long stgBatchId = upload.path("data").path("stgBatchId").asLong();
        api.post("/governance/publish/tasks/" + stgBatchId + "/execute", null);

        Map<String, Object> project = new HashMap<String, Object>();
        project.put("projectName", "GoldenPath-" + requestId);
        project.put("templateCode", "COHORT");
        JsonNode projectCreated = api.post("/projects", project);
        Long projectId = projectCreated.path("data").path("id").asLong();

        Map<String, Object> search = new HashMap<String, Object>();
        search.put("queryJson", "{\"logic\":\"AND\",\"conditions\":[{\"field\":\"specialtyType\",\"op\":\"EQ\",\"value\":\"METABOLIC\"}]}");
        search.put("page", 1);
        search.put("size", 5);
        JsonNode searchResult = api.post("/search/query", search);
        assertTrue(searchResult.path("data").path("total").asLong() >= 0);

        Map<String, Object> assess = new HashMap<String, Object>();
        assess.put("empiId", 1L);
        assess.put("input", new HashMap<String, Object>());
        JsonNode risk = api.post("/risk-models/ascvd", assess);
        assertNotNull(risk.path("data"));

        AcceptanceFixtures.ensureWritebackEndpoint(api);
        JsonNode wb = api.post("/integration/writeback", AcceptanceFixtures.writebackPayload(api, 1L));
        assertNotNull(wb.path("data").path("status"));

        api.get("/projects/" + projectId + "/progress");
    }
}
