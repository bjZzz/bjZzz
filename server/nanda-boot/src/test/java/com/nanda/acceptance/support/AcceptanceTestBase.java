package com.nanda.acceptance.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class AcceptanceTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected AcceptanceApiClient api;

    @BeforeEach
    void setUpAcceptanceClient() throws Exception {
        api = new AcceptanceApiClient(mockMvc, objectMapper);
        api.login("admin", "admin123");
    }
}
