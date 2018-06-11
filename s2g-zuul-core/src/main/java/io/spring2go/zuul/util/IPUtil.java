package io.spring2go.zuul.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class IPUtil {
	private static final String NETWORK_CARD = "eth0";
	private static final String NETWORK_CARD_BAND = "bond0";

	public static String getLocalHostName() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getLinuxLocalIP() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> e1 = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
			while (e1.hasMoreElements()) {
				NetworkInterface ni = e1.nextElement();
				if ((NETWORK_CARD.equals(ni.getName())) || (NETWORK_CARD_BAND.equals(ni.getName()))) {
					Enumeration<InetAddress> e2 = ni.getInetAddresses();
					while (e2.hasMoreElements()) {
						InetAddress ia = e2.nextElement();
						if (ia instanceof Inet6Address) {
							continue;
						}
						ip = ia.getHostAddress();
					}
					break;
				} else {
					continue;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return ip;
	}

	public static String getWinLocalIP() {
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress().toString();
		} finally {
			return ip;
		}
	}

	public static String getLocalIP() {
		String ip = null;
		if (!System.getProperty("os.name").contains("Win")) {
			ip = getLinuxLocalIP();
		} else {

			ip = getWinLocalIP();
		}
		return ip;
	}
	
	public static String getClientIpAddr(HttpServletRequest request) {
	    String ip = request.getHeader("x-forwarded-for");
	    if(StringUtils.isEmpty(ip) ||"unknown".equalsIgnoreCase(ip)) {
	        ip = request.getHeader("Proxy-Client-IP");
	    }
	    if(StringUtils.isEmpty(ip)  ||"unknown".equalsIgnoreCase(ip)) {
	        ip = request.getHeader("WL-Proxy-Client-IP");
	    }
	    if(StringUtils.isEmpty(ip)  ||"unknown".equalsIgnoreCase(ip)) {
	        ip = request.getRemoteAddr();
	    }
	    if(!StringUtils.isEmpty(ip)){
	    	ip=ip.split(",")[0];
	    }
	    return ip;
	 }
}
