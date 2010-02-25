package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.key.CacheKeyBuilder

abstract class AbstractKeyGenerator implements KeyGenerator {

	def generateKey(CachingFilterContext context, HttpServletRequest request) {
		def builder = new CacheKeyBuilder()
		generateKeyInternal(builder, context, request)
		return builder.toCacheKey()
	}

	protected abstract void generateKeyInternal(CacheKeyBuilder builder, CachingFilterContext context, HttpServletRequest request)

}
