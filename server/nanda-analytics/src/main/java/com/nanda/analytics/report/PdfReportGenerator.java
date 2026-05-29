package com.nanda.analytics.report;

import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PdfReportGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成风险评估 PDF，水印嵌入操作者与时间戳（REQ-14-04-03~05）。
     */
    public byte[] generateRiskReport(String title, String modelCode, Long empiId,
                                     RiskResult result, Long userId) {
        List<String> lines = new ArrayList<String>();
        lines.add("Report Type   : Risk Assessment");
        lines.add("Model Code    : " + modelCode);
        lines.add("EMPI ID       : " + (empiId != null ? empiId : "-"));
        lines.add("Generated At  : " + LocalDateTime.now().format(FORMATTER));
        lines.add("Operator      : user-" + (userId != null ? userId : "-"));
        lines.add("");
        lines.add("---- Result ----");
        lines.add("Score         : " + result.getScore());
        lines.add("Risk Level    : " + safe(result.getRiskLevel()));
        lines.add("");
        lines.add("---- Details ----");
        for (Map.Entry<String, Object> entry : result.getDetail().entrySet()) {
            lines.add(pad(entry.getKey()) + ": " + safe(String.valueOf(entry.getValue())));
        }
        lines.add("");
        lines.add("This report is confidential and watermarked for audit.");

        String watermark = "user-" + (userId != null ? userId : "0") + "  "
                + LocalDateTime.now().format(FORMATTER);
        return SimplePdfWriter.generate(safe(title), lines, watermark);
    }

    private String pad(String key) {
        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < 14) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private String safe(String text) {
        return text != null ? text : "-";
    }
}
