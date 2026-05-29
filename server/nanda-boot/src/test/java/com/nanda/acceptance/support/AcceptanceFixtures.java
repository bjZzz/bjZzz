package com.nanda.acceptance.support;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public final class AcceptanceFixtures {

    private AcceptanceFixtures() {
    }

    public static void ensureWritebackEndpoint(AcceptanceApiClient api) throws Exception {
        Map<String, Object> endpoint = new HashMap<String, Object>();
        endpoint.put("endpointCode", "acceptance-default");
        endpoint.put("endpointType", "WRITEBACK");
        endpoint.put("baseUrl", "http://127.0.0.1:59999/acceptance/writeback");
        endpoint.put("authType", "API_KEY");
        endpoint.put("authConfigJson", "{\"apiKey\":\"acceptance-key\"}");
        endpoint.put("status", "ACTIVE");
        try {
            api.post("/integration/endpoints", endpoint);
        } catch (AssertionError ex) {
            // endpoint may already exist from a previous run
        }
    }

    public static Map<String, Object> writebackPayload(AcceptanceApiClient api, Long empiId) {
        Map<String, Object> summary = new HashMap<String, Object>();
        summary.put("score", 12.5);
        summary.put("riskLevel", "MODERATE");

        Map<String, Object> writeback = new HashMap<String, Object>();
        writeback.put("endpointCode", "acceptance-default");
        writeback.put("clientRequestId", api.newRequestId());
        writeback.put("empiId", empiId);
        writeback.put("assessmentType", "ASCVD");
        writeback.put("resultSummary", summary);
        return writeback;
    }

    public static Long firstEmpiId(AcceptanceApiClient api) throws Exception {
        JsonNode patients = api.get("/specialty/METABOLIC/patients?page=1&size=1");
        JsonNode items = patients.path("data").path("items");
        if (items.isArray() && items.size() > 0) {
            return items.get(0).path("empiId").asLong(1L);
        }
        return 1L;
    }
}
