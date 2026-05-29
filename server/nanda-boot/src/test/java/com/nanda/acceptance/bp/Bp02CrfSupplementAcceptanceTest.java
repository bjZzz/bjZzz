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

@CoversReqGroup("BP-02")
@AcceptanceSpringTest
public class Bp02CrfSupplementAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcBp02CrfDesignSubmitAndDualScreenSupplement() throws Exception {
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("formCode", "ACC-CRF-" + System.currentTimeMillis());
        form.put("formName", "AcceptanceCRF");
        form.put("specialtyType", "METABOLIC");
        form.put("schemaJson", "{\"fields\":[{\"code\":\"hba1c\",\"type\":\"number\",\"label\":\"HbA1c\"}]}");

        JsonNode created = api.post("/governance/crf/forms", form);
        Long formId = created.path("data").path("id").asLong();
        api.post("/governance/crf/forms/" + formId + "/publish", null);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("formId", formId);
        response.put("empiId", 1L);
        response.put("answersJson", "{\"hba1c\":7.2}");
        JsonNode submitted = api.post("/governance/crf/forms/responses", response);
        assertNotNull(submitted.path("data").path("id"));

        Map<String, Object> supplement = new HashMap<String, Object>();
        supplement.put("patientId", 1L);
        supplement.put("fieldCode", "hba1c");
        supplement.put("fieldValue", "7.0");
        supplement.put("sourceSnapshotJson", "{\"source\":\"acceptance\"}");
        JsonNode supplementResult = api.post("/quality/supplement/dual-screen", supplement);
        assertNotNull(supplementResult.path("data").path("changeLogId"));
    }
}
