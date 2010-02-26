package grails.plugin.springcache.web.key

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.FilterContext

class MimeTypeAwareKeyGenerator extends DefaultKeyGenerator {

	protected void generateKeyInternal(CacheKeyBuilder builder, FilterContext context) {
		super.generateKeyInternal(builder, context)
		builder << context.request.format
	}

}
