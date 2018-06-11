package io.spring2go.zuul.monitoring;

/**
 * Abstraction layer to provide counter based monitoring.
 *
 */
public abstract class CounterFactory {

    private static CounterFactory instance;

    /**
     * Pass in a CounterFactory Instance. This must be done to use Zuul as Zuul uses several internal counters
     *
     * @param f a <code>CounterFactory</code> value
     */
    public static final void initialize(CounterFactory f) {
        instance = f;
    }

    /**
     * return the singleton CounterFactory instance.
     *
     * @return a <code>CounterFactory</code> value
     */
    public static final CounterFactory instance() {
        if(instance == null) throw new IllegalStateException(String.format("%s not initialized", CounterFactory.class.getSimpleName()));
        return instance;
    }

    /**
     * Increments the counter of the given name
     *
     * @param name a <code>String</code> value
     */
    public abstract void increment(String name);

}