package com.nanda.acceptance.support;

import com.alibaba.excel.EasyExcel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExcelTestDataBuilder {

    private ExcelTestDataBuilder() {
    }

    public static byte[] metabolicPatientRow(String sourceRef, String patientName) {
        List<List<String>> head = new ArrayList<List<String>>();
        head.add(Collections.singletonList("sourceRef"));
        head.add(Collections.singletonList("patientName"));
        head.add(Collections.singletonList("gender"));
        head.add(Collections.singletonList("birthDate"));
        head.add(Collections.singletonList("diagnosisCode"));
        head.add(Collections.singletonList("diagnosisName"));
        head.add(Collections.singletonList("recordTime"));

        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("sourceRef", sourceRef);
        row.put("patientName", patientName);
        row.put("gender", "M");
        row.put("birthDate", "1980-01-15");
        row.put("diagnosisCode", "E11.9");
        row.put("diagnosisName", "Type2Diabetes");
        row.put("recordTime", "2026-05-01T10:00:00");

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add(Arrays.asList(
                row.get("sourceRef"),
                row.get("patientName"),
                row.get("gender"),
                row.get("birthDate"),
                row.get("diagnosisCode"),
                row.get("diagnosisName"),
                row.get("recordTime")
        ));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EasyExcel.write(output).head(head).sheet("template").doWrite(data);
        return output.toByteArray();
    }
}
