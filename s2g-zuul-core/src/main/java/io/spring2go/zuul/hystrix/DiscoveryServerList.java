package io.spring2go.zuul.hystrix;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.shared.Application;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

public class DiscoveryServerList extends AbstractServerList<DiscoveryEnabledServer> {

	private static final Logger logger = LoggerFactory.getLogger(DiscoveryServerList.class);

	String clientName;

	boolean isSecure = false;

	boolean prioritizeVipAddressBasedServers = true;

	String datacenter;
	String targetRegion;

	int overridePort = DefaultClientConfigImpl.DEFAULT_PORT;
	boolean shouldUseOverridePort = false;
	boolean shouldUseIpAddr = false;

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		clientName = clientConfig.getClientName();

		isSecure = Boolean.parseBoolean("" + clientConfig.getProperty(CommonClientConfigKey.IsSecure, "false"));
		prioritizeVipAddressBasedServers = Boolean.parseBoolean("" + clientConfig
				.getProperty(CommonClientConfigKey.PrioritizeVipAddressBasedServers, prioritizeVipAddressBasedServers));
		datacenter = ConfigurationManager.getDeploymentContext().getDeploymentDatacenter();
		targetRegion = (String) clientConfig.getProperty(CommonClientConfigKey.TargetRegion);

		shouldUseIpAddr = clientConfig.getPropertyAsBoolean(CommonClientConfigKey.UseIPAddrForServer,
				DefaultClientConfigImpl.DEFAULT_USEIPADDRESS_FOR_SERVER);

		// override client configuration and use client-defined port
		if (clientConfig.getPropertyAsBoolean(CommonClientConfigKey.ForceClientPortConfiguration, false)) {

			if (isSecure) {

				if (clientConfig.containsProperty(CommonClientConfigKey.SecurePort)) {

					overridePort = clientConfig.getPropertyAsInteger(CommonClientConfigKey.SecurePort,
							DefaultClientConfigImpl.DEFAULT_PORT);
					shouldUseOverridePort = true;

				} else {
					logger.warn(clientName + " set to force client port but no secure port is set, so ignoring");
				}
			} else {

				if (clientConfig.containsProperty(CommonClientConfigKey.Port)) {

					overridePort = clientConfig.getPropertyAsInteger(CommonClientConfigKey.Port,
							DefaultClientConfigImpl.DEFAULT_PORT);
					shouldUseOverridePort = true;

				} else {
					logger.warn(clientName + " set to force client port but no port is set, so ignoring");
				}
			}
		}

	}

	@Override
	public List<DiscoveryEnabledServer> getInitialListOfServers() {
		return obtainServersViaDiscovery();
	}

	@Override
	public List<DiscoveryEnabledServer> getUpdatedListOfServers() {
		return obtainServersViaDiscovery();
	}

	private List<DiscoveryEnabledServer> obtainServersViaDiscovery() {
		List<DiscoveryEnabledServer> serverList = new ArrayList<DiscoveryEnabledServer>();

		DiscoveryClient discoveryClient = DiscoveryManager.getInstance().getDiscoveryClient();
		if (discoveryClient == null) {
			return new ArrayList<DiscoveryEnabledServer>();
		}
		// if (vipAddresses != null) {
		// for (String vipAddress : vipAddresses.split(",")) {
		// // if targetRegion is null, it will be interpreted as the same
		// // region of client
		// List<InstanceInfo> listOfinstanceInfo =
		// discoveryClient.getInstancesByVipAddress(vipAddress, isSecure,
		// targetRegion);
		// for (InstanceInfo ii : listOfinstanceInfo) {
		// if (ii.getStatus().equals(InstanceStatus.UP)) {
		//
		// if (shouldUseOverridePort) {
		// if (logger.isDebugEnabled()) {
		// logger.debug("Overriding port on client name: " + clientName + " to "
		// + overridePort);
		// }
		//
		// // copy is necessary since the InstanceInfo builder
		// // just uses the original reference,
		// // and we don't want to corrupt the global eureka
		// // copy of the object which may be
		// // used by other clients in our system
		// InstanceInfo copy = new InstanceInfo(ii);
		//
		// if (isSecure) {
		// ii = new
		// InstanceInfo.Builder(copy).setSecurePort(overridePort).build();
		// } else {
		// ii = new InstanceInfo.Builder(copy).setPort(overridePort).build();
		// }
		// }
		//
		// DiscoveryEnabledServer des = new DiscoveryEnabledServer(ii, isSecure,
		// shouldUseIpAddr);
		// des.setZone(DiscoveryClient.getZone(ii));
		// serverList.add(des);
		// }
		// }
		// if (serverList.size() > 0 && prioritizeVipAddressBasedServers) {
		// break; // if the current vipAddress has servers, we dont use
		// // subsequent vipAddress based servers
		// }
		// }
		// }

		Application application = discoveryClient.getApplication(clientName);

		if (application == null) {
			logger.error(clientName + "服务未找到");
		} else {
			for (InstanceInfo ii : application.getInstances()) {
				if (ii.getStatus() == InstanceStatus.UP) {
					if (shouldUseOverridePort) {
						if (logger.isDebugEnabled()) {
							logger.debug("Overriding port on client name: " + clientName + " to " + overridePort);
						}

						// copy is necessary since the InstanceInfo builder
						// just uses the original reference,
						// and we don't want to corrupt the global eureka
						// copy of the object which may be
						// used by other clients in our system
						InstanceInfo copy = new InstanceInfo(ii);

						if (isSecure) {
							ii = new InstanceInfo.Builder(copy).setSecurePort(overridePort).build();
						} else {
							ii = new InstanceInfo.Builder(copy).setPort(overridePort).build();
						}
					}

					DiscoveryEnabledServer des = new DiscoveryEnabledServer(ii, isSecure, shouldUseIpAddr);
					des.setZone(DiscoveryClient.getZone(ii));
					serverList.add(des);
				}
			}
		}

		return serverList;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("DiscoveryEnabledNIWSServerList:");
		sb.append("; clientName:").append(clientName);
		sb.append("; isSecure:").append(isSecure);
		sb.append("; datacenter:").append(datacenter);
		return sb.toString();
	}

}
