package io.spring2go.zuul.hystrix;

import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.http.HttpRequest;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;

public class RibbonRequestCommandForThreadIsolation extends RibbonZuulCommon {

	public RibbonRequestCommandForThreadIsolation(AbstractLoadBalancerAwareClient client,HttpRequest request, String serviceName, String commandGroup,
			String commandKey, String threadPoolKey) {
		super(client,request, serviceName, commandGroup, commandKey,
				Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup))
						.andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
						.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey))
						.andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIsolationStrategy(
								HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)));

	}

	public RibbonRequestCommandForThreadIsolation(AbstractLoadBalancerAwareClient client,HttpRequest request, String serviceName, String commandGroup,
			String commandKey) {
		this(client,request,  serviceName,  commandGroup,
			 commandKey,  ZuulCommandHelper.getThreadPoolKey(commandGroup,commandKey));

	}
}
