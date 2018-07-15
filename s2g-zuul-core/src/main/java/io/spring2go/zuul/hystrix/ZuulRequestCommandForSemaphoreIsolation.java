package io.spring2go.zuul.hystrix;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import io.spring2go.zuul.common.CatContext;
import io.spring2go.zuul.common.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class ZuulRequestCommandForSemaphoreIsolation extends HystrixCommand<HttpResponse> {

    HttpClient httpclient;
    HttpUriRequest httpUriRequest;
    HttpContext httpContext;

    public ZuulRequestCommandForSemaphoreIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, String commandGroup, String commandKey) {
        this(httpclient, httpUriRequest, null, commandGroup, commandKey);
    }

    public ZuulRequestCommandForSemaphoreIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, HttpContext httpContext, String commandGroup, String commandKey) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(
                        // we want to default to semaphore-isolation since this wraps
                        // 2 others commands that are already thread isolated
                        HystrixCommandProperties.Setter()
                                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                ));

        this.httpclient = httpclient;
        this.httpUriRequest = httpUriRequest;
        this.httpContext = httpContext;
    }

    @Override
    protected HttpResponse run() throws Exception {
    	
    	Transaction t = Cat.newTransaction(CatConstants.TYPE_REMOTE_CALL, httpUriRequest.getURI().toString());
    	
        try {
        	HttpResponse response = forward();
            t.setStatus(Transaction.SUCCESS);
            return response;
        } catch (IOException e) {
            t.setStatus(e);
            Cat.logError(e);
        	throw e;
        } finally {
        	t.complete();
        }
    }

    HttpResponse forward() throws IOException {
    	
    	Context ctx = new CatContext();
    	Cat.logRemoteCallClient(ctx);
    	httpUriRequest.addHeader(Constants.CAT_ROOT_MESSAGE_ID, ctx.getProperty(Cat.Context.ROOT));
    	httpUriRequest.addHeader(Constants.CAT_PARENT_MESSAGE_ID, ctx.getProperty(Cat.Context.PARENT));
    	httpUriRequest.addHeader(Constants.CAT_CHILD_MESSAGE_ID, ctx.getProperty(Cat.Context.CHILD));
        return httpclient.execute(httpUriRequest, httpContext);
    }
}
