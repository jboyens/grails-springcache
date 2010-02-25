grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {        
        grailsPlugins()
        grailsHome()
        mavenLocal()
        mavenCentral()
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
//		test "org.codehaus.groovy.modules.http-builder:http-builder:0.5.0-RC2"
    }

}

grails.plugin.location.springcache = "../../.."