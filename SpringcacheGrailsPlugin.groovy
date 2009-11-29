import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
import org.slf4j.LoggerFactory
import grails.plugins.springcache.implementations.ehcache.EhCacheProvider
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean

class SpringcacheGrailsPlugin {
	def version = "1.1-SNAPSHOT"
	def grailsVersion = "1.2-M3 > *"
	def dependsOn = [:]

	def author = "Rob Fletcher"
	def authorEmail = "rob@energizedwork.com"
	def title = "Spring Cache Plugin"
	def description = "Allows caching and flushing aspects to be added to Grails services using annotations."

	def documentation = "http://grails.org/Springcache+Plugin"

	def doWithSpring = {
		if (ConfigurationHolder.config.springcache.disabled) {
			log.warn "Springcache plugin is disabled"
		} else {
			autoProxyCreator(AnnotationAwareAspectJAutoProxyCreator) {
				proxyTargetClass = true
			}

			cacheProvider(EhCacheProvider) {
				cacheManager = ref("cacheManager")
			}

			cacheManager(EhCacheManagerFactoryBean)
		}
	}

	def doWithApplicationContext = { applicationContext ->
	}

	def onChange = {event ->
		// TODO: make sure we decorate changed / newly created annotated beans
	}

	def onConfigChange = {event ->
	}

	private static final log = LoggerFactory.getLogger("springcache.SpringcacheGrailsPlugin")

}
