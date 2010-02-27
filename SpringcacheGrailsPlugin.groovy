import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
import org.slf4j.LoggerFactory
import org.springframework.cache.ehcache.*
import grails.plugin.springcache.aop.CachingAspect
import grails.plugin.springcache.aop.FlushingAspect
import grails.plugin.springcache.web.GrailsFragmentCachingFilter
import org.springframework.web.filter.DelegatingFilterProxy
import grails.plugin.springcache.web.key.DefaultKeyGenerator

class SpringcacheGrailsPlugin {
	def version = "1.2-SNAPSHOT"
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
		if (!application.config.springcache.disabled) {
			def filters = xml.filter
			def lastFilter = filters[filters.size() - 1]
			lastFilter + {
				filter {
					"filter-name" "springcacheContentCache"
					"filter-class" DelegatingFilterProxy.name
					"init-param" {
						"param-name" "targetBeanName"
						"param-value" "springcacheFilter"
					}
					"init-param" {
						"param-name" "targetFilterLifecycle"
						"param-value" "true"
					}
				}
			}

			def filterMappings = xml."filter-mapping"
			def lastMapping = filterMappings[filterMappings.size() - 1]
			lastMapping + {
				"filter-mapping" {
					"filter-name" "springcacheContentCache"
					"url-pattern" "*.dispatch"
					dispatcher "FORWARD"
					dispatcher "INCLUDE"
				}
			}
		}
	}

	def doWithSpring = {
		if (application.config.springcache.disabled) {
			log.warn "Springcache plugin is disabled"
		} else {
			springcacheAutoProxyCreator(AnnotationAwareAspectJAutoProxyCreator) {
				proxyTargetClass = true
			}

			springcacheCacheManager(EhCacheManagerFactoryBean) {
				cacheManagerName = "Springcache Plugin Cache Manager"
			}

			application.config.springcache.caches.each {String name, ConfigObject cacheConfig ->
				"$name"(EhCacheFactoryBean) {bean ->
					cacheManager = ref("springcacheCacheManager")
					cacheName = name
					cacheConfig.each {
						bean.setPropertyValue it.key, it.value
					}
				}
			}

			springcacheCachingAspect(CachingAspect) {
				cacheManager = ref("springcacheCacheManager")
			}

			springcacheFlushingAspect(FlushingAspect) {
				cacheManager = ref("springcacheCacheManager")
			}

			springcacheFilter(GrailsFragmentCachingFilter) {
				cacheManager = ref("springcacheCacheManager")
				keyGenerator = new DefaultKeyGenerator()
			}
		}
	}

	def doWithDynamicMethods = {ctx ->
	}

	def doWithApplicationContext = {applicationContext ->
	}

	def onChange = {event ->
	}

	def onConfigChange = {event ->
	}

	private static final log = LoggerFactory.getLogger("grails.plugin.springcache.SpringcacheGrailsPlugin")

}

