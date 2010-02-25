package grails.plugin.springcache.web.key

import javax.servlet.http.HttpServletRequest
import grails.plugin.springcache.web.FilterContext

interface KeyGenerator {

	def generateKey(FilterContext context, HttpServletRequest request)

}
