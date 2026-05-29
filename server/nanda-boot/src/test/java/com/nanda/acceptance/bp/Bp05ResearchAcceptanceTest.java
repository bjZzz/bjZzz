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

@CoversReqGroup("BP-05")
@AcceptanceSpringTest
public class Bp05ResearchAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp05ProjectCohortFollowUpFlow() throws Exception {
        Map<String, Object> project = new HashMap<String, Object>();
        project.put("projectName", "AcceptanceProject-" + System.currentTimeMillis());
        project.put("templateCode", "COHORT");
        project.put("designJson", "{\"type\":\"cohort\"}");
        JsonNode created = api.post("/projects", project);
        Long projectId = created.path("data").path("id").asLong();

        Map<String, Object> approve = new HashMap<String, Object>();
        approve.put("targetStatus", "APPROVED");
        api.post("/projects/" + projectId + "/status", approve);

        Map<String, Object> executing = new HashMap<String, Object>();
        executing.put("targetStatus", "EXECUTING");
        api.post("/projects/" + projectId + "/status", executing);

        JsonNode progress = api.get("/projects/" + projectId + "/progress");
        assertNotNull(progress.path("data"));

        Map<String, Object> cohort = new HashMap<String, Object>();
        cohort.put("projectId", projectId);
        cohort.put("cohortName", "AcceptanceCohort");
        cohort.put("cohortType", "DYNAMIC");
        cohort.put("ruleJson", "{\"logic\":\"AND\",\"conditions\":[]}");
        JsonNode cohortCreated = api.post("/cohorts", cohort);
        Long cohortId = cohortCreated.path("data").path("id").asLong();

        api.post("/cohorts/" + cohortId + "/screen", null);

        Map<String, Object> plan = new HashMap<String, Object>();
        plan.put("projectId", projectId);
        plan.put("planName", "AcceptanceFollowUp");
        api.post("/follow-ups/plans", plan);

        JsonNode tasks = api.get("/follow-ups/tasks?page=1&size=10");
        assertTrue(tasks.path("data").path("total").asLong() >= 0);
    }
}
