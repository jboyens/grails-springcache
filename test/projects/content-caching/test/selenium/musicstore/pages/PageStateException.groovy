package musicstore.pages

/**
 * Exception indicating an assertion as to the state of the page has failed, e.g. a link has been followed but not
 * loaded the expected page.
 */
class PageStateException extends RuntimeException {

	PageStateException(String message) {
		super(message)
	}

}