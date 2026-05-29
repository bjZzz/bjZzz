package com.nanda.integration.fhir;

import com.nanda.integration.config.EndpointConfigService;
import com.nanda.integration.domain.entity.IntEndpointConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FhirResourceProvider {

    private final EndpointConfigService endpointConfigService;

    public Map<String, Object> patient(Long empiId, Long orgId, String apiKey) {
        IntEndpointConfig endpoint = endpointConfigService.requireActive(
                EndpointConfigService.TYPE_FHIR, null, orgId);
        endpointConfigService.verifyAccess(endpoint, apiKey, "integration:fhir:read");

        Map<String, Object> resource = new LinkedHashMap<String, Object>();
        resource.put("resourceType", "Patient");
        resource.put("id", String.valueOf(empiId));
        resource.put("active", true);
        resource.put("identifier", identifiers(empiId));
        resource.put("name", names(empiId));
        resource.put("gender", "unknown");
        resource.put("managingOrganization", reference("Organization/" + orgId));
        resource.put("extension", specialtyExtensions());
        return resource;
    }

    public Map<String, Object> observations(Long empiId, Long orgId, String apiKey) {
        IntEndpointConfig endpoint = endpointConfigService.requireActive(
                EndpointConfigService.TYPE_FHIR, null, orgId);
        endpointConfigService.verifyAccess(endpoint, apiKey, "integration:fhir:read");

        List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
        Map<String, Object> entry = new LinkedHashMap<String, Object>();
        entry.put("resource", observation(empiId));
        entries.add(entry);

        Map<String, Object> bundle = new LinkedHashMap<String, Object>();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "searchset");
        bundle.put("total", entries.size());
        bundle.put("entry", entries);
        return bundle;
    }

    private List<Map<String, Object>> identifiers(Long empiId) {
        List<Map<String, Object>> identifiers = new ArrayList<Map<String, Object>>();
        Map<String, Object> identifier = new LinkedHashMap<String, Object>();
        identifier.put("system", "urn:nanda:empi");
        identifier.put("value", String.valueOf(empiId));
        identifiers.add(identifier);
        return identifiers;
    }

    private List<Map<String, Object>> names(Long empiId) {
        List<Map<String, Object>> names = new ArrayList<Map<String, Object>>();
        Map<String, Object> name = new LinkedHashMap<String, Object>();
        name.put("use", "official");
        name.put("text", "Mock Patient " + empiId);
        names.add(name);
        return names;
    }

    private List<Map<String, Object>> specialtyExtensions() {
        List<Map<String, Object>> extensions = new ArrayList<Map<String, Object>>();
        Map<String, Object> extension = new LinkedHashMap<String, Object>();
        extension.put("url", "urn:nanda:fhir-mode");
        extension.put("valueString", "mock");
        extensions.add(extension);
        return extensions;
    }

    private Map<String, Object> observation(Long empiId) {
        Map<String, Object> resource = new LinkedHashMap<String, Object>();
        resource.put("resourceType", "Observation");
        resource.put("id", "mock-observation-" + empiId);
        resource.put("status", "final");
        resource.put("code", code("MOCK-RISK"));
        resource.put("subject", reference("Patient/" + empiId));
        resource.put("valueQuantity", valueQuantity());
        return resource;
    }

    private Map<String, Object> code(String examCode) {
        Map<String, Object> code = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> coding = new ArrayList<Map<String, Object>>();
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("system", "urn:nanda:lab-code");
        item.put("code", examCode);
        coding.add(item);
        code.put("coding", coding);
        code.put("text", examCode);
        return code;
    }

    private Map<String, Object> valueQuantity() {
        Map<String, Object> value = new LinkedHashMap<String, Object>();
        value.put("value", 0);
        value.put("unit", "score");
        return value;
    }

    private Map<String, Object> reference(String reference) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("reference", reference);
        return result;
    }
}
