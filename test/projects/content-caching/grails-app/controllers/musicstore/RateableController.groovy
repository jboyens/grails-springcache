package musicstore

import grails.plugin.springcache.annotations.CacheFlush

@CacheFlush(["albumControllerCache", "popularControllerCache"])
class RateableController extends org.grails.rateable.RateableController {

}