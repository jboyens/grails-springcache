package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.key.CacheKeyBuilder

class MimeTypeAwareKeyGenerator extends DefaultKeyGenerator {

	protected void generateKeyInternal(CacheKeyBuilder builder, CachingFilterContext context, HttpServletRequest request) {
		super.generateKeyInternal(builder, context, request)
		builder << request.format
	}

}
