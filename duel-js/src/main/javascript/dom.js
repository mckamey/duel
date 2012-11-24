	/* dom.js --------------------*/

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
	var ATTR_MAP = {
		'accesskey': 'accessKey',
		'bgcolor': 'bgColor',
		'cellpadding': 'cellPadding',
		'cellspacing': 'cellSpacing',
		'checked': 'defaultChecked',
		'class': 'className',
		'colspan': 'colSpan',
		'contenteditable': 'contentEditable',
		'defaultchecked': 'defaultChecked',
		'for': 'htmlFor',
		'formnovalidate': 'formNoValidate',
		'hidefocus': 'hideFocus',
		'ismap': 'isMap',
		'maxlength': 'maxLength',
		'novalidate': 'noValidate',
		'readonly': 'readOnly',
		'rowspan': 'rowSpan',
		'spellcheck': 'spellCheck',
		'tabindex': 'tabIndex',
		'usemap': 'useMap',
		'willvalidate': 'willValidate'
		// can add more attributes here as needed
	};

	/**
	 * Attribute duplicates map
	 * 
	 * @private
	 * @constant
	 * @type {Object.<string>}
	 */
	var ATTR_DUP = {
		'enctype': 'encoding',
		'onscroll': 'DOMMouseScroll'
		// can add more attributes here as needed
	};

	/**
	 * Attributes to be set via DOM
	 * 
	 * @private
	 * @constant
	 * @type {Object.<number>}
	 */
	var ATTR_DOM = {
		'autocapitalize': 1,
		'autocomplete': 1,
		'autocorrect': 1
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
	 * Adds an event handler to an element
	 * 
	 * @private
	 * @param {Node} elem The element
	 * @param {string} name The event name
	 * @param {function(Event)} handler The event handler
	 */
	function addHandler(elem, name, handler) {
		if (name.substr(0,2) === 'on') {
			name = name.substr(2);
		}

		switch (typeof handler) {
			case 'function':
				if (elem.addEventListener) {
					// DOM Level 2
					elem.addEventListener(name, handler, false);

				} else if (isFunction(window.jQuery) && getType(elem[name]) !== NUL) {
					// cop out and patch IE6-8 with jQuery
					var $elem = window.jQuery(elem);
					if (isFunction($elem.on)) {
						$elem.on(name, handler);	// v1.7+
					} else {
						$elem.bind(name, handler);	// pre-1.7
					}

				} else if (elem.attachEvent && getType(elem[name]) !== NUL) {
					// IE legacy events
					elem.attachEvent('on'+name, handler);

				} else {
					// DOM Level 0
					var old = elem['on'+name] || elem[name];
					elem['on'+name] = elem[name] = !isFunction(old) ? handler :
						function(e) {
							return (old.call(this, e) !== false) && (handler.call(this, e) !== false);
						};
				}
				break;

			case 'string':
				// inline functions are DOM Level 0
				/*jslint evil:true */
				elem['on'+name] = new Function('event', handler);
				/*jslint evil:false */
				break;
		}
	}

	/**
	 * Appends an attribute to an element
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

					name = ATTR_MAP[name.toLowerCase()] || name;

					if (name === 'style') {
						if (getType(elem.style.cssText) !== NUL) {
							elem.style.cssText = value;
						} else {
							elem.style = value;
						}

					} else if (name.substr(0,2) === 'on') {
						addHandler(elem, name, value);

						// also set duplicated events
						name = ATTR_DUP[name];
						if (name) {
							addHandler(elem, name, value);
						}

					} else if (!ATTR_DOM[name.toLowerCase()] && (type !== VAL || name.charAt(0) === '$' || getType(elem[name]) !== NUL || getType(elem[ATTR_DUP[name]]) !== NUL)) {
						// direct setting of existing properties
						elem[name] = value;

						// also set duplicated properties
						name = ATTR_DUP[name];
						if (name) {
							elem[name] = value;
						}

					} else if (ATTR_BOOL[name.toLowerCase()]) {
						if (value) {
							// boolean attributes
							elem.setAttribute(name, name);

							// also set duplicated attributes
							name = ATTR_DUP[name];
							if (name) {
								elem.setAttribute(name, name);
							}
						}

					} else {
						// http://www.quirksmode.org/dom/w3c_core.html#attributes

						// custom and 'data-*' attributes
						elem.setAttribute(name, value);

						// also set duplicated attributes
						name = ATTR_DUP[name];
						if (name) {
							elem.setAttribute(name, value);
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
				try {
					// IE7 doesn't like deleting from DOM
					elem[key] = '';
					elem.removeAttribute(key);
				} catch (ex2) {}
			}

			if (!isFunction(method)) {
				try {
					/*jslint evil:true */
					method = new Function(''+method);
					/*jslint evil:false */
				} catch (ex3) {
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
	function callbacks(elem) {
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
						callbacks(child);

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
		callbacks(elem);

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
	 * @param {Node|string=} elem An optional element or element ID to be replaced or merged
	 * @param {boolean=} merge Optionally merge result into elem
	 * @return {Node|null}
	 */
	Result.prototype.toDOM = function(elem, merge) {
		// resolve the element ID
		if (getType(elem) === VAL) {
			elem = document.getElementById(
				// Closure Compiler type cast
				/** @type{string} */(elem));
		}

		var view;
		try {
			if (merge) {
				view = elem;
				elem = null;
			}
			// Closure Compiler type cast
			view = patchDOM(/** @type{Node} */(view) || createElement(this.value[0]), this.value);

		} catch (ex) {
			// handle error with context
			view = onErrorDOM(ex);
		}

		if (elem && elem.parentNode) {
			// replace existing element with result
			// Closure Compiler type cast
			elem.parentNode.replaceChild(view, /** @type{Node} */(elem));
		}

		return view;
	};

	/**
	 * Replaces entire document with this Result
	 * 
	 * @public
	 * @this {Result}
	 */
	Result.prototype.reload = function() {
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

