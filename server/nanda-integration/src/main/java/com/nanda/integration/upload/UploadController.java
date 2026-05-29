package com.nanda.integration.upload;

import com.alibaba.excel.EasyExcel;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.UploadResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(tags = "集成-分中心上传")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/integration/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadBatchService uploadBatchService;

    @ApiOperation("Excel模板上传")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresPermission("integration:upload:write")
    public Result<UploadResultVO> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam String templateType,
                                         @RequestParam(required = false) String clientRequestId) {
        return Result.ok(uploadBatchService.upload(file, templateType, clientRequestId));
    }

    @ApiOperation("下载上传模板")
    @GetMapping("/templates/{type}")
    @RequiresPermission("integration:upload:write")
    public ResponseEntity<byte[]> template(@PathVariable("type") String templateType) {
        byte[] content = buildTemplate(templateType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"nanda-" + templateType + "-template.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    private byte[] buildTemplate(String templateType) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EasyExcel.write(output).head(templateHead(templateType)).sheet("template").doWrite(Collections.emptyList());
        return output.toByteArray();
    }

    private List<List<String>> templateHead(String templateType) {
        List<List<String>> head = new ArrayList<List<String>>();
        head.add(Collections.singletonList("sourceRef"));
        head.add(Collections.singletonList("patientName"));
        head.add(Collections.singletonList("gender"));
        head.add(Collections.singletonList("birthDate"));
        head.add(Collections.singletonList("diagnosisCode"));
        head.add(Collections.singletonList("diagnosisName"));
        head.add(Collections.singletonList("recordTime"));
        return head;
    }
}
