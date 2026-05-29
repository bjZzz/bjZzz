package com.nanda.acceptance.bp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.support.AcceptanceSpringTest;
import com.nanda.acceptance.support.AcceptanceTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoversReqGroup("PLATFORM")
@AcceptanceSpringTest
public class PlatformAcceptanceTest extends AcceptanceTestBase {

    @Test
    void tcPlatformLoginLogoutAndAudit() throws Exception {
        JsonNode me = api.get("/auth/me");
        assertTrue(me.path("data").path("permissions").isArray());
        assertFalse(me.path("data").path("permissions").isEmpty());

        JsonNode orgTree = api.get("/orgs/tree");
        assertTrue(orgTree.path("data").isArray());

        JsonNode users = api.get("/users?page=1&size=10");
        assertTrue(users.path("data").path("total").asLong() >= 1);

        JsonNode roles = api.get("/roles");
        assertTrue(roles.path("data").isArray());

        JsonNode permissions = api.get("/permissions/tree");
        assertTrue(permissions.path("data").isArray());

        JsonNode audit = api.get("/audit/logs?page=1&size=5");
        assertNotNull(audit.path("data"));

        api.post("/auth/logout", null);
    }
}
