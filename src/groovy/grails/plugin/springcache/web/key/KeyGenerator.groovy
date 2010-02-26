package grails.plugin.springcache.web.key

import grails.plugin.springcache.web.FilterContext

interface KeyGenerator {

	def generateKey(FilterContext context)

}
