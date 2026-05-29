package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.support.AcceptanceFixtures;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@CoversReqGroup("BP-08")
@AcceptanceSpringTest
public class Bp08IntegrationAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp08FhirReadAndWriteback() throws Exception {
        JsonNode patient = api.get("/integration/fhir/Patient/1");
        assertNotNull(patient.path("data"));

        JsonNode observations = api.get("/integration/fhir/Observation?patient=1");
        assertNotNull(observations.path("data"));

        AcceptanceFixtures.ensureWritebackEndpoint(api);
        JsonNode result = api.post("/integration/writeback", AcceptanceFixtures.writebackPayload(api, 1L));
        assertNotNull(result.path("data").path("status"));
    }
}
