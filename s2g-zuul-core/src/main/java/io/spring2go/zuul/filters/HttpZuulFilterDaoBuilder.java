package io.spring2go.zuul.filters;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import io.spring2go.zuul.common.Constants;
import io.spring2go.zuul.common.IZuulFilterDao;
import io.spring2go.zuul.common.IZuulFilterDaoBuilder;

public class HttpZuulFilterDaoBuilder implements IZuulFilterDaoBuilder {

	private static final DynamicStringProperty appName = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.DeploymentApplicationID, Constants.ApplicationName);

	public HttpZuulFilterDaoBuilder() {

	}

	@Override
	public IZuulFilterDao build() {
		return new HttpZuulFilterDao(appName.get());

	}

}
