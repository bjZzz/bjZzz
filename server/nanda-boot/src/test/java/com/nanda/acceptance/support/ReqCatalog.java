package com.nanda.acceptance.support;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReqCatalog {

    private static final Pattern REQ_ID = Pattern.compile("REQ-(\\d{2})-(\\d{2})-(\\d{2})");

    private static final List<ReqEntry> ENTRIES = loadEntries();
    private static final Map<String, List<String>> GROUP_TO_REQS = buildGroupIndex();

    private ReqCatalog() {
    }

    public static List<ReqEntry> all() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static int expectedCount() {
        return 233;
    }

    public static List<String> resolveGroup(String group) {
        List<String> reqs = GROUP_TO_REQS.get(group);
        return reqs == null ? Collections.<String>emptyList() : Collections.unmodifiableList(reqs);
    }

    public static String resolveGroupForReq(String reqId) {
        for (Map.Entry<String, List<String>> entry : GROUP_TO_REQS.entrySet()) {
            if (entry.getValue().contains(reqId)) {
                return entry.getKey();
            }
        }
        return "UNMAPPED";
    }

    private static List<ReqEntry> loadEntries() {
        List<ReqEntry> entries = new ArrayList<ReqEntry>();
        InputStream stream = ReqCatalog.class.getResourceAsStream("/acceptance/req-catalog.tsv");
        if (stream == null) {
            throw new IllegalStateException("Missing /acceptance/req-catalog.tsv");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                int tab = line.indexOf('\t');
                if (tab <= 0) {
                    continue;
                }
                String id = line.substring(0, tab).trim();
                if (id.startsWith("\uFEFF")) {
                    id = id.substring(1);
                }
                String title = line.substring(tab + 1).trim();
                entries.add(new ReqEntry(id, title, resolveGroupForReqId(id)));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load REQ catalog", ex);
        }
        if (entries.size() != expectedCount()) {
            throw new IllegalStateException("REQ catalog size " + entries.size() + " != " + expectedCount());
        }
        return entries;
    }

    private static Map<String, List<String>> buildGroupIndex() {
        Map<String, List<String>> index = new LinkedHashMap<String, List<String>>();
        for (ReqEntry entry : ENTRIES) {
            List<String> list = index.get(entry.getGroup());
            if (list == null) {
                list = new ArrayList<String>();
                index.put(entry.getGroup(), list);
            }
            list.add(entry.getId());
        }
        return index;
    }

    static String resolveGroupForReqId(String reqId) {
        Matcher matcher = REQ_ID.matcher(reqId);
        if (!matcher.matches()) {
            return "UNMAPPED";
        }
        String module = matcher.group(1);
        String section = matcher.group(2);
        int item = Integer.parseInt(matcher.group(3));

        if ("20".equals(module)) {
            return "PLATFORM";
        }
        if ("05".equals(module) && "07".equals(section)) {
            return "PLATFORM";
        }
        if ("01".equals(module) && "01".equals(section)) {
            return "BP-01";
        }
        if ("01".equals(module) && "03".equals(section)) {
            return "BP-01";
        }
        if ("01".equals(module) && "04".equals(section)) {
            return "BP-01";
        }
        if ("05".equals(module) && "03".equals(section)) {
            return "BP-01";
        }
        if ("05".equals(module) && "04".equals(section)) {
            return "BP-01";
        }
        if ("05".equals(module) && "02".equals(section) && item <= 18) {
            return "BP-01";
        }
        if ("01".equals(module) && "02".equals(section)) {
            return "BP-02";
        }
        if ("05".equals(module) && "06".equals(section)) {
            return "BP-02";
        }
        if ("05".equals(module) && "05".equals(section)) {
            return "BP-03";
        }
        if ("10".equals(module)) {
            return "BP-04";
        }
        if ("17".equals(module) || "18".equals(module) || "19".equals(module)) {
            return "BP-05";
        }
        if ("11".equals(module) && ("02".equals(section) || "03".equals(section))) {
            return "BP-06";
        }
        if ("14".equals(module)) {
            return "BP-06";
        }
        if ("11".equals(module) && "01".equals(section)) {
            return "BP-07";
        }
        if ("13".equals(module) && item <= 2) {
            return "BP-07";
        }
        if ("13".equals(module) && item >= 3) {
            return "BP-08";
        }
        if ("05".equals(module) && "02".equals(section) && item == 13) {
            return "BP-08";
        }
        if ("05".equals(module) && "01".equals(section)) {
            return "BP-09";
        }
        if ("05".equals(module) && "02".equals(section) && item >= 19) {
            return "BP-09";
        }
        return "UNMAPPED";
    }

    @Getter
    public static final class ReqEntry {
        private final String id;
        private final String title;
        private final String group;

        ReqEntry(String id, String title, String group) {
            this.id = id;
            this.title = title;
            this.group = group;
        }
    }
}
