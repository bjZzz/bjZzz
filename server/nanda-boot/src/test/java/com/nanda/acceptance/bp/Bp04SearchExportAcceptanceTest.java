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

@CoversReqGroup("BP-04")
@AcceptanceSpringTest
public class Bp04SearchExportAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp04SearchExportApprovalFlow() throws Exception {
        Map<String, Object> search = new HashMap<String, Object>();
        search.put("queryName", "acceptance-search");
        search.put("queryJson", "{\"logic\":\"AND\",\"conditions\":[{\"field\":\"specialtyType\",\"op\":\"EQ\",\"value\":\"METABOLIC\"}]}");
        search.put("page", 1);
        search.put("size", 10);
        JsonNode searchResult = api.post("/search/query", search);
        assertNotNull(searchResult.path("data"));

        Map<String, Object> countNodes = new HashMap<String, Object>();
        countNodes.put("queryJson", search.get("queryJson"));
        JsonNode counts = api.post("/search/count-nodes", countNodes);
        assertTrue(counts.path("data").isArray());

        JsonNode suggest = api.get("/search/suggest?prefix=acc");
        assertTrue(suggest.path("data").isArray());

        Map<String, Object> export = new HashMap<String, Object>();
        export.put("queryJson", search.get("queryJson"));
        export.put("exportFormat", "CSV");
        JsonNode exportTask = api.post("/exports", export);
        Long exportId = exportTask.path("data").path("id").asLong();

        api.post("/exports/" + exportId + "/submit", null);
        Map<String, Object> approve = new HashMap<String, Object>();
        approve.put("comment", "acceptance approved");
        api.post("/exports/" + exportId + "/approve", approve);

        JsonNode exportDetail = api.get("/exports/" + exportId);
        assertNotNull(exportDetail.path("data").path("status"));
    }
}
