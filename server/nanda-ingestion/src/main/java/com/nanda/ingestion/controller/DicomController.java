package com.nanda.ingestion.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.ingestion.dicom.DicomIngestService;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.DicomMetadataRequest;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.DicomUploadResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "数据采集-DICOM")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/ingestion/dicom")
@RequiredArgsConstructor
public class DicomController {

    private final DicomIngestService dicomIngestService;

    @ApiOperation("上传 DICOM 文件")
    @PostMapping("/upload")
    @RequiresPermission("ingestion:dicom:write")
    public Result<DicomUploadResultVO> upload(@RequestPart("file") MultipartFile file) {
        return Result.ok(dicomIngestService.upload(file));
    }

    @ApiOperation("提交 DICOM 元数据")
    @PostMapping("/metadata")
    @RequiresPermission("ingestion:dicom:write")
    public Result<DicomUploadResultVO> metadata(@RequestBody DicomMetadataRequest request) {
        return Result.ok(dicomIngestService.ingestMetadata(request));
    }
}
