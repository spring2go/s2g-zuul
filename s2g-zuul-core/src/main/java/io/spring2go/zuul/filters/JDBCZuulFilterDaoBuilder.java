package io.spring2go.zuul.filters;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import io.spring2go.zuul.common.Constants;
import io.spring2go.zuul.common.IZuulFilterDao;
import io.spring2go.zuul.common.IZuulFilterDaoBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class JDBCZuulFilterDaoBuilder implements IZuulFilterDaoBuilder {
	private static final DynamicStringProperty dataSourceClass = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DataSourceClassName, null);
	private static final DynamicStringProperty url = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DataSourceUrl, null);
	private static final DynamicStringProperty user = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DataSourceUser, null);
	private static final DynamicStringProperty password = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DataSourcePasswd, null);
	private static final DynamicIntProperty minPoolSize = DynamicPropertyFactory.getInstance()
			.getIntProperty(Constants.DataSourceMinPoolSize, 10);
	private static final DynamicIntProperty maxPoolSize = DynamicPropertyFactory.getInstance()
			.getIntProperty(Constants.DataSourceMaxPoolSize, 20);
	private static final DynamicLongProperty connectionTimeout = DynamicPropertyFactory.getInstance()
			.getLongProperty(Constants.DataSourceConnectTimeOut, 1000);
	private static final DynamicLongProperty idleTimeout = DynamicPropertyFactory.getInstance()
			.getLongProperty(Constants.DataSourceIdleTimeOut, 600000);
	private static final DynamicLongProperty maxLifetime = DynamicPropertyFactory.getInstance()
			.getLongProperty(Constants.DataSourceMaxLifeTime, 1800000);

	private static final DynamicStringProperty environment = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DeployEnvironment, "test");

	private static final DynamicStringProperty filterTableName = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.FilterTableName, null);
	
	private static final DynamicStringProperty appName = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DeploymentApplicationID, Constants.ApplicationName);

	private HikariDataSource dataSource;
	private String filterTable;

	public JDBCZuulFilterDaoBuilder() {
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName(dataSourceClass.get());
		config.addDataSourceProperty("url", url.get());
		config.addDataSourceProperty("user", user.get());
		config.addDataSourceProperty("password", password.get());

		config.setMinimumPoolSize(minPoolSize.get());
		config.setMaximumPoolSize(maxPoolSize.get());
		config.setConnectionTimeout(connectionTimeout.get());
		config.setIdleTimeout(idleTimeout.get());
		config.setMaxLifetime(maxLifetime.get());

		this.dataSource = new HikariDataSource(config);
		this.filterTable = filterTableName.get() + "_" + environment.get();
	}

	@Override
	public IZuulFilterDao build() {
        return new HystrixZuulFilterDao(new JDBCZuulFilterDao(filterTable, dataSource, appName.get()));

	}

}
