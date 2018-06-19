package io.spring2go.zuul.common;

public class Constants {

	// config items
	public static final String APPLICATION_NAME = "zuul-gateway";
	public static final String DEPLOYMENT_APPLICATION_ID = "archaius.deployment.applicationId";
	public static final String DEPLOY_CONFIG_URL = "archaius.configurationSource.additionalUrls";
	public static final String DEPLOY_ENVIRONMENT = "archaius.deployment.environment";

	public static final String DATA_SOURCE_CLASS_NAME = "zuul.data-source.class-name";
	public static final String DATA_SOURCE_URL = "zuul.data-source.url";
	public static final String DATA_SOURCE_USER = "zuul.data-source.user";
	public static final String DATA_SOURCE_PASSWORD = "zuul.data-source.password";
	public static final String DATA_SOURCE_MIN_POOL_SIZE = "zuul.data-source.min-pool-size";
	public static final String DATA_SOURCE_MAX_POOL_SIZE = "zuul.data-source.max-pool-size";
	public static final String DATA_SOURCE_CONNECT_TIMEOUT = "zuul.data-source.connection-timeout";
	public static final String DATA_SOURCE_IDLE_TIMEOUT = "zuul.data-source.idle-timeout";
	public static final String DATA_SOURCE_MAX_LIFETIME = "zuul.data-source.max-lifetime";

	public static final String FILTER_TABLE_NAME = "zuul.filter.table.name";
	public static final String ZUUL_FILTER_DAO_TYPE = "zuul.filter.dao.type";
	public static final String ZUUL_FILTER_REPO = "zuul.filter.repository";

	public static final String ZUUL_FILTER_ADMIN_ENABLED = "zuul.filter.admin.enabled";

	public static final String ZUUL_FILTER_POLLER_ENABLED = "zuul.filter.poller.enabled";
	public static final String ZUUL_FILTER_POLLER_INTERVAL = "zuul.filter.poller.interval";

	public static final String ZUUL_USE_ACTIVE_FILTERS = "zuul.use.active.filters";
	public static final String ZUUL_USE_CANARY_FILTERS = "zuul.use.canary.filters";

	public static final String ZUUL_FILTER_PRE_PATH = "zuul.filter.pre.path";
	public static final String ZUUL_FILTER_ROUTE_PATH = "zuul.filter.route.path";
	public static final String ZUUL_FILTER_POST_PATH = "zuul.filter.post.path";
	public static final String ZUUL_FILTER_ERROR_PATH = "zuul.filter.error.path";
	public static final String Zuul_FILTER_CUSTOM_PATH = "zuul.filter.custom.path";

	public static final String ZUUL_SERVLET_ASYNC_TIMEOUT = "zuul.servlet.async.timeout";
	public static final String ZUUL_THREADPOOL_CODE_SIZE = "zuul.thread-pool.core-size";
	public static final String ZUUL_THREADPOOL_MAX_SIZE = "zuul.thread-pool.maximum-size";
	public static final String ZUUL_THREADPOOL_ALIVE_TIME = "zuul.thread-pool.alive-time";

	public static final String ZUUL_INITIAL_STREAM_BUFFER_SIZE = "zuul.initial-stream-buffer-size";
	public static final String ZUUL_SET_CONTENT_LENGTH = "zuul.set-content-length";

	public static final String ZUUL_CLIENT_MAX_CONNECTIONS = "zuul.client.max.connections";
	public static final String ZUUL_CLIENT_ROUTE_MAX_CONNECTIONS = "zuul.client.route.max.connections";

	public static final String DEFAULT_GROUP = "default-group";
	public static final String DEFAULT_NAME = "default-name";
//	public static final String DEFAULT_DOMAIN = "default-domain";

//	public static final String ZUUL_ROUTE_POLLER_ENABLED = "zuul.route.poller.enabled";
//	public static final String ZUUL_ROUTE_POLLER_INTERVAL = "zuul.route.poller.interval";
//	public static final String ZUUL_ROUTE_POLLER_URL = "zuul.route.poller.url";
	// constants
	public static final String CAT_CHILD_MESSAGE_ID = "X-CAT-CHILD-ID";
	public static final String CAT_PARENT_MESSAGE_ID = "X-CAT-PARENT-ID";
	public static final String CAT_ROOT_MESSAGE_ID = "X-CAT-ROOT-ID";
	public static final String CAT_S2G_APP = "X-S2G-CAT-APP";
	public static final String HTTP_ERROR_CODE_HEADER = "X-S2G-CODE";
	public static final String HTTP_ERROR_MESSAGE_HEADER = "X-S2G-MESSAGE";
	public static final String HTTP_S2G_DOMAIN = "X-S2G-DOMAIN";
	public static final String HTTP_S2G_SERVICEID = "X-S2G-SERVICE";
	
//	public static final String GAETEWAY_DATA = "GatewayData";
	
	
    public static final String ZUUL_DEBUG_REQUEST = "zuul.debug.request";
    public static final String ZUUL_DEBUG_PARAMETER = "zuul.debug.parameter";
}

