package grails.plugin.springcache.web;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.constructs.web.filter.SimplePageFragmentCachingFilter;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;

public class ContentCachingFilter extends SimplePageFragmentCachingFilter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws Exception {
		GrailsWebRequest grailsRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes();

		if (grailsRequest.getControllerName() != null) {
			log.debug(String.format("Using caching filter for %s", grailsRequest.getControllerName()));
			super.doFilter(request, response, chain);
		} else {
			log.debug(String.format("Not caching %s", request.getRequestURI()));
			chain.doFilter(request, response);
		}
	}
}