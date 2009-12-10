import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
import org.slf4j.LoggerFactory
import grails.plugin.springcache.providers.ehcache.EhCacheProvider
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import grails.plugin.springcache.cache.CacheProvider
import grails.plugin.springcache.aop.CachingAspect
import grails.plugin.springcache.aop.FlushingAspect

class SpringcacheGrailsPlugin {
	def version = "1.1"
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
			springcacheAutoProxyCreator(AnnotationAwareAspectJAutoProxyCreator) {
				proxyTargetClass = true
			}

			if (!ConfigurationHolder.config.springcache.provider.bean) {
				springcacheCacheProvider(EhCacheProvider) {
					cacheManager = ref("springcacheCacheManager")
				}

				springcacheCacheManager(EhCacheManagerFactoryBean)
			}

			springcacheCachingAspect(CachingAspect) {
				cacheProvider = ref(ConfigurationHolder.config.springcache.provider.bean ?: "springcacheCacheProvider")
			}

			springcacheFlushingAspect(FlushingAspect) {
				cacheProvider = ref(ConfigurationHolder.config.springcache.provider.bean ?: "springcacheCacheProvider")
			}
		}
	}

	def doWithApplicationContext = {applicationContext ->
		String providerBeanName = ConfigurationHolder.config.springcache.provider.bean ?: "springcacheCacheProvider"
		CacheProvider provider = applicationContext.getBean(providerBeanName)
		ConfigurationHolder.config.springcache.cachingModels.each {String modelId, ConfigObject modelConfig ->
			log.debug "cachingModel id = $modelId, config = ${modelConfig.toProperties()}"
			provider.addCachingModel modelId, modelConfig.toProperties()
		}
		ConfigurationHolder.config.springcache.flushingModels.each {String modelId, ConfigObject modelConfig ->
			log.debug "flushingModel id = $modelId, config = ${modelConfig.toProperties()}"
			provider.addFlushingModel modelId, modelConfig.toProperties()
		}
	}

	def onChange = {event ->
		// TODO: make sure we decorate changed / newly created annotated beans
	}

	def onConfigChange = {event ->
	}

	private static final log = LoggerFactory.getLogger("grails.plugins.springcache.SpringcacheGrailsPlugin")

}
