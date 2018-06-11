package io.spring2go.zuul.common;

import io.spring2go.zuul.filters.ZuulFilter;

/**
 * Interface to provide instances of ZuulFilter from a given class.
 */
public interface IFilterFactory {

	/**
	 * Returns an instance of the specified class.
	 * 
	 * @param clazz
	 *            the Class to instantiate
	 * @return an instance of ZuulFilter
	 * @throws Exception
	 *             if an error occurs
	 */
	public ZuulFilter newInstance(Class clazz) throws Exception;
}