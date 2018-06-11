package io.spring2go.zuul.common;

/**
 * HTTP Headers that are accessed or added by Spring2go Zuul Gateway
 */
public class ZuulHeaders {
    public static final String TRANSFER_ENCODING = "transfer-encoding";
    public static final String CHUNKED = "chunked";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String ACCEPT_ENCODING = "accept-encoding";
    public static final String CONNECTION = "connection";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String HOST = "host";
    public static final String X_FORWARDED_PROTO = "x-forwarded-proto";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";

    public static final String X_ZUUL = "x-zuul";
    public static final String X_ZUUL_INSTANCE = "x-zuul-instance";
    public static final String X_ORIGINATING_URL = "x-originating-url";
    public static final String X_ZUUL_ERROR_CAUSE = "x-zuul-error-cause";
    public static final String X_ZUUL_CLIENT_HOST = "x-zuul-client-host";
    public static final String X_ZUUL_CLIENT_PROTO = "x-zuul-client-proto";
    public static final String X_ZUUL_SURGICAL_FILTER = "x-zuul-surgical-filter";
    public static final String X_ZUUL_FILTER_EXECUTION_STATUS = "x-zuul-filter-executions";
    public static final String X_ZUUL_REQUEST_TOPLEVEL_ID = "x-zuul-request.toplevel.uuid";
}