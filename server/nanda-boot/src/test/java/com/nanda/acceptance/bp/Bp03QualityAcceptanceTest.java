package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoversReqGroup("BP-03")
@AcceptanceSpringTest
public class Bp03QualityAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp03QualitySamplingReviewLoop() throws Exception {
        JsonNode dashboard = api.get("/quality/dashboard");
        assertTrue(dashboard.path("data").path("metrics").isArray());

        Map<String, Object> sample = new HashMap<String, Object>();
        sample.put("batchName", "ACC-QC-" + System.currentTimeMillis());
        sample.put("sampleSize", 5);
        sample.put("specialtyType", "METABOLIC");
        JsonNode sampleBatch = api.post("/quality/sampling/batches", sample);
        assertNotNull(sampleBatch.path("data").path("id"));

        JsonNode tasks = api.get("/quality/review-tasks?page=1&size=10");
        assertNotNull(tasks.path("data"));

        JsonNode compare = api.get("/quality/compare?specialtyType=METABOLIC&patientId=1");
        assertNotNull(compare.path("data"));
    }
}
