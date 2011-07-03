package org.duelengine.duel;

/**
 * Allows links within view markup to be transformed at runtime.
 * 
 * Useful for dynamically resolving links from custom names, or transforming URLs per environment (DEV/TEST/PROD).
 * 
 * LinkInterceptors may be chained using a Decorator pattern.
 */
public interface LinkInterceptor {

	public String transformURL(String url);
}
