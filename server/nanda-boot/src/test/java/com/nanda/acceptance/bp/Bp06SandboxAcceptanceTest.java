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

@CoversReqGroup("BP-06")
@AcceptanceSpringTest
public class Bp06SandboxAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp06SandboxSessionJobAndStatistics() throws Exception {
        JsonNode methods = api.get("/analytics/statistics/methods");
        assertTrue(methods.path("data").isArray());

        Map<String, Object> statInput = new HashMap<String, Object>();
        statInput.put("input", new HashMap<String, Object>());
        statInput.put("persist", false);
        JsonNode stat = api.post("/analytics/statistics/descriptive_continuous", statInput);
        assertNotNull(stat.path("data"));

        JsonNode session = api.post("/sandbox/sessions", new HashMap<String, Object>());
        String sessionId = session.path("data").path("sessionId").asText();
        assertNotNull(sessionId);

        Map<String, Object> mount = new HashMap<String, Object>();
        mount.put("sessionId", sessionId);
        mount.put("datasetRef", "minio://sandbox/acceptance/sample.parquet");
        api.post("/sandbox/datasets/mount", mount);

        Map<String, Object> job = new HashMap<String, Object>();
        job.put("sessionId", sessionId);
        job.put("jobType", "NOTEBOOK");
        JsonNode jobResult = api.post("/sandbox/jobs", job);
        String jobId = jobResult.path("data").path("jobId").asText();
        api.get("/sandbox/jobs/" + jobId);

        JsonNode dashboards = api.get("/dashboards?page=1&size=5");
        assertNotNull(dashboards.path("data"));
    }
}
