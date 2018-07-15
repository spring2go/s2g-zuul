package io.spring2go.tools.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.AbstractConfiguration;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Response;

import com.alibaba.fastjson.JSON;
import com.netflix.config.ConfigurationManager;

public class PropsServlet extends HttpServlet {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> allPropsAsString = new TreeMap<String, String>();
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        Iterator<String> keys = config.getKeys();

        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value;
            value = config.getProperty(key);
            allPropsAsString.put(key, value.toString());
        }

        String jsonStr = JSON.toJSONString(allPropsAsString);
        		//mapper.writeValueAsString(allPropsAsString);

        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
        resp.setContentType("application/json; charset=UTF-8");

        PrintWriter writer = resp.getWriter();
        try{
            writer.write(jsonStr);
            resp.setStatus(Response.SC_OK);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}