package com.nanda.acceptance.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nanda.acceptance.support.ReqCatalog;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC-REQ matrix audit: verifies 233 REQs are catalogued and mapped to BP acceptance tests.
 * Runs without MySQL.
 */
class ReqCoverageReportTest {

    @Test
    void catalogContains233Reqs() {
        assertEquals(233, ReqCatalog.all().size());
        assertEquals(233, ReqCatalog.expectedCount());
    }

    @Test
    void allReqsMappedToBusinessProcessGroups() {
        List<String> unmapped = new ArrayList<String>();
        for (ReqCatalog.ReqEntry entry : ReqCatalog.all()) {
            if ("UNMAPPED".equals(entry.getGroup())) {
                unmapped.add(entry.getId());
            }
        }
        assertTrue(unmapped.isEmpty(), "Unmapped REQs: " + unmapped);
    }

    @Test
    void acceptanceTestsCoverAllReqGroups() throws Exception {
        Set<String> coveredGroups = AcceptanceTestRegistry.coveredGroups();

        Set<String> expectedGroups = new TreeSet<String>();
        Map<String, List<String>> groupIndex = new LinkedHashMap<String, List<String>>();
        for (ReqCatalog.ReqEntry entry : ReqCatalog.all()) {
            groupIndex.computeIfAbsent(entry.getGroup(), k -> new ArrayList<String>()).add(entry.getId());
        }
        expectedGroups.addAll(groupIndex.keySet());

        Set<String> missingGroups = new TreeSet<String>(expectedGroups);
        missingGroups.removeAll(coveredGroups);
        assertTrue(missingGroups.isEmpty(), "Missing @CoversReqGroup for: " + missingGroups);

        Set<String> coveredReqs = new TreeSet<String>();
        for (String group : coveredGroups) {
            coveredReqs.addAll(ReqCatalog.resolveGroup(group));
        }
        Set<String> allReqs = new TreeSet<String>();
        for (ReqCatalog.ReqEntry entry : ReqCatalog.all()) {
            allReqs.add(entry.getId());
        }
        Set<String> uncovered = new TreeSet<String>(allReqs);
        uncovered.removeAll(coveredReqs);
        assertTrue(uncovered.isEmpty(), "Uncovered REQs: " + uncovered);

        writeReport(coveredGroups, groupIndex, coveredReqs);
    }

    private void writeReport(Set<String> coveredGroups,
                           Map<String, List<String>> groupIndex,
                           Set<String> coveredReqs) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("totalReqs", ReqCatalog.expectedCount());
        root.put("coveredReqs", coveredReqs.size());
        root.put("coveragePercent", coveredReqs.size() * 100.0 / ReqCatalog.expectedCount());

        ArrayNode groups = root.putArray("groups");
        for (String group : new TreeSet<String>(coveredGroups)) {
            ObjectNode node = groups.addObject();
            node.put("group", group);
            node.put("reqCount", groupIndex.getOrDefault(group, Collections.<String>emptyList()).size());
        }

        Path reportDir = Paths.get("target");
        Files.createDirectories(reportDir);
        Path reportFile = reportDir.resolve("acceptance-req-report.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(reportFile.toFile(), root);
    }
}
