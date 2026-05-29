package com.nanda.asset.empi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.EmpiMaster;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class MatchScoreCalculator {

    public BigDecimal calculate(Map<String, Object> config, EmpiMaster candidate, MatchFeatures input) {
        double nameWeight = doubleVal(config.get("nameWeight"), 0.35);
        double phoneWeight = doubleVal(config.get("phoneWeight"), 0.25);
        double addressWeight = doubleVal(config.get("addressWeight"), 0.15);
        double birthDateWeight = doubleVal(config.get("birthDateWeight"), 0.15);
        double idCardWeight = doubleVal(config.get("idCardWeight"), 0.10);

        double score = nameWeight * nameSimilarity(candidate.getDisplayName(), input.getName())
                + phoneWeight * fieldPresent(input.getPhone())
                + addressWeight * fieldPresent(input.getAddress())
                + birthDateWeight * birthDateSimilarity(candidate.getBirthDate(), input.getBirthDate())
                + idCardWeight * fieldPresent(input.getIdCard());

        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal crossMatchScore(MatchFeatures input) {
        if (isBlank(input.getIdCard()) || isBlank(input.getName()) || isBlank(input.getBirthDate())) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("0.9900");
    }

    public Map<String, Object> buildFeatures(MatchFeatures input, EmpiMaster candidate, BigDecimal score) {
        Map<String, Object> features = new HashMap<String, Object>();
        features.put("name", input.getName());
        features.put("candidateName", candidate.getDisplayName());
        features.put("phone", input.getPhone());
        features.put("birthDate", input.getBirthDate());
        features.put("score", score);
        return features;
    }

    public Map<String, Object> defaultConfig() {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("nameWeight", 0.35);
        config.put("phoneWeight", 0.25);
        config.put("addressWeight", 0.15);
        config.put("birthDateWeight", 0.15);
        config.put("idCardWeight", 0.10);
        config.put("autoThreshold", 0.85);
        config.put("crossMatchThreshold", 0.99);
        return config;
    }

    public Map<String, Object> parseConfig(String ruleConfigJson) {
        if (ruleConfigJson == null || ruleConfigJson.isEmpty()) {
            return defaultConfig();
        }
        Map<String, Object> config = JsonUtils.fromJson(ruleConfigJson, new TypeReference<Map<String, Object>>() {
        });
        return config != null ? config : defaultConfig();
    }

    public BigDecimal threshold(Map<String, Object> config) {
        return BigDecimal.valueOf(doubleVal(config.get("autoThreshold"), 0.85));
    }

    public BigDecimal crossThreshold(Map<String, Object> config) {
        return BigDecimal.valueOf(doubleVal(config.get("crossMatchThreshold"), 0.99));
    }

    private double nameSimilarity(String left, String right) {
        if (isBlank(left) || isBlank(right)) {
            return 0.0;
        }
        String a = left.trim();
        String b = right.trim();
        if (a.equals(b)) {
            return 1.0;
        }
        if (a.equalsIgnoreCase(b)) {
            return 0.95;
        }
        return 0.0;
    }

    private double birthDateSimilarity(LocalDate candidateBirthDate, String inputBirthDate) {
        if (candidateBirthDate == null || isBlank(inputBirthDate)) {
            return 0.0;
        }
        try {
            LocalDate input = LocalDate.parse(inputBirthDate);
            return candidateBirthDate.equals(input) ? 1.0 : 0.0;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private double fieldPresent(String value) {
        return isBlank(value) ? 0.0 : 1.0;
    }

    private double doubleVal(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class MatchFeatures {
        private final String name;
        private final String phone;
        private final String address;
        private final String birthDate;
        private final String idCard;

        public MatchFeatures(String name, String phone, String address, String birthDate, String idCard) {
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.birthDate = birthDate;
            this.idCard = idCard;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public String getBirthDate() {
            return birthDate;
        }

        public String getIdCard() {
            return idCard;
        }

        public String normalizedName() {
            return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        }
    }
}
