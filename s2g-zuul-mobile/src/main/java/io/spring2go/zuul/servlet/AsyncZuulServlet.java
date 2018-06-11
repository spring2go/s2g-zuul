package io.spring2go.zuul.servlet;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.Transaction;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import io.spring2go.zuul.common.CatContext;
import io.spring2go.zuul.common.Constants;
import io.spring2go.zuul.core.ZuulCallable;
import io.spring2go.zuul.core.ZuulRunner;

public class AsyncZuulServlet extends HttpServlet {
	private static final long serialVersionUID = 2723461074152665339L;

	private static Logger LOGGER = LoggerFactory.getLogger(AsyncZuulServlet.class);
	
	private DynamicIntProperty asyncTimeout = DynamicPropertyFactory.getInstance().getIntProperty(Constants.ZuulServletAsyncTimeOut, 20000);
	private DynamicIntProperty coreSize = DynamicPropertyFactory.getInstance().getIntProperty(Constants.ZuulThreadPoolCodeSize, 200);
	private DynamicIntProperty maximumSize = DynamicPropertyFactory.getInstance().getIntProperty(Constants.ZuulThreadPoolMaxSize, 2000);
	private DynamicLongProperty aliveTime = DynamicPropertyFactory.getInstance().getLongProperty(Constants.ZuulThreadPoolAliveTime, 1000 * 60 * 5);
	
	private ZuulRunner zuulRunner = new ZuulRunner();
	private AtomicReference<ThreadPoolExecutor> poolExecutorRef = new AtomicReference<ThreadPoolExecutor>();	    
	private AtomicLong rejectedRequests = new AtomicLong(0);
    @Override
    public void init() throws ServletException {
    	reNewThreadPool();
        Runnable c = new Runnable() {
            @Override
            public void run() {
                ThreadPoolExecutor p = poolExecutorRef.get();
                p.setCorePoolSize(coreSize.get());
                p.setMaximumPoolSize(maximumSize.get());
                p.setKeepAliveTime(aliveTime.get(),TimeUnit.MILLISECONDS);
            }
        };
        
        coreSize.addCallback(c);
        maximumSize.addCallback(c);
        aliveTime.addCallback(c);
        
        // TODO metrics reporting
    }
    
    private void reNewThreadPool() {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(coreSize.get(), maximumSize.get(), aliveTime.get(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        ThreadPoolExecutor old = poolExecutorRef.getAndSet(poolExecutor);
        if (old != null) {
            shutdownPoolExecutor(old);
        }
    }
    
    private void shutdownPoolExecutor(ThreadPoolExecutor old) {
        try {
            old.awaitTermination(5, TimeUnit.MINUTES);
            old.shutdown();
        } catch (InterruptedException e) {
            old.shutdownNow();
            LOGGER.error("Shutdown Zuul Thread Pool:", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	Transaction tran = Cat.getProducer().newTransaction("AsyncZuulServlet", req.getRequestURL().toString());
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(asyncTimeout.get());
        asyncContext.addListener(new AsyncZuulListener());
        try {
        	Context ctx = new CatContext();
        	Cat.logRemoteCallClient(ctx);
            poolExecutorRef.get().submit(new ZuulCallable(ctx,asyncContext, zuulRunner,req));            
            tran.setStatus(Transaction.SUCCESS);
        } catch (RuntimeException e) {
            Cat.logError(e);
            tran.setStatus(e);
            rejectedRequests.incrementAndGet();
            throw e;
        }finally{
        	tran.complete();
        }
    }
    

    @Override
    public void destroy() {
        shutdownPoolExecutor(poolExecutorRef.get());
    }
}
