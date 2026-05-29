package com.nanda.acceptance.report;

import com.nanda.acceptance.annotation.CoversReqGroup;
import com.nanda.acceptance.bp.Bp01IngestionAcceptanceTest;
import com.nanda.acceptance.bp.Bp02CrfSupplementAcceptanceTest;
import com.nanda.acceptance.bp.Bp03QualityAcceptanceTest;
import com.nanda.acceptance.bp.Bp04SearchExportAcceptanceTest;
import com.nanda.acceptance.bp.Bp05ResearchAcceptanceTest;
import com.nanda.acceptance.bp.Bp06SandboxAcceptanceTest;
import com.nanda.acceptance.bp.Bp07RiskReportAcceptanceTest;
import com.nanda.acceptance.bp.Bp08IntegrationAcceptanceTest;
import com.nanda.acceptance.bp.Bp09KnowledgePatient360AcceptanceTest;
import com.nanda.acceptance.bp.PlatformAcceptanceTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class AcceptanceTestRegistry {

    private static final List<Class<?>> BP_TESTS = Arrays.asList(
            PlatformAcceptanceTest.class,
            Bp01IngestionAcceptanceTest.class,
            Bp02CrfSupplementAcceptanceTest.class,
            Bp03QualityAcceptanceTest.class,
            Bp04SearchExportAcceptanceTest.class,
            Bp05ResearchAcceptanceTest.class,
            Bp06SandboxAcceptanceTest.class,
            Bp07RiskReportAcceptanceTest.class,
            Bp08IntegrationAcceptanceTest.class,
            Bp09KnowledgePatient360AcceptanceTest.class
    );

    private AcceptanceTestRegistry() {
    }

    static Set<String> coveredGroups() {
        Set<String> groups = new HashSet<String>();
        for (Class<?> type : BP_TESTS) {
            CoversReqGroup annotation = type.getAnnotation(CoversReqGroup.class);
            if (annotation != null) {
                groups.addAll(Arrays.asList(annotation.value()));
            }
        }
        return groups;
    }
}
