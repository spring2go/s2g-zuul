package io.spring2go.zuul.hystrix;

import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.http.HttpRequest;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class RibbonRequestCommandForSemaphoreIsolation extends RibbonZuulCommon {
	


	public RibbonRequestCommandForSemaphoreIsolation(AbstractLoadBalancerAwareClient client,HttpRequest request, String serviceName, String commandGroup,
			String commandKey) {
		super(client,request, serviceName, commandGroup, commandKey,
				Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup))
						.andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
						.andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIsolationStrategy(
								HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
	}

}