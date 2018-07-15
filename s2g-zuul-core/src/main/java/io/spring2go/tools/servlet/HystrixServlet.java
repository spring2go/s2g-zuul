package io.spring2go.tools.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Response;

import com.alibaba.fastjson.JSON;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;

public class HystrixServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		try {

			// make a writable copy of the immutable System.getenv() map
			Map<String, String> breakerMap = new TreeMap<String, String>();

			for (HystrixCommandMetrics commandMetrics : HystrixCommandMetrics.getInstances()) {
				HystrixCommandKey key = commandMetrics.getCommandKey();
				HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(key);

				if (circuitBreaker != null) {
					if (circuitBreaker.isOpen()) {
						breakerMap.put(key.name(), DynamicPropertyFactory.getInstance().getStringProperty("mail."+key.name(), "bobo@spring2go.com").get());
					}else{
						if(DynamicPropertyFactory.getInstance().getBooleanProperty("hystrix.command."+key.name()+".circuitBreaker.forceOpen",false).get()){
							breakerMap.put(key.name(), DynamicPropertyFactory.getInstance().getStringProperty("mail."+key.name(), "bobo@spring2go.com").get());
						}else if(DynamicPropertyFactory.getInstance().getBooleanProperty("hystrix.command.default.circuitBreaker.forceOpen",false).get()){
							breakerMap.put(key.name(), DynamicPropertyFactory.getInstance().getStringProperty("mail."+key.name(), "bobo@spring2go.com").get());							
						}
					}
				}
			}

			String jsonStr = JSON.toJSONString(breakerMap);

			resp.addHeader("Access-Control-Allow-Origin", "*");
			resp.addHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
			resp.setContentType("application/json; charset=UTF-8");

			writer.write(jsonStr);
			resp.setStatus(Response.SC_OK);
		}catch(Throwable t){
			writer.write(t.getMessage());
			resp.setStatus(Response.SC_INTERNAL_SERVER_ERROR);
		}finally {
		
			if (writer != null) {
				writer.close();
			}
		}
	}
}
