package grails.plugin.springcache.web.key

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.FilterContext

class DefaultKeyGenerator extends AbstractKeyGenerator {

	protected void generateKeyInternal(CacheKeyBuilder builder, FilterContext context) {
		builder << context.controllerName
		builder << context.actionName
		context.params?.sort { it.key }?.each { entry ->
			if (!(entry.key in ["controller", "action"])) {
				builder << entry
			}
		}
	}
	
}