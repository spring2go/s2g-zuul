package io.spring2go.zuul.common;

public class Constants {

	// config items
	public static final String ApplicationName = "zuul-gateway";
	public static final String DeploymentApplicationID = "archaius.deployment.applicationId";
	public static final String DeployConfigUrl = "archaius.configurationSource.additionalUrls";
	public static final String DeployEnvironment = "archaius.deployment.environment";

	public static final String DataSourceClassName = "zuul.data-source.class-name";
	public static final String DataSourceUrl = "zuul.data-source.url";
	public static final String DataSourceUser = "zuul.data-source.user";
	public static final String DataSourcePasswd = "zuul.data-source.password";
	public static final String DataSourceMinPoolSize = "zuul.data-source.min-pool-size";
	public static final String DataSourceMaxPoolSize = "zuul.data-source.max-pool-size";
	public static final String DataSourceConnectTimeOut = "zuul.data-source.connection-timeout";
	public static final String DataSourceIdleTimeOut = "zuul.data-source.idle-timeout";
	public static final String DataSourceMaxLifeTime = "zuul.data-source.max-lifetime";

	public static final String FilterTableName = "zuul.filter.table.name";
	public static final String ZuulFilterDaoType = "zuul.filter.dao.type";
	public static final String ZuulFilterRepo = "zuul.filter.repository";

	public static final String ZuulFilterAdminEnabled = "zuul.filter.admin.enabled";

	public static final String ZuulFilterPollerEnabled = "zuul.filter.poller.enabled";
	public static final String ZuulFilterPollerInterval = "zuul.filter.poller.interval";

	public static final String ZuulUseActiveFilters = "zuul.use.active.filters";
	public static final String ZuulUseCanaryFilters = "zuul.use.canary.filters";

	public static final String ZuulFilterPrePath = "zuul.filter.pre.path";
	public static final String ZuulFilterRoutePath = "zuul.filter.route.path";
	public static final String ZuulFilterPostPath = "zuul.filter.post.path";
	public static final String ZuulFilterErrorPath = "zuul.filter.error.path";
	public static final String ZuulFilterCustomPath = "zuul.filter.custom.path";

	public static final String ZuulServletAsyncTimeOut = "zuul.servlet.async.timeout";
	public static final String ZuulThreadPoolCodeSize = "zuul.thread-pool.core-size";
	public static final String ZuulThreadPoolMaxSize = "zuul.thread-pool.maximum-size";
	public static final String ZuulThreadPoolAliveTime = "zuul.thread-pool.alive-time";

	public static final String ZuulInitialStreamBufferSize = "zuul.initial-stream-buffer-size";
	public static final String ZuulSetContentLength = "zuul.set-content-length";

	public static final String ZuulClientMaxConnections = "zuul.client.max.connections";
	public static final String ZuulClientRouteMaxConnections = "zuul.client.route.max.connections";

	public static final String DefaultGroup = "default-group";
	public static final String DefaultName = "default-name";
	public static final String DefaultDomain = "default-domain";

	public static final String ZuulRoutePollerEnabled = "zuul.route.poller.enabled";
	public static final String ZuulRoutePollerInterval = "zuul.route.poller.interval";
	public static final String ZuulRoutePollerUrl = "zuul.route.poller.url";
	// constants
	public static final String CAT_CHILD_MESSAGE_ID = "X-CAT-CHILD-ID";
	public static final String CAT_PARENT_MESSAGE_ID = "X-CAT-PARENT-ID";
	public static final String CAT_ROOT_MESSAGE_ID = "X-CAT-ROOT-ID";
	public static final String CAT_S2G_APP = "X-S2G-CAT-APP";
	public static final String HTTP_ERROR_CODE_HEADER = "X-S2G-CODE";
	public static final String HTTP_ERROR_MESSAGE_HEADER = "X-S2G-MESSAGE";
	public static final String HTTP_S2G_DOMAIN = "X-S2G-DOMAIN";
	public static final String HTTP_S2G_SERVICEID = "X-S2G-SERVICE";
	
	public static final String GatewayData = "GatewayData";
	
	
    public static final String ZuulDebugRequest = "zuul.debug.request";
    public static final String ZuulDebugParameter = "zuul.debug.parameter";
}

