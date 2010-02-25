package grails.plugin.springcache.web.key

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.FilterContext

class MimeTypeAwareKeyGenerator extends DefaultKeyGenerator {

	protected void generateKeyInternal(CacheKeyBuilder builder, FilterContext context, HttpServletRequest request) {
		super.generateKeyInternal(builder, context, request)
		builder << request.format
	}

}
