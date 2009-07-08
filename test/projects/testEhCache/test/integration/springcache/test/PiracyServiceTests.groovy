package springcache.test

import javax.management.MBeanServer
import javax.management.ObjectName
import javax.management.MBeanInfo
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Cache
import net.sf.ehcache.Statistics

class PiracyServiceTests extends GroovyTestCase {

	CacheManager cacheManager
	def piracyService

	void testObjects() {
		piracyService.registerNewSailor(new Sailor(name: "Jack"))
		assertEquals "Jack", piracyService.sailors.name.join(", ")
		println "[$cacheManager.cacheNames]"
//		Statistics stats = cacheManager.getCache("SAILOR_CACHE").statistics
//		assertEquals 1, stats.cacheMisses
//		assertEquals 0, stats.cacheHits
	}

}