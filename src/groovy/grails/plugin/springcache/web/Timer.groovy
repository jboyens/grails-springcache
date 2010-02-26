package grails.plugin.springcache.web

import org.apache.commons.lang.time.StopWatch
import org.slf4j.LoggerFactory

class Timer {

	private final log = LoggerFactory.getLogger(getClass())
	private final uri
	private final stopWatch = new StopWatch()

	void start(String uri) {
		if (log.isInfoEnabled()) {
			this.uri = uri
			stopWatch.start()
		}
	}

	void stop(boolean cached) {
		if (log.isInfoEnabled()) {
			stopWatch.stop()
			log.info "${cached ? 'Cached' : 'Uncached'} request for $uri took $stopWatch"
		}
	}

}
