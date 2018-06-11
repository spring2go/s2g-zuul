package io.spring2go.zuul.hystrix;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.ClientException;
import com.netflix.client.ClientFactory;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import io.spring2go.zuul.util.SleepUtil;

public class RestClientFactory {
	private static Logger logger = LoggerFactory.getLogger(RestClientFactory.class);
	
	private static final ConcurrentMap<String ,RestClient> restClientMap = Maps.newConcurrentMap();
	
	public static  RestClient getRestClient(String serviceName,IClientConfig clientConfig) throws ClientException{
		RestClient oldClient = restClientMap.get(serviceName);
		
		if(oldClient == null){			
			synchronized (RestClient.class) {
				oldClient = restClientMap.get(serviceName);
				if(oldClient == null){
					RestClient client =  newRestClient(serviceName,clientConfig);
					oldClient = restClientMap.putIfAbsent(serviceName, client);
					
					if(oldClient != null){
						oldClient.shutdown();
					}					
					oldClient = client;
				}				
			}			
		}
		
		return oldClient;
	}
	
	public static void closeRestClient(String serviceName){
		RestClient oldClient = restClientMap.remove(serviceName);
		if(oldClient != null){
			SleepUtil.sleep(30*1000);
			oldClient.shutdown();
		}
	}
	
	
	private static RestClient newRestClient(String restClientName,IClientConfig clientConfig) throws ClientException{
		RestClient restClient = new RestClient(clientConfig);
		ILoadBalancer loadBalancer = null;
		boolean initializeNFLoadBalancer = Boolean.parseBoolean(clientConfig.getProperty(
				CommonClientConfigKey.InitializeNFLoadBalancer, DefaultClientConfigImpl.DEFAULT_ENABLE_LOADBALANCER).toString());
		if (initializeNFLoadBalancer) {
			loadBalancer  = newLoadBalancerFromConfig(restClientName, clientConfig);
		}
		if (restClient instanceof AbstractLoadBalancerAwareClient) {
			((AbstractLoadBalancerAwareClient) restClient).setLoadBalancer(loadBalancer);
		}
		return restClient;
	}
	
	private static ILoadBalancer newLoadBalancerFromConfig(String restClientName,IClientConfig clientConfig) throws ClientException {
    	ILoadBalancer lb = null;
        try {
            String loadBalancerClassName = (String) clientConfig.getProperty(CommonClientConfigKey.NFLoadBalancerClassName);
            lb = (ILoadBalancer) ClientFactory.instantiateInstanceWithClientConfig(loadBalancerClassName, clientConfig);                                    
                      
            logger.info("Client:" + restClientName  + " instantiated a LoadBalancer:" + lb.toString());
            return lb;
        } catch (Exception e) {           
           throw new ClientException("Unable to instantiate/associate LoadBalancer with Client:" + restClientName, e);
        }  
	}
	
}

