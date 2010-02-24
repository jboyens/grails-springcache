package grails.plugin.springcache.web

import org.apache.commons.lang.time.StopWatch
import org.slf4j.LoggerFactory

class Timer {

	private final log = LoggerFactory.getLogger(getClass())
	private final stopWatch = new StopWatch()

	void start() {
		if (log.isInfoEnabled()) stopWatch.start()
	}

	void stop(String message) {
		if (log.isInfoEnabled()) {
			stopWatch.stop()
			log.info "$message: $stopWatch"
		}
	}

}
