package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.key.CacheKeyBuilder

class DefaultKeyGenerator implements KeyGenerator {

	def generateKey(CachingFilterContext context, HttpServletRequest request) {
		def builder = new CacheKeyBuilder()
		builder << context.controllerName
		builder << context.actionName
		context.params?.sort { it.key }?.each { entry ->
			if (!(entry.key in ["controller", "action"])) {
				builder << entry
			}
		}
		return builder.toCacheKey()
	}

}
