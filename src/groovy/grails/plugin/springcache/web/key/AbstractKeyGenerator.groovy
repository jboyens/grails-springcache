package grails.plugin.springcache.web.key

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.key.CacheKeyBuilder

import grails.plugin.springcache.web.FilterContext

abstract class AbstractKeyGenerator implements KeyGenerator {

	def generateKey(FilterContext context, HttpServletRequest request) {
		def builder = new CacheKeyBuilder()
		generateKeyInternal(builder, context, request)
		return builder.toCacheKey()
	}

	protected abstract void generateKeyInternal(CacheKeyBuilder builder, FilterContext context, HttpServletRequest request)

}
