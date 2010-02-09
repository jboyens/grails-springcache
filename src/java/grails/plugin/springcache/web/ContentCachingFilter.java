package grails.plugin.springcache.web;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;

public class ContentCachingFilter implements Filter, ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private ApplicationContext applicationContext;

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		GrailsWebRequest grailsRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		System.out.printf("%nFiltering URI = %s%n", request.getRequestURI());
		System.out.printf("    controller = %s, action = %s%n", grailsRequest.getControllerName(), grailsRequest.getActionName());

		if (grailsRequest.getControllerName() != null) {
			response.getWriter().printf("<html><head><title>Cached Response from %s %s</title><body><h1>Cached Response</h1></body></html>", grailsRequest.getControllerName(), grailsRequest.getActionName());
		} else {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}