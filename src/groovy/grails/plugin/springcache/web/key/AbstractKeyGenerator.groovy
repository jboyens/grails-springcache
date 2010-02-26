package grails.plugin.springcache.web.key

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.FilterContext

abstract class AbstractKeyGenerator implements KeyGenerator {

	def generateKey(FilterContext context) {
		def builder = new CacheKeyBuilder()
		generateKeyInternal(builder, context)
		return builder.toCacheKey()
	}

	protected abstract void generateKeyInternal(CacheKeyBuilder builder, FilterContext context)

}
