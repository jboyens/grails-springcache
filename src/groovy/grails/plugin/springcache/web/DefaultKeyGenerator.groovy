package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.key.CacheKeyBuilder

class DefaultKeyGenerator implements KeyGenerator {

	def generateKey(CachingFilterContext context, HttpServletRequest request) {
		def builder = new CacheKeyBuilder()
		builder.append(context.controllerName) // TODO: override leftShift
		builder.append(context.actionName)
		context.params.each { entry ->
			builder.append(entry)
		}
		return builder.toCacheKey()
	}

}
