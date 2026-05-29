package com.nanda.acceptance.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanda.common.core.constant.CommonConstants;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AcceptanceApiClient {

    private static final Long DEFAULT_ORG_ID = 1L;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private String accessToken;

    public AcceptanceApiClient(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public void login(String username, String password) throws Exception {
        Map<String, String> body = new HashMap<String, String>();
        body.put("username", username);
        body.put("password", password);
        JsonNode result = postInternal("/auth/login", body, false);
        accessToken = result.path("data").path("accessToken").asText();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("Login failed for user " + username);
        }
    }

    public byte[] getBinary(String path) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(CommonConstants.API_PREFIX + path)
                        .header("Authorization", bearer())
                        .header(CommonConstants.HEADER_ORG_ID, DEFAULT_ORG_ID))
                .andExpect(status().isOk())
                .andReturn();
        return mvcResult.getResponse().getContentAsByteArray();
    }

    public JsonNode get(String path) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(CommonConstants.API_PREFIX + path)
                        .header("Authorization", bearer())
                        .header(CommonConstants.HEADER_ORG_ID, DEFAULT_ORG_ID))
                .andExpect(status().isOk())
                .andReturn();
        return readResult(mvcResult);
    }

    public JsonNode post(String path, Object body) throws Exception {
        return postInternal(path, body, true);
    }

    public JsonNode uploadExcel(String path, byte[] content, String filename, Map<String, String> params)
            throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(CommonConstants.API_PREFIX + path)
                .file(file)
                .header("Authorization", bearer())
                .header(CommonConstants.HEADER_ORG_ID, DEFAULT_ORG_ID);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder = builder.param(entry.getKey(), entry.getValue());
            }
        }
        MvcResult mvcResult = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readResult(mvcResult);
    }

    public String newRequestId() {
        return "acceptance-" + UUID.randomUUID();
    }

    private JsonNode postInternal(String path, Object body, boolean authenticated) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(CommonConstants.API_PREFIX + path)
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            builder.content(objectMapper.writeValueAsBytes(body));
        }
        if (authenticated) {
            builder.header("Authorization", bearer())
                    .header(CommonConstants.HEADER_ORG_ID, DEFAULT_ORG_ID);
        }
        MvcResult mvcResult = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readResult(mvcResult);
    }

    private JsonNode readResult(MvcResult mvcResult) throws Exception {
        String json = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        if (root.path("code").asInt(-1) != 0) {
            throw new AssertionError("API error code=" + root.path("code").asInt()
                    + " message=" + root.path("message").asText() + " body=" + json);
        }
        return root;
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }
}
