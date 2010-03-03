/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web.key

import grails.plugin.springcache.web.FilterContext

class DefaultKeyGeneratorTests extends GroovyTestCase {

	KeyGenerator generator

	void setUp() {
		super.setUp()

		generator = new DefaultKeyGenerator()
	}

	void testKeyVariesOnControllerName() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key3 = generator.generateKey(new FilterContext(controllerName: "baz", actionName: "bar"))

		assertEquals key1, key2
		assertFalse key1 == key3
	}

	void testKeyVariesOnActionName() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz"))

		assertEquals key1, key2
		assertFalse key1 == key3
	}

	void testKeyVariesWithParams() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]))
		def key4 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]))
		def key5 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]))

		assertEquals key1, key2
		assertFalse key1 == key3
		assertFalse key3 == key4
		assertEquals key4, key5
	}

	void testParameterOrderNotImportant() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [foo: "bar", id: "1"]))

		assertEquals key1, key2
	}

	void testMatchingSubsetOfParamsCreatesDifferentKey() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]))

		assertFalse key1 == key2
	}

	void testControllerAndActionParamsAreIgnored() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [controller: "foo", action: "bar"]))

		assertEquals key1, key2
	}

}
