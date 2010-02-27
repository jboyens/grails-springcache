grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"

grails.project.dependency.resolution = {
	inherits("global")
	log "warn"
	repositories {
        grailsHome()
        grailsPlugins()
		mavenLocal()
		mavenCentral()
	}
	dependencies {
		compile("net.sf.ehcache:ehcache-web:2.0.0") {
			excludes "ehcache-core" // ehcache-core is provided by Grails
		}
		test("org.gmock:gmock:0.8.0") {
			excludes "junit"
		}
		test "org.hamcrest:hamcrest-all:1.1"
	}
}
