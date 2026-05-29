package com.nanda.analytics.export;

import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO;
import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CdiscExportAdapter {

    public byte[] toOdm(List<SearchHitVO> hits, Long taskId) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ODM FileType=\"Snapshot\" FileOID=\"TASK-").append(taskId).append("\" xmlns=\"http://www.cdisc.org/ns/odm/v1.3\">\n");
        xml.append("  <ClinicalData StudyOID=\"NANDA-STUDY\" MetaDataVersionOID=\"v1.0\">\n");
        for (SearchHitVO hit : hits) {
            xml.append("    <SubjectData SubjectKey=\"EMP").append(hit.getEmpiId()).append("\">\n");
            xml.append("      <ItemData ItemOID=\"IT.EMPI\" Value=\"").append(hit.getEmpiId()).append("\"/>\n");
            if (hit.getDiagnosisCodes() != null) {
                xml.append("      <ItemData ItemOID=\"IT.DIAG\" Value=\"")
                        .append(escape(hit.getDiagnosisCodes())).append("\"/>\n");
            }
            if (hit.getDemographics() != null) {
                xml.append("      <ItemData ItemOID=\"IT.DEMO\" Value=\"")
                        .append(escape(hit.getDemographics())).append("\"/>\n");
            }
            xml.append("    </SubjectData>\n");
        }
        xml.append("  </ClinicalData>\n");
        xml.append("</ODM>\n");
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toCsv(List<SearchHitVO> hits) {
        StringBuilder csv = new StringBuilder("empi_id,specialty_types,diagnosis_codes,demographics,completeness_score\n");
        for (SearchHitVO hit : hits) {
            csv.append(hit.getEmpiId()).append(',')
                    .append(quote(hit.getSpecialtyTypes())).append(',')
                    .append(quote(hit.getDiagnosisCodes())).append(',')
                    .append(quote(hit.getDemographics())).append(',')
                    .append(quote(hit.getCompletenessScore())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toJson(List<SearchHitVO> hits) {
        return JsonUtils.toJson(hits).getBytes(StandardCharsets.UTF_8);
    }

    private String quote(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    }
}
