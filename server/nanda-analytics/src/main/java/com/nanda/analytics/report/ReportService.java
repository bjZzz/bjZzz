package com.nanda.analytics.report;

import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.ReportCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.ReportDownloadVO;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.ReportVO;
import com.nanda.analytics.domain.entity.AnaReport;
import com.nanda.analytics.mapper.AnaReportMapper;
import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskCalculatorFactory;
import com.nanda.analytics.risk.RiskResult;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Path REPORT_DIR = Paths.get("data", "reports");

    private final RiskCalculatorFactory riskCalculatorFactory;
    private final PdfReportGenerator pdfReportGenerator;
    private final AnaReportMapper anaReportMapper;

    @Transactional
    public ReportVO generateRiskAssessment(ReportCreateRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        if (request.getModelCode() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少风险模型编码");
        }
        RiskCalculator calculator = riskCalculatorFactory.get(request.getModelCode());
        RiskResult result = calculator.calculate(request.getInput());

        String title = request.getTitle() != null ? request.getTitle() : "Risk Assessment Report";
        byte[] pdf = pdfReportGenerator.generateRiskReport(title, request.getModelCode(),
                request.getEmpiId(), result, userId);

        Long reportId = IdGenerator.nextId();
        String fileRef = writeFile(reportId, pdf);

        AnaReport report = new AnaReport();
        report.setId(reportId);
        report.setReportType("RISK_ASSESSMENT");
        report.setSourceId(request.getEmpiId());
        report.setFileRef(fileRef);
        report.setStatus("COMPLETED");
        report.setUserId(userId);
        report.setOrgId(orgId);
        report.setCreatedAt(LocalDateTime.now());
        anaReportMapper.insert(report);

        return toVO(report);
    }

    public ReportVO get(Long id) {
        return toVO(requireReport(id));
    }

    public ReportDownloadVO download(Long id) {
        AnaReport report = requireReport(id);
        if (report.getFileRef() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报告文件不存在");
        }
        ReportDownloadVO vo = new ReportDownloadVO();
        vo.setFileName("report-" + id + ".pdf");
        vo.setContentType("application/pdf");
        vo.setContent(readFile(report.getFileRef()));
        return vo;
    }

    private String writeFile(Long reportId, byte[] content) {
        try {
            Files.createDirectories(REPORT_DIR);
            Path file = REPORT_DIR.resolve(reportId + ".pdf");
            Files.write(file, content);
            return file.toString();
        } catch (IOException ex) {
            log.error("Failed to write report file id={}", reportId, ex);
            throw new IllegalStateException("报告文件写入失败", ex);
        }
    }

    private byte[] readFile(String fileRef) {
        try {
            return Files.readAllBytes(Paths.get(fileRef));
        } catch (IOException ex) {
            log.error("Failed to read report file ref={}", fileRef, ex);
            throw new IllegalStateException("报告文件读取失败", ex);
        }
    }

    private AnaReport requireReport(Long id) {
        AnaReport report = anaReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报告不存在");
        }
        if (!report.getOrgId().equals(AnalyticsOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该报告");
        }
        return report;
    }

    private ReportVO toVO(AnaReport report) {
        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setReportType(report.getReportType());
        vo.setSourceId(report.getSourceId());
        vo.setStatus(report.getStatus());
        vo.setCreatedAt(report.getCreatedAt());
        return vo;
    }
}
