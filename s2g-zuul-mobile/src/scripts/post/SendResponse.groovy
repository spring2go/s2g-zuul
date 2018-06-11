package scripts.post

import java.util.zip.GZIPInputStream

import javax.servlet.http.HttpServletResponse

import com.dianping.cat.Cat
import com.dianping.cat.message.Transaction
import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.util.Pair
import io.spring2go.zuul.common.*
import io.spring2go.zuul.context.RequestContext
import io.spring2go.zuul.filters.ZuulFilter

public class SendResponse extends ZuulFilter {
	private static final DynamicIntProperty BUFFER_SIZE = DynamicPropertyFactory.getInstance().getIntProperty(Constants.ZuulInitialStreamBufferSize, 1024);

    private static final DynamicBooleanProperty CONTENT_LENGTH = DynamicPropertyFactory.getInstance().getBooleanProperty(Constants.ZuulSetContentLength, false);
	
	@Override
	public String filterType() {
		return 'post';
	}

	@Override
	public int filterOrder() {
		return 100;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext context = RequestContext.getCurrentContext();
		return context.getResponseBody() != null || context.getResponseDataStream() != null;
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.currentContext;
		Transaction tran = Cat.getProducer().newTransaction("SendResponseFilter", context.getRequest().getRequestURL().toString());
		
		HttpServletResponse servletResponse = context.getResponse();
		OutputStream outStream = servletResponse.getOutputStream();
		String responseBody = context.getResponseBody();
		InputStream responseDataStream = context.getResponseDataStream();
		
		InputStream inStream = null;
		try{
			tran.setStatus(Transaction.SUCCESS);
			addResponseHeaders()
			if(responseBody != null){							
				inStream = new ByteArrayInputStream(responseBody.bytes);
				writeResponse(inStream, outStream);
				return;
			}else{
				inStream = responseDataStream;
			}
						
			if (!context.getResponseGZipped()) {
				writeResponse(inStream, outStream)
				return;
			}
			// if origin response is gzipped, and client has not requested gzip, decompress stream
            // before sending to client
            // else, stream gzip directly to client			
			boolean isGZipRequested = false;
			final String requestEncoding = context.getRequest().getHeader(ZuulHeaders.ACCEPT_ENCODING)
			if (requestEncoding != null && requestEncoding.contains("gzip")) {
				isGZipRequested = true;
			}
			if (isGZipRequested) {
				servletResponse.setHeader(ZuulHeaders.CONTENT_ENCODING, "gzip")
			} else {
				try {
					inStream = new GZIPInputStream(responseDataStream);
				} catch (java.util.zip.ZipException e) {
					Cat.logError("gzip expected but not received assuming unencoded response" + RequestContext.currentContext.getRequest().getRequestURL().toString(),e);
				}
			}
			writeResponse(inStream, outStream);
			
		}catch(Throwable t){
			tran.setStatus(t);
			throw t;
		}finally{
			try {
				if (inStream != null) {
					inStream.close();
				}
				if (outStream != null) {
					outStream.flush();
					outStream.close();
				}
			} catch (IOException e) {
			}
			tran.complete();
		}
	}
	
	private void addResponseHeaders() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletResponse servletResponse = context.getResponse();
		List<Pair<String, String>> zuulResponseHeaders = context.getZuulResponseHeaders();

		zuulResponseHeaders?.each { Pair<String, String> it ->
			servletResponse.addHeader(it.first(), it.second())
		}

		RequestContext ctx = RequestContext.getCurrentContext()
		Integer contentLength = ctx.getOriginContentLength()

		// only inserts Content-Length if origin provides it and origin response is not gzipped
		if (CONTENT_LENGTH.get()){
			if (contentLength != null && !ctx.getResponseGZipped())
				servletResponse.setContentLength(contentLength)
		}
	}
	
	private void writeResponse(InputStream zin, OutputStream out) {
		long start = System.currentTimeMillis();
		
		long readCost = 0; // store the cost for reading data from server
		long writeCost = 0; // store the cost for sending data to client
		
		long begin = 0;
		long end = 0;
		try {
			byte[] bytes = new byte[BUFFER_SIZE.get()];
			int bytesRead = -1;
			
			begin = System.currentTimeMillis()
			while ((bytesRead = zin.read(bytes)) != -1) {
				end = System.currentTimeMillis()
				readCost += (end-begin)

				begin = end
				try {
					out.write(bytes, 0, bytesRead);
					out.flush();
				} catch (IOException e) {
					Cat.logError(e)
				} finally {
					end = System.currentTimeMillis()
					writeCost += (end-begin)
				}

				// doubles buffer size if previous read filled it
				if (bytesRead == bytes.length) {
					bytes = new byte[bytes.length * 2]
				}

				begin = end
			}
		}finally{
			RequestContext.getCurrentContext().set("sendResponseCost", System.currentTimeMillis() - start)
			RequestContext.getCurrentContext().set("sendResponseCost:read", readCost)
			RequestContext.getCurrentContext().set("sendResponseCost:write", writeCost)
		}
	}
}