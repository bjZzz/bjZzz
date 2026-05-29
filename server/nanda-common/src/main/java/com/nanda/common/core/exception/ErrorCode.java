package com.nanda.common.core.exception;

public final class ErrorCode {

    public static final int SUCCESS = 0;
    public static final int PARAM_INVALID = 40001;
    public static final int UNAUTHORIZED = 40101;
    public static final int FORBIDDEN = 40301;
    public static final int NOT_FOUND = 40401;
    public static final int CONFLICT = 40901;
    public static final int BUSINESS_RULE = 42201;
    public static final int INTERNAL_ERROR = 50001;

    public static final int AUTH_BAD_CREDENTIALS = 40102;
    public static final int AUTH_ACCOUNT_FROZEN = 41002;
    public static final int ORG_CODE_DUPLICATE = 41001;
    public static final int USERNAME_DUPLICATE = 40902;
    public static final int ORG_HAS_DEPENDENCY = 42202;
    public static final int ORG_CYCLE_REFERENCE = 42203;

    public static final int INGESTION_CONNECTION_FAILED = 42001;
    public static final int INGESTION_PARSE_FAILED = 42002;
    public static final int INGESTION_BATCH_NOT_RETRYABLE = 42003;
    public static final int INGESTION_WEBHOOK_AUTH_FAILED = 42004;
    public static final int INGESTION_DICOM_PARSE_FAILED = 42005;

    public static final int SANDBOX_UNAVAILABLE = 46001;
    public static final int SANDBOX_JOB_FAILED = 46002;

    public static final int INTEGRATION_TEMPLATE_MISMATCH = 47001;
    public static final int INTEGRATION_FILE_TOO_LARGE = 47002;
    public static final int INTEGRATION_DUPLICATE_REQUEST = 47003;
    public static final int INTEGRATION_ENDPOINT_DISABLED = 47004;
    public static final int INTEGRATION_WRITEBACK_FAILED = 47005;
    public static final int INTEGRATION_AUTH_FAILED = 47006;

    private ErrorCode() {
    }
}
