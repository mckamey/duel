	/* dom.js --------------------*/

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var TODOM = 'toDOM';

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var RELOAD = 'reload';

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var ATTR_EXTERN = 'attr';

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var REPLACE_EXTERN = 'replace';

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var INIT = '$init';

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var LOAD = '$load';

	/**
	 * Attribute name map
	 * 
	 * @private
	 * @constant
	 * @type {Object.<string>}
	 */
	var ATTRMAP = {
		'rowspan' : 'rowSpan',
		'colspan' : 'colSpan',
		'cellpadding' : 'cellPadding',
		'cellspacing' : 'cellSpacing',
		'tabindex' : 'tabIndex',
		'accesskey' : 'accessKey',
		'hidefocus' : 'hideFocus',
		'usemap' : 'useMap',
		'maxlength' : 'maxLength',
		'readonly' : 'readOnly',
		'contenteditable' : 'contentEditable'
		// can add more attributes here as needed
	};

	/**
	 * Attribute duplicates map
	 * 
	 * @private
	 * @constant
	 * @type {Object.<string>}
	 */
	var ATTRDUP = {
		'enctype' : 'encoding',
		'onscroll' : 'DOMMouseScroll'
		// can add more attributes here as needed
	};

	/**
	 * Leading SGML line ending pattern
	 * 
	 * @private
	 * @constant
	 * @type {RegExp}
	 */
	var LEADING = /^[\r\n]+/;

	/**
	 * Trailing SGML line ending pattern
	 * 
	 * @private
	 * @constant
	 * @type {RegExp}
	 */
	var TRAILING = /[\r\n]+$/;

	/**
	 * Creates a DOM element 
	 * 
	 * @private
	 * @param {string} tag The element's tag name
	 * @return {Node}
	 */
	function createElement(tag) {
		if (!tag) {
			// create a document fragment to hold multiple-root elements
			if (document.createDocumentFragment) {
				return document.createDocumentFragment();
			}

			tag = '';

		} else if (tag.charAt(0) === '!') {
			return document.createComment(tag === '!' ? '' : tag.substr(1)+' ');
		}

		if (tag.toLowerCase() === 'style' && document.createStyleSheet) {
			// IE requires this interface for styles
			return document.createStyleSheet();
		}

		return document.createElement(tag);
	}

	/**
	 * Appends a child to an element
	 * 
	 * @private
	 * @param {Node} elem The parent element
	 * @param {Node} child The child
	 */
	function appendDOM(elem, child) {
		if (child) {
			var tag = (elem.tagName||'').toLowerCase();
			if (elem.nodeType === 8) { // comment
				if (child.nodeType === 3) { // text node
					elem.nodeValue += child.nodeValue;
				}
			} else if (tag === 'table' && elem.tBodies) {
				if (!child.tagName) {
					// must unwrap documentFragment for tables
					if (child.nodeType === 11) {
						while (child.firstChild) {
							appendDOM(elem, child.removeChild(child.firstChild));
						}
					}
					return;
				}

				// in IE must explicitly nest TRs in TBODY
				var childTag = child.tagName.toLowerCase();// child tagName
				if (childTag && childTag !== 'tbody' && childTag !== 'thead') {
					// insert in last tbody
					var tBody = elem.tBodies.length > 0 ? elem.tBodies[elem.tBodies.length-1] : null;
					if (!tBody) {
						tBody = createElement(childTag === 'th' ? 'thead' : 'tbody');
						elem.appendChild(tBody);
					}
					tBody.appendChild(child);
				} else if (elem.canHaveChildren !== false) {
					elem.appendChild(child);
				}

			} else if (tag === 'style' && document.createStyleSheet) {
				// IE requires this interface for styles
				elem.cssText = child;

			} else if (elem.canHaveChildren !== false) {
				elem.appendChild(child);

			} else if (tag === 'object' &&
				child.tagName && child.tagName.toLowerCase() === 'param') {
					// IE-only path
					try {
						elem.appendChild(child);
					} catch (ex1) {}
					try {
						if (elem.object) {
							elem.object[child.name] = child.value;
						}
					} catch (ex2) {}
			}
		}
	}

	/**
	 * Appends a child to an element
	 * 
	 * @private
	 * @param {Node} elem The element
	 * @param {string} name The event name
	 * @param {function(Event)} handler The event handler
	 */
	function addHandler(elem, name, handler) {
		if (isFunction(handler)) {
			if (elem.addEventListener) {
				// DOM Level 2
				elem.addEventListener((name.substr(0,2) === 'on') ? name.substr(2) : name, handler, false);
			} else {
				// DOM Level 0
				elem[name] = handler;
			}
		}

		else if (isString(handler)) {
			// inline functions are DOM Level 0
			/*jslint evil:true */
			elem[name] = new Function('event', handler);
			/*jslint evil:false */
		}
	}

	/**
	 * Appends a child to an element
	 * 
	 * @private
	 * @param {Node} elem The element
	 * @param {Object} attr Attributes object
	 * @return {Node}
	 */
	function addAttributes(elem, attr) {
		if (attr.name && document.attachEvent && !elem.parentNode) {
			try {
				// IE fix for not being able to programatically change the name attribute
				var alt = createElement('<'+elem.tagName+' name="'+attr.name+'">');
				// fix for Opera 8.5 and Netscape 7.1 creating malformed elements
				if (elem.tagName === alt.tagName) {
					elem = alt;
				}
			} catch (ex) { }
		}

		// for each attributeName
		for (var name in attr) {
			if (attr.hasOwnProperty(name)) {
				// attributeValue
				var value = attr[name],
					type = getType(value);

				if (name) {
					if (type === NUL) {
						value = '';
						type = VAL;
					}

					name = ATTRMAP[name.toLowerCase()] || name;
					if (name === 'style') {
						if (typeof elem.style.cssText !== 'undefined') {
							elem.style.cssText = value;
						} else {
							elem.style = value;
						}

					} else if (name === 'class') {
						elem.className = value;

					} else if (name.substr(0,2) === 'on') {
						addHandler(elem, name, value);

						// also set duplicated events
						if (ATTRDUP[name]) {
							addHandler(elem, ATTRDUP[name], value);
						}

					} else if (type === VAL) {
						elem.setAttribute(name, value);
	
						// also set duplicated attributes
						if (ATTRDUP[name]) {
							elem.setAttribute(ATTRDUP[name], value);
						}

					} else {
						// allow direct setting of complex properties
						elem[name] = value;
	
						// also set duplicated attributes
						if (ATTRDUP[name]) {
							elem[ATTRDUP[name]] = value;
						}
					}
				}
			}
		}
		return elem;
	}

	/**
	 * Tests a node for whitespace
	 * 
	 * @private
	 * @param {Node} node The node
	 * @return {boolean}
	 */
	function isWhitespace(node) {
		return !!node && (node.nodeType === 3) && (!node.nodeValue || !/\S/.exec(node.nodeValue));
	}

	/**
	 * Trims whitespace pattern from the text node
	 * 
	 * @private
	 * @param {Node} node The node
	 */
	function trimPattern(node, pattern) {
		if (!!node && (node.nodeType === 3) && pattern.exec(node.nodeValue)) {
			node.nodeValue = node.nodeValue.replace(pattern, '');
		}
	}

	/**
	 * Removes leading and trailing whitespace nodes
	 * 
	 * @private
	 * @param {Node} elem The node
	 */
	function trimWhitespace(elem) {
		if (elem) {
			while (isWhitespace(elem.firstChild)) {
				// trim leading whitespace text nodes
				elem.removeChild(elem.firstChild);
			}
			// trim leading whitespace text
			trimPattern(elem.firstChild, LEADING);
			while (isWhitespace(elem.lastChild)) {
				// trim trailing whitespace text nodes
				elem.removeChild(elem.lastChild);
			}
			// trim trailing whitespace text
			trimPattern(elem.lastChild, TRAILING);
		}
	}

	/**
	 * Converts the markup to DOM nodes
	 * 
	 * @private
	 * @param {string|Markup} value The node
	 * @return {Node}
	 */
	function toDOM(value) {
		var wrapper = createElement('div');
		wrapper.innerHTML = ''+value;
	
		// trim extraneous whitespace
		trimWhitespace(wrapper);

		// eliminate wrapper for single nodes
		if (wrapper.childNodes.length === 1) {
			return wrapper.firstChild;
		}

		// create a document fragment to hold elements
		var frag = createElement('');
		while (wrapper.firstChild) {
			frag.appendChild(wrapper.firstChild);
		}
		return frag;
	}

	/**
	 * Retrieve and remove method
	 * 
	 * @private
	 * @param {Node} elem The element
	 * @param {string} key The callback name
	 * @return {function(Node)}
	 */
	function popCallback(elem, key) {
		var method = elem[key];
		if (method) {
			try {
				delete elem[key];
			} catch (ex) {
				// sometimes IE doesn't like deleting from DOM
				elem[key] = undefined;
			}

			if (!isFunction(method)) {
				try {
					/*jslint evil:true */
					method = new Function(''+method);
					/*jslint evil:false */
				} catch (ex2) {
					// filter
					method = null;
				}
			}
		}

		return method;
	}

	/**
	 * Executes oninit/onload callbacks
	 * 
	 * @private
	 * @param {Node} elem The element
	 */
	function onInit(elem) {
		if (!elem) {
			return;
		}

		// execute and remove oninit method
		var method = popCallback(elem, INIT);
		if (method) {
			// execute in context of element
			method.call(elem);
		}

		// execute and remove onload method
		method = popCallback(elem, LOAD);
		if (method) {
			// queue up to execute after insertion into parentNode
			setTimeout(function() {
				// execute in context of element
				method.call(elem);
				method = elem = null;
			}, 0);
		} else {
			method = elem = null;
		}
	}

	/**
	 * Applies node to DOM
	 * 
	 * @private
	 * @param {Node} elem The element to append
	 * @param {Array} node The node to populate
	 * @return {Node}
	 */
	function patchDOM(elem, node) {
		for (var i=1, length=node.length; i<length; i++) {
			var child = node[i];
			switch (getType(child)) {
				case ARY:
					// build child element
					var childTag = child[0];
					child = patchDOM(createElement(childTag), child);

					if (childTag === 'html') {
						// trim extraneous whitespace
						trimWhitespace(child);

						// trigger callbacks
						onInit(child);

						// unwrap HTML root, to simplify insertion
						return child;
					}

					// append child element
					appendDOM(elem, child);
					break;
				case VAL:
					if (child !== '') {
						// append child value as text
						appendDOM(elem, document.createTextNode(''+child));
					}
					break;
				case OBJ:
					if (elem.nodeType === 1) {
						// add attributes
						elem = addAttributes(elem, child);
					}
					break;
				case RAW:
					appendDOM(elem, toDOM(child));
					break;
			}
		}

		// trim extraneous whitespace
		trimWhitespace(elem);

		// trigger callbacks
		onInit(elem);

		// eliminate wrapper for single nodes
		if (elem.nodeType === 11 && elem.childNodes.length === 1) {
			elem = elem.firstChild;
		}

		return elem;
	}

	/**
	 * Renders an error as a text node
	 * 
	 * @private
	 * @param {Error} ex The exception
	 * @return {Node}
	 */
	function onErrorDOM(ex) {
		return document.createTextNode(onError(ex));
	}

	/**
	 * Returns result as DOM objects
	 * 
	 * @public
	 * @this {Result}
	 * @return {Node}
	 */
	Result.prototype[TODOM] = Result.prototype.toDOM = function() {
		try {
			return patchDOM(createElement(this.value[0]), this.value);
		} catch (ex) {
			// handle error with context
			return onErrorDOM(ex);
		}
	};

	/**
	 * Replaces entire document with this Result
	 * 
	 * @public
	 * @this {Result}
	 */
	Result.prototype[RELOAD] = Result.prototype.reload = function() {
		// http://stackoverflow.com/questions/4297877
		var doc = document;
		try {
			var newRoot = this.toDOM();
			doc.replaceChild(newRoot, doc.documentElement);

			if (doc.createStyleSheet) {
				// IE requires link repair
				var head = newRoot.firstChild;
				while (head && (head.tagName||'') !== 'HEAD') {
					head = head.nextSibling;
				}

				var link = head && head.firstChild;
				while (link) {
					if ((link.tagName||'') === 'LINK') {
						// this seems to repair the link
						link.href = link.href;
					}
					link = link.nextSibling;
				}
			}
		} catch (ex) {
			/*jslint evil:true*/
			doc = doc.open('text/html');
			doc.write(this.toString());
			doc.close();
			/*jslint evil:false*/
		}
	};

	/**
	 * @public
	 * @param {Node} elem The element to affect 
	 * @param {Object} node The attributes object to apply
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 */
	duel[ATTR_EXTERN] = duel.attr = function(elem, attr, data, index, count, key) {
		// resolve the element ID
		if (getType(elem) === VAL) {
			elem = document.getElementById(elem);
		}

		if (elem) {
			// bind attribute nodes
			attr = bind(attr, data, index, count, key);

			// apply them to the existing element
			// Closure Compiler type cast
			addAttributes(elem, /** @type {Array} */(attr));
		}
	};

	/**
	 * @public
	 * @param {Node} elem The element to be replaced
	 * @param {Array|Object|string|number|function(*,*,*,*):(Object|null)} view The view to replace
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 */
	duel[REPLACE_EXTERN] = duel.replace = function(elem, view, data, index, count, key) {
		// resolve the element ID
		if (getType(elem) === VAL) {
			elem = document.getElementById(elem);
		}

		if (elem && elem.parentNode) {
			// bind node
			view = duel(view).getView();
			// Closure Compiler type cast
			view = bind(/** @type {Array} */(view), data, index, count, key);

			try {
				view = patchDOM(createElement(view[0]), view);
			} catch (ex) {
				// handle error with context
				view = onErrorDOM(ex);
			}

			// replace existing element with result
			elem.parentNode.replaceChild(view, elem);
		}
	};

