package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoversReqGroup("BP-07")
@AcceptanceSpringTest
public class Bp07RiskReportAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp07RiskModelsAndPdfReport() throws Exception {
        JsonNode models = api.get("/risk-models");
        assertFalse(models.path("data").isEmpty());

        Map<String, Object> assess = new HashMap<String, Object>();
        assess.put("empiId", 1L);
        assess.put("input", new HashMap<String, Object>());
        JsonNode risk = api.post("/risk-models/ascvd", assess);
        assertNotNull(risk.path("data"));

        Map<String, Object> report = new HashMap<String, Object>();
        report.put("empiId", 1L);
        report.put("modelCode", "ascvd");
        JsonNode reportTask = api.post("/reports/risk-assessment", report);
        Long reportId = reportTask.path("data").path("id").asLong();
        assertTrue(api.getBinary("/reports/" + reportId + "/download").length > 0);
    }
}
