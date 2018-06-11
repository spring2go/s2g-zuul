package io.spring2go.zuul.hystrix;

import com.netflix.config.DynamicPropertyFactory;

public class ZuulCommandHelper {
    public static int getSemaphoreMaxConcurrent(String commandGroup, String commandKey) {
        int maxConcurrent = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".semaphore.max", 0).get();
        if (maxConcurrent == 0) {
            maxConcurrent = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".semaphore.max", 0).get();
        }
        if (maxConcurrent == 0) {
            maxConcurrent = DynamicPropertyFactory.getInstance().getIntProperty("zuul.semaphore.max.global", 100).get();
        }
        return maxConcurrent;
    }

    public static String getThreadPoolKey(String commandGroup, String commandKey) {
        String poolKey = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".thread-pool.key", null).get();
        if (poolKey == null) {
            poolKey = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".thread-pool.key", commandGroup).get();
        }
        return poolKey;
    }

    public static int getThreadTimeout(String commandGroup, String commandKey) {
        int timeout = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".thread.timeout", 0).get();
        if (timeout == 0) {
            timeout = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".thread.timeout", 0).get();
        }
        if (timeout == 0) {
            timeout = DynamicPropertyFactory.getInstance().getIntProperty("zuul.thread.timeout.global", 1500).get();
        }
        return timeout;
    }
    


    public static boolean getCircuitBreakerEnabled(String commandGroup, String commandKey) {
        String bool = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".circuit-breaker.enabled", null).get();
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".circuit-breaker.enabled", null).get();
        }
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty("zuul.circuit-breaker.enabled.global", "true").get();
        }
        return bool.toString().equalsIgnoreCase("true");
    }

    public static boolean getCircuitBreakerForceOpen(String commandGroup, String commandKey) {
        String bool = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".circuit-breaker.force-open", null).get();
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".circuit-breaker.force-open", null).get();
        }
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty("zuul.circuit-breaker.force-open.global", "false").get();
        }
        return bool.toString().equalsIgnoreCase("true");
    }

    public static boolean getCircuitBreakerForceClosed(String commandGroup, String commandKey) {
        String bool = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".circuit-breaker.force-closed", null).get();
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".circuit-breaker.force-closed", null).get();
        }
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty("zuul.circuit-breaker.force-closed.global", "false").get();
        }
        return bool.toString().equalsIgnoreCase("true");
    }

    public static int getCircuitBreakerRequestThreshold(String commandGroup, String commandKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".circuit-breaker.request-threshold", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".circuit-breaker.request-threshold", 0).get();
        }
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("zuul.circuit-breaker.request-threshold.global", 10).get();
        }
        return i;
    }

    public static int getCircuitBreakerErrorThreshold(String commandGroup, String commandKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".circuit-breaker.error-percentage", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".circuit-breaker.error-percentage", 0).get();
        }
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("zuul.circuit-breaker.error-percentage.global", 30).get();
        }
        return i;
    }

    public static int getCircuitBreakerSleep(String commandGroup, String commandKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".circuit-breaker.sleep", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".circuit-breaker.sleep", 0).get();
        }
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("zuul.circuit-breaker.sleep.global", 10000).get();
        }
        return i;
    }

    public static int getThreadPoolSize(String threadPoolKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(threadPoolKey + ".hystrix.thread-pool.size", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("zuul.hystrix.thread-pool.size.global", 10).get();
        }
        return i;
    }
    
    public static int getRibbonMaxHttpConnectionsPerHost(String serviceName){
        int i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon."+serviceName + ".hystrix.maxconnections.perhost", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon.hystrix.maxconnections.perhost.global", 500).get();
        }
        return i;
    }
    
    public static int  getRibbonMaxTotalHttpConnections(String serviceName){
        int i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon."+serviceName + ".hystrix.maxconnections", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon.hystrix.maxconnections.global", 2000).get();
        }
        return i;
    }
    
    public static int  getRibbonMaxAutoRetries(String serviceName){
        int i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon."+serviceName + ".hystrix.maxautoretries", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon.global.hystrix.maxautoretries.global", 1).get();
        }
        return i;
    }
    
    public static int getRibbonMaxAutoRetriesNextServer(String serviceName){
        int i = DynamicPropertyFactory.getInstance().getIntProperty(serviceName + ".ribbon.hystrix.maxautoretries.nextserver", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("ribbon.hystrix.maxautoretries.nextserver.global", 1).get();
        }
        return i;
    }
    
    public static String getRibbonLoadBalanceRule(String groupName, String routeName) {
        String rule = DynamicPropertyFactory.getInstance().getStringProperty("ribbon."+routeName + ".lb.rule", null).get();
        if (rule == null) {
        	rule = DynamicPropertyFactory.getInstance().getStringProperty("ribbon."+groupName + ".lb.rule", null).get();
        }
        if (rule == null) {
        	rule = DynamicPropertyFactory.getInstance().getStringProperty("ribbon.lb.rule.global", "com.netflix.loadbalancer.RetryRule").get();
        }
        return rule;
    }
    
    public static int getRibbonConnectTimeout(String groupName, String routeName) {
		int connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("ribbon."+routeName + ".connect.timeout", 0)
				.get();
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("ribbon."+groupName + ".connect.timeout", 0)
					.get();
		}
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("zuul.connect.timeout.global", 20000)
					.get();
		}
		return connectTimeout;
    }
    
    public static int  getRibbonReadTimeout(String groupName, String routeName) {
		int socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty("ribbbon."+routeName + ".socket.timeout", 0).get();
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty("ribbbon."+groupName + ".socket.timeout", 0).get();
		}
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty("zuul.socket.timeout.global", 10000)
					.get();
		}
		return socketTimeout;
    }
    
    public static boolean getRibbonRequestSpecificRetryOn(String groupName, String routeName) {
		boolean nextTry = DynamicPropertyFactory.getInstance().getBooleanProperty("ribbbon."+routeName + ".next.try", false).get();
		if (!nextTry) {
			nextTry = DynamicPropertyFactory.getInstance().getBooleanProperty("ribbbon."+groupName + ".next.try", false).get();
		}
		if (!nextTry) {
			nextTry = DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.socket.next.try.global", true).get();
		}
		return nextTry;
    }
    
    public static boolean getRibbonOkToRetryOnAllOperations(String groupName, String routeName) {
    	boolean sameTry = DynamicPropertyFactory.getInstance().getBooleanProperty("ribbbon."+routeName + ".same.try", false).get();
		if (!sameTry) {
			sameTry = DynamicPropertyFactory.getInstance().getBooleanProperty("ribbbon."+groupName + ".same.try", false).get();
		}
		if (!sameTry) {
			sameTry = DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.socket.same.try.global", true).get();
		}
		return sameTry;
    }
}