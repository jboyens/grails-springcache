package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest

interface KeyGenerator {

	def generateKey(CachingFilterContext context, HttpServletRequest request)

}
