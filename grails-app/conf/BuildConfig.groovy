grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"

grails.project.dependency.resolution = {
	inherits "global"
	log "warn"
	repositories {
        grailsHome()
        grailsPlugins()
		mavenLocal()
		mavenCentral()
	}
	dependencies {
		compile ("net.sf.ehcache:ehcache:1.7.1") {
			excludes "jms", "commons-logging", "servlet-api"
		}
		compile("net.sf.ehcache:ehcache-web:2.0.0") {
			excludes "ehcache-core"
		}
	}
}
