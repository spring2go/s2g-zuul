package io.spring2go.tools.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticServlet extends HttpServlet {

    final static Map<String, String> EXT_TO_MEDIATYPE = new HashMap<String, String>();
    static {
        EXT_TO_MEDIATYPE.put("js", "text/javascript");
        EXT_TO_MEDIATYPE.put("png", "image/png");
        EXT_TO_MEDIATYPE.put("gif", "image/gif");
        EXT_TO_MEDIATYPE.put("css", "text/css");
        EXT_TO_MEDIATYPE.put("jpg", "image/jpeg");
        EXT_TO_MEDIATYPE.put("jpeg", "image/jpeg");
        EXT_TO_MEDIATYPE.put("html", "text/html");
    }

    final static ConcurrentHashMap<String, byte[]> CONTENT_CACHE = new ConcurrentHashMap<String, byte[]>();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        if(path.equals("/")){
            path = "/index.html";
        }
        String ext = path.substring(path.lastIndexOf(".")+1);
        String mediaType = EXT_TO_MEDIATYPE.get(ext);
        byte[] contentBytes = null;

        if(mediaType!=null){
            contentBytes = CONTENT_CACHE.get(path);
            if (contentBytes == null) {
                InputStream is = getClass().getClassLoader().getResourceAsStream("content" + path);
                if (is != null) {
                    try {
                        ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
                        byte[] bs = new byte[4096];
                        int c = 0;
                        while((c = is.read(bs)) > 0){
                            os.write(bs,0,c);
                        }
                        contentBytes=os.toByteArray();
                        CONTENT_CACHE.putIfAbsent(path, contentBytes);
                    } catch (IOException e) {
                        try {
                            is.close();
                        } catch (IOException e1) {
                            logger.warn("Could not close the resource " + path, e1);
                        }
                    }
                }

            }
        }

        if (contentBytes == null) {
            resp.sendError(Response.SC_NOT_FOUND);
        }else {

            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
            resp.setContentType(mediaType);
            resp.setStatus(Response.SC_OK);
            resp.getOutputStream().write(contentBytes);
            resp.getOutputStream().close();
        }
    }
}
