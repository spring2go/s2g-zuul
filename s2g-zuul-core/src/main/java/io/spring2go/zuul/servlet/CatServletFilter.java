package io.spring2go.zuul.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import io.spring2go.zuul.common.Constants;

public class CatServletFilter implements Filter {

    private String[] urlPatterns = new String[0];

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String patterns = filterConfig.getInitParameter("CatHttpModuleUrlPatterns");
        if (patterns != null) {
            patterns = patterns.trim();
            urlPatterns = patterns.split(",");
            for (int i = 0; i < urlPatterns.length; i++) {
                urlPatterns[i] = urlPatterns[i].trim();
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String url = request.getRequestURL().toString();
        for (String urlPattern : urlPatterns) {
            if (url.startsWith(urlPattern)) {
                url = urlPattern;
            }
        }

       Transaction t = Cat.newTransaction("Service", url);

        try {

            PropertyContext propertyContext = new PropertyContext();
            propertyContext.addProperty(Cat.Context.ROOT, request.getHeader(Constants.CAT_ROOT_MESSAGE_ID));
            propertyContext.addProperty(Cat.Context.PARENT, request.getHeader(Constants.CAT_PARENT_MESSAGE_ID));
            propertyContext.addProperty(Cat.Context.CHILD, request.getHeader(Constants.CAT_CHILD_MESSAGE_ID));
            Cat.logRemoteCallServer(propertyContext);

            Cat.logEvent("Service.method", request.getMethod(), Message.SUCCESS, request.getRequestURL().toString());
            Cat.logEvent("Service.client", request.getRemoteHost());
            // for cross report
            String clientApp = request.getHeader("X-S2G-CAT-APP");
            Cat.logEvent("Service.app", clientApp == null ? "unknown" : clientApp);

            filterChain.doFilter(servletRequest, servletResponse);

            t.setStatus(Transaction.SUCCESS);
        } catch (Exception ex) {
            t.setStatus(ex);
            Cat.logError(ex);
            throw ex;
        } finally {
            t.complete();
        }
    }

    @Override
    public void destroy() {

    }

    class PropertyContext implements Cat.Context {

        private Map<String, String> properties = new HashMap<>();

        @Override
        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return properties.get(key);
        }
    }
}

