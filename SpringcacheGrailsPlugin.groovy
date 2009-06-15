import com.energizedwork.cache.mapcache.CacheManager
import com.energizedwork.cache.mapcache.MapCacheFacade
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.exceptions.PluginException
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springmodules.cache.annotations.AnnotationCachingAttributeSource
import org.springmodules.cache.annotations.AnnotationFlushingAttributeSource
import org.springmodules.cache.interceptor.caching.CachingAttributeSourceAdvisor
import org.springmodules.cache.interceptor.caching.MetadataCachingInterceptor
import org.springmodules.cache.interceptor.flush.FlushingAttributeSourceAdvisor
import org.springmodules.cache.interceptor.flush.MetadataFlushingInterceptor
import org.springmodules.cache.provider.ehcache.EhCacheFacade
import org.springmodules.cache.provider.jboss.JbossCacheFacade
import org.springmodules.cache.provider.jboss.JbossCacheManagerFactoryBean
import org.springmodules.cache.provider.jcs.JcsFacade
import org.springmodules.cache.provider.jcs.JcsManagerFactoryBean
import org.springmodules.cache.provider.oscache.OsCacheFacade
import org.springmodules.cache.provider.oscache.OsCacheManagerFactoryBean

class SpringcacheGrailsPlugin {
	def version = '1.0.1'
	def dependsOn = [core: '1.0 > *']

	def author = "Rob Fletcher"
	def authorEmail = "rob@energizedwork.com"
	def title = "Spring Cache Plugin"
	def description = '''\
Uses the spring-modules-cache library to allow caching pointcuts to be easily applied to Spring managed beans in a
Grails application (e.g. services). Default cache configuration uses a simple ConcurrentHashMap backed cache
implementation but this can easily be overridden by declaring a springcache.provider property in Config.groovy. Valid
provider options are 'ehcache', 'jboss', 'jcs' and 'oscache', any other value will be ignored and the default cache
implementation used.

Simply add @Cacheable(modelId="myCachingModel1") to methods that should cache their results and
@CacheFlush(modelId="myFlushingModel") to methods that should flush the cache. The modelId attribute should match models
configured in Config.groovy, e.g.:
    springcache {
        cachingModels {
            myCachingModel1 = 'cacheName=CACHE_1'
            myCachingModel2 = 'cacheName=CACHE_2'
        }
        flushingModels {
            myFlushingModel = 'cacheNames=CACHE_1,CACHE_2'
        }
    }
For more on specific options for configuring caching and flushing models see:
https://springmodules.dev.java.net/docs/reference/0.9/html/cache.html#cachingModels and
https://springmodules.dev.java.net/docs/reference/0.9/html/cache.html#flushingModels

Be aware that the annotations will only have any effect on Spring-managed beans. If you create instances of your class
directly rather than getting them from the application context they will not be decorated with caching/flushing
behaviour.

See https://springmodules.dev.java.net/docs/reference/0.9/html/cache.html for more details on how to configure alternate
caching providers.

Caching can be disabled with the config key springcache.disabled=true, for exaple you may want to do this in your test
environment to avoid having methods you are testing returning cached results.
'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/Springcache+Plugin"

	def doWithSpring = {
		if (ConfigurationHolder.config.springcache.disabled) {
			log.warn "Springcache plugin is disabled"
		} else {
			def providerName = ConfigurationHolder.config.springcache.provider ?: 'mapcache'
			def provider = SpringcacheGrailsPlugin.getProvider(providerName)

			cacheManager(provider.cacheManager) {
				ConfigurationHolder.config.springcache."$providerName".each { k, v ->
					println "setting cacheManager.$k = $v"
					delegate."$k" = v
				}
			}

			cacheProviderFacade(provider.cacheProviderFacade) {
				cacheManager = ref("cacheManager")
			}

			// annotation based caching & flushing configuration
			autoproxy(DefaultAdvisorAutoProxyCreator) {
				proxyTargetClass = true
			}

			cachingAttributeSource(AnnotationCachingAttributeSource)

			cachingInterceptor(MetadataCachingInterceptor) {
				cacheProviderFacade = ref("cacheProviderFacade")
				cachingAttributeSource = ref("cachingAttributeSource")
				cachingListeners = []
				cachingModels = this.getCachingModelsConfig()
			}

			cachingAttributeSourceAdvisor(CachingAttributeSourceAdvisor, ref("cachingInterceptor"))

			flushingAttributeSource(AnnotationFlushingAttributeSource)

			flushingInterceptor(MetadataFlushingInterceptor) {
				cacheProviderFacade = ref("cacheProviderFacade")
				flushingAttributeSource = ref("flushingAttributeSource")
				flushingModels = this.getFlushingModelsConfig()
			}

			flushingAttributeSourceAdvisor(FlushingAttributeSourceAdvisor, ref("flushingInterceptor"))
		}
	}

	def doWithApplicationContext = { applicationContext ->
		if (log.isTraceEnabled() && !ConfigurationHolder.config.springcache.disabled) {
			log.trace applicationContext.getBean('cacheManager')?.dump()
			log.trace applicationContext.getBean('cacheProviderFacade')?.dump()
		}
	}

	def onChange = {event ->
		// TODO: make sure we decorate changed / newly created annotated beans
	}

	def onConfigChange = {event ->
	}

	private static final DEFAULT_CACHING_MODELS = [defaultCachingModel: 'cacheName=DefaultSpringCache']
	private static final DEFAULT_FLUSHING_MODELS = [defaultFlushingModel: 'cacheNames=DefaultSpringCache']

	private static final log = LogFactory.getLog('springcache.SpringcacheGrailsPlugin')

	static getProvider(providerName) {
		switch (providerName) {
			case 'ehcache':
				return [cacheManager: EhCacheManagerFactoryBean,
						cacheProviderFacade: EhCacheFacade]
			case 'jboss':
				return [cacheManager: JbossCacheManagerFactoryBean,
						cacheProviderFacade: JbossCacheFacade]
			case 'jcs':
				return [cacheManager: JcsManagerFactoryBean,
						cacheProviderFacade: JcsFacade]
			case 'oscache':
				return [cacheManager: OsCacheManagerFactoryBean,
						cacheProviderFacade: OsCacheFacade]
			case 'mapcache':
				return [cacheManager: CacheManager,
						cacheProviderFacade: MapCacheFacade]
			default:
				throw new PluginException("Unsupported caching provider: $providerName")
		}
	}

	private Properties getCachingModelsConfig() {
		def props = new Properties()
		def cachingModelsConfig = ConfigurationHolder.config.springcache.cachingModels ?: DEFAULT_CACHING_MODELS
		if (!cachingModelsConfig) {
			log.warn "No caching model configuration found, using default"
			cachingModelsConfig = DEFAULT_CACHING_MODELS
		}
		log.info "Configuring springcache caching models..."
		cachingModelsConfig.each {
			log.info "   $it.key: $it.value"
			props."$it.key" = it.value
		}
		return props
	}

	private Properties getFlushingModelsConfig() {
		def props = new Properties()
		def flushingModelsConfig = ConfigurationHolder.config.springcache.flushingModels ?: DEFAULT_FLUSHING_MODELS
		if (!flushingModelsConfig) {
			log.warn "No flushing model configuration found, using default"
			flushingModelsConfig = DEFAULT_FLUSHING_MODELS
		}
		log.info "Configuring springcache flushing models..."
		flushingModelsConfig.each {
			log.info "   $it.key: $it.value"
			props."$it.key" = it.value
		}
		return props
	}
}
