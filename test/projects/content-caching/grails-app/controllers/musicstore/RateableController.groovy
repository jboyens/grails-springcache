package musicstore

import grails.plugin.springcache.annotations.CacheFlush

@CacheFlush(modelId = "RateableController")
class RateableController extends org.grails.rateable.RateableController {

}