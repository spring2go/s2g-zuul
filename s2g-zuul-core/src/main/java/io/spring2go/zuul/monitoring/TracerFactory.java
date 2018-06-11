package io.spring2go.zuul.monitoring;

import io.spring2go.zuul.common.ITracer;

/**
 * Abstraction layer to provide time-based monitoring.
 *
 */
public abstract class TracerFactory {

    private static TracerFactory instance;

    /**
     * sets a TracerFactory Implementation
     *
     * @param f a <code>TracerFactory</code> value
     */
    public static final void initialize(TracerFactory f) {
        instance = f;
    }


    /**
     * Returns the singleton TracerFactory 
     *
     * @return a <code>TracerFactory</code> value
     */
    public static final TracerFactory instance() {
        if(instance == null) throw new IllegalStateException(String.format("%s not initialized", TracerFactory.class.getSimpleName()));
        return instance;
    }

    public abstract ITracer startMicroTracer(String name);

}