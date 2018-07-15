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

public class EnvServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // make a writable copy of the immutable System.getenv() map
        Map<String,String> envVarsMap = new TreeMap<String,String>(System.getenv());

        String jsonStr = JSON.toJSONString(envVarsMap);

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
