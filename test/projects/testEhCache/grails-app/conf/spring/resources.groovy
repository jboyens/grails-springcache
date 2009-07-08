import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.jmx.support.MBeanServerFactoryBean
import org.springframework.jmx.export.MBeanExporter

beans = {
	ehCacheMBeanRegistration(MethodInvokingFactoryBean) {bean ->
		staticMethod = "net.sf.ehcache.management.ManagementService.registerMBeans"
		arguments = [ref("cacheManager"), ref("mbeanServer"), true, true, true, true]
	}

	mbeanServer(MBeanServerFactoryBean) {
		locateExistingServerIfPossible = true
	}

//	mbeanExporter(org.springframework.jmx.export.MBeanExporter) {
//		beans = ["net.sf.ehcache:type=CacheStatistics,CacheManager=cacheManager,name=SAILOR_CACHE": ref("hibernateStatisticsMBean")]
//		beans = ["net.sf.ehcache:type=CacheStatistics,CacheManager=cacheManager,name=SHIP_CACHE": ref("hibernateStatisticsMBean")]
//		server = ref("mbeanServer")
//	}
}