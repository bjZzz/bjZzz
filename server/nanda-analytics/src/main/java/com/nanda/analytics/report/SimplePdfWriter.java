package com.nanda.analytics.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 轻量级 PDF 生成器（无第三方依赖），输出单页 A4 文本报告并支持对角水印。
 * 字体为标准 Type1 Helvetica，文本以 WinAnsi 编码，建议使用 ASCII 内容。
 */
public final class SimplePdfWriter {

    private SimplePdfWriter() {
    }

    public static byte[] generate(String title, List<String> lines, String watermark) {
        String content = buildContentStream(title, lines, watermark);
        byte[] contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);

        List<String> objects = new ArrayList<String>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>");
        objects.add("<< /Length " + contentBytes.length + " >>\nstream\n" + content + "\nendstream");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1);
            out.write(header);

            int[] offsets = new int[objects.size() + 1];
            int position = header.length;
            for (int i = 0; i < objects.size(); i++) {
                offsets[i + 1] = position;
                String obj = (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n";
                byte[] objBytes = obj.getBytes(StandardCharsets.ISO_8859_1);
                out.write(objBytes);
                position += objBytes.length;
            }

            int xrefStart = position;
            StringBuilder xref = new StringBuilder();
            int count = objects.size() + 1;
            xref.append("xref\n0 ").append(count).append("\n");
            xref.append("0000000000 65535 f \n");
            for (int i = 1; i < count; i++) {
                xref.append(String.format("%010d 00000 n \n", offsets[i]));
            }
            xref.append("trailer\n<< /Size ").append(count).append(" /Root 1 0 R >>\n");
            xref.append("startxref\n").append(xrefStart).append("\n%%EOF");
            out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("PDF 生成失败", ex);
        }
    }

    private static String buildContentStream(String title, List<String> lines, String watermark) {
        StringBuilder sb = new StringBuilder();
        if (watermark != null && !watermark.isEmpty()) {
            sb.append("q\n0.88 0.88 0.88 rg\nBT\n/F1 36 Tf\n");
            sb.append("0.7071 0.7071 -0.7071 0.7071 120 280 Tm\n");
            sb.append("(").append(escape(watermark)).append(") Tj\nET\nQ\n");
        }
        sb.append("BT\n0 0 0 rg\n/F1 18 Tf\n50 790 Td\n22 TL\n");
        sb.append("(").append(escape(title)).append(") Tj\n");
        sb.append("/F1 11 Tf\n");
        sb.append("T*\n");
        if (lines != null) {
            for (String line : lines) {
                sb.append("T* (").append(escape(line)).append(") Tj\n");
            }
        }
        sb.append("ET");
        return sb.toString();
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
