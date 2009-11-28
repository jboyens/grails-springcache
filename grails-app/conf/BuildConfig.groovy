grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
	inherits "global"
	log "warn"
	repositories {
        grailsPlugins()
        grailsHome()
		mavenLocal()
		mavenCentral()
        mavenRepo "http://download.java.net/maven/2/"
	}
	dependencies {
//		runtime ("opensymphony:oscache:2.4.1") {
//			excludes 'jms', 'commons-logging', 'servlet-api'
//		}
//        runtime ("net.sf.ehcache:ehcache:1.6.1") {
//            excludes 'jms', 'commons-logging', 'servlet-api'
//        }
//		test "org.gmock:gmock:0.8.0"
//		test "org.hamcrest:hamcrest-all:1.2"
	}
}