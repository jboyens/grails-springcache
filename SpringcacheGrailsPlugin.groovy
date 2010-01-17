import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
import org.slf4j.LoggerFactory
import grails.plugin.springcache.provider.ehcache.EhCacheProvider
import org.springframework.cache.ehcache.*
import grails.plugin.springcache.CacheProvider
import grails.plugin.springcache.aop.CachingAspect
import grails.plugin.springcache.aop.FlushingAspect
import grails.plugin.springcache.CacheProvider

class SpringcacheGrailsPlugin {
	def version = "1.1.3"
	def grailsVersion = "1.2-M3 > *"
	def dependsOn = [:]
	def pluginExcludes = [
			"grails-app/views/**",
			"web-app/**",
			"**/.gitignore"
	]

	def author = "Rob Fletcher"
	def authorEmail = "rob@energizedwork.com"
	def title = "Spring Cache Plugin"
	def description = "Allows caching and flushing aspects to be added to Grails services using annotations."

	def documentation = "http://grails.org/Springcache+Plugin"

	def doWithWebDescriptor = {xml ->
	}

	def doWithSpring = {
		if (ConfigurationHolder.config.springcache.disabled) {
			log.warn "Springcache plugin is disabled"
		} else {
			springcacheAutoProxyCreator(AnnotationAwareAspectJAutoProxyCreator) {
				proxyTargetClass = true
			}

			if (!ConfigurationHolder.config.springcache.provider.bean) {
			    log.info "No springcache provider configured; using default EhCacheProvider..."
				springcacheCacheProvider(EhCacheProvider) {
					cacheManager = ref("springcacheCacheManager")
					createCachesOnDemand = true
				}

				springcacheCacheManager(EhCacheManagerFactoryBean) {
					cacheManagerName = "Springcache Plugin Cache Manager"
				}
				
				ConfigurationHolder.config.springcache.caches.each { String name, ConfigObject cacheConfig ->
					"$name"(EhCacheFactoryBean) { bean ->
						cacheManager = ref("springcacheCacheManager")
						cacheName = name
						cacheConfig.each {
							bean.setPropertyValue it.key, it.value
						}
					}
				}
			} else {
			    log.info "Using ${ConfigurationHolder.config.springcache.provider.bean} as springcache provider..."
			}

			springcacheCachingAspect(CachingAspect) {
				cacheProvider = ref(ConfigurationHolder.config.springcache.provider.bean ?: "springcacheCacheProvider")
			}

			springcacheFlushingAspect(FlushingAspect) {
				cacheProvider = ref(ConfigurationHolder.config.springcache.provider.bean ?: "springcacheCacheProvider")
			}
		}
	}

	def doWithDynamicMethods = {ctx ->
	}

	def doWithApplicationContext = {applicationContext ->
		if (!ConfigurationHolder.config.springcache.disabled) {
		    String providerBeanName = ConfigurationHolder.config.springcache.provider.bean ?: "springcacheCacheProvider"
		    CacheProvider provider = applicationContext.getBean(providerBeanName)
		    ConfigurationHolder.config.springcache.cachingModels.each {String modelId, ConfigObject modelConfig ->
			    if (log.isDebugEnabled()) log.debug "cachingModel id = $modelId, config = ${modelConfig.toProperties()}"
			    provider.addCachingModel modelId, modelConfig.toProperties()
		    }
		    ConfigurationHolder.config.springcache.flushingModels.each {String modelId, ConfigObject modelConfig ->
			    if (log.isDebugEnabled()) log.debug "flushingModel id = $modelId, config = ${modelConfig.toProperties()}"
			    provider.addFlushingModel modelId, modelConfig.toProperties()
		    }
		
			if (log.isDebugEnabled()) {
				log.debug "Configured caches: ${applicationContext.getBeansOfType(EhCacheFactoryBean).values().cacheName}"
			}
		}
	}
	
	def onChange = {event ->
	}

	def onConfigChange = {event ->
	}

	private static final log = LoggerFactory.getLogger("grails.plugin.springcache.SpringcacheGrailsPlugin")

}

