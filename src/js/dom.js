	/* dom.js --------------------*/
	
	/**
	 * Attribute name map
	 * @constant
	 * @type {Object.<string>}
	 */
	var ATTRMAP = {
		rowspan : "rowSpan",
		colspan : "colSpan",
		cellpadding : "cellPadding",
		cellspacing : "cellSpacing",
		tabindex : "tabIndex",
		accesskey : "accessKey",
		hidefocus : "hideFocus",
		usemap : "useMap",
		maxlength : "maxLength",
		readonly : "readOnly",
		contenteditable : "contentEditable"
		// can add more attributes here as needed
	};

	/**
	 * Attribute duplicates map
	 * @constant
	 * @type {Object.<string>}
	 */
	var ATTRDUP = {
		enctype : "encoding",
		onscroll : "DOMMouseScroll"
		// can add more attributes here as needed
	};

	/**
	 * Event names map
	 * @constant
	 * @type {Object.<string>}
	 */
	var EVTS = (function(names) {
		var evts = {};
		while (names.length) {
			var evt = names.pop();
			evts["on"+evt.toLowerCase()] = evt;
		}
		return evts;
	})("blur,change,click,dblclick,error,focus,keydown,keypress,keyup,load,mousedown,mouseenter,mouseleave,mousemove,mouseout,mouseover,mouseup,resize,scroll,select,submit,unload".split(','));

	/**
	 * Creates a DOM element 
	 * 
	 * @param {string} tag The element's tag name
	 * @returns {Node}
	 */
	function createElement(tag) {
		if (!tag) {
			// create a document fragment to hold multiple-root elements
			if (document.createDocumentFragment) {
				return document.createDocumentFragment();
			}

			tag = "";
		}

		if (tag.toLowerCase() === "style" && document.createStyleSheet) {
			// IE requires this interface for styles
			return document.createStyleSheet();
		}

		return document.createElement(tag);
	}

	/**
	 * Appends a child to an element
	 * 
	 * @param {Node} elem The parent element
	 * @param {Node} child The child
	 */
	function appendDOM(elem, child) {
		if (child) {
			if (elem.tagName && elem.tagName.toLowerCase() === "table" && elem.tBodies) {
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
				if (childTag && childTag !== "tbody" && childTag !== "thead") {
					// insert in last tbody
					var tBody = elem.tBodies.length > 0 ? elem.tBodies[elem.tBodies.length-1] : null;
					if (!tBody) {
						tBody = createElement(childTag === "th" ? "thead" : "tbody");
						elem.appendChild(tBody);
					}
					tBody.appendChild(child);
				} else if (elem.canHaveChildren !== false) {
					elem.appendChild(child);
				}

			} else if (elem.tagName && elem.tagName.toLowerCase() === "style" && document.createStyleSheet) {
				// IE requires this interface for styles
				elem.cssText = child;

			} else if (elem.canHaveChildren !== false) {
				elem.appendChild(child);

			} else if (elem.tagName && elem.tagName.toLowerCase() === "object" &&
				child.tagName && child.tagName.toLowerCase() === "param") {
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
	 * @param {Node} elem The element
	 * @param {string} name The event name
	 * @param {function(Event)} handler The event handler
	 */
	function addHandler(elem, name, handler) {
		if (typeof handler === "string") {
			/*jslint evil:true */
			handler = new Function("event", handler);
			/*jslint evil:false */
		}
	
		if (typeof handler === "function") {
			elem[name] = handler;
		}
	}

	/**
	 * Appends a child to an element
	 * 
	 * @param {Node} elem The element
	 * @param {Object} attr Attributes object
	 * @returns {Node}
	 */
	function addAttributes(elem, attr) {
		if (attr.name && document.attachEvent) {
			try {
				// IE fix for not being able to programatically change the name attribute
				var alt = createElement("<"+elem.tagName+" name='"+attr.name+"'>");
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
				var value = attr[name];
				if (name && value) {
					name = ATTRMAP[name.toLowerCase()] || name;
					if (name === "style") {
						if (typeof elem.style.cssText !== "undefined") {
							elem.style.cssText = value;
						} else {
							elem.style = value;
						}
					} else if (name === "class") {
						elem.className = value;
					} else if (EVTS[name]) {
						addHandler(elem, name, value);

						// also set duplicated events
						if (ATTRDUP[name]) {
							addHandler(elem, ATTRDUP[name], value);
						}
					} else if (getType(value) === VAL) {
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
	 * @param {Node} node The node
	 * @returns {boolean}
	 */
	function isWhitespace(node) {
		return !!node && (node.nodeType === 3) && (!node.nodeValue || !/\S/.exec(node.nodeValue));
	}

	/**
	 * Removes leading and trailing whitespace nodes
	 * 
	 * @param {Node} elem The node
	 */
	function trimWhitespace(elem) {
		if (elem) {
			while (isWhitespace(elem.firstChild)) {
				// trim leading whitespace text nodes
				elem.removeChild(elem.firstChild);
			}
			while (isWhitespace(elem.lastChild)) {
				// trim trailing whitespace text nodes
				elem.removeChild(elem.lastChild);
			}
		}
	}

	/**
	 * Removes leading and trailing whitespace nodes
	 * 
	 * @param {string|Markup} value The node
	 * @returns {Node}
	 */
	function toDOM(value) {
		var wrapper = createElement("div");
		wrapper.innerHTML = ""+value;
	
		// trim extraneous whitespace
		trimWhitespace(wrapper);

		// eliminate wrapper for single nodes
		if (wrapper.childNodes.length === 1) {
			return wrapper.firstChild;
		}

		// create a document fragment to hold elements
		var frag = createElement("");
		while (wrapper.firstChild) {
			frag.appendChild(wrapper.firstChild);
		}
		return frag;
	}

	/**
	 * Retrieve and remove method
	 * 
	 * @param {Node} elem The element
	 * @param {string} key The callback name
	 * @returns {function(Node)}
	 */
	function popCallback(elem, key) {
		var method = elem[key];
		if (method) {
			try {
				delete elem[key];
			} catch (ex) {
				// sometimes IE doesn't like deleting from DOM
				elem[key] = undef;
			}

			if (typeof method !== "function") {
				try {
					/*jslint evil:true */
					method = new Function(""+method);
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
	 * Renders an error as a text node
	 * 
	 * @param {Error} ex The exception
	 * @returns {Node}
	 */
	function onError(ex) {
		return document.createTextNode("["+ex+"]");
	}

	/**
	 * Applies node to DOM
	 * 
	 * @param {Node} elem The element to append
	 * @param {Array} node The node to populate
	 * @returns {Node}
	 */
	function patchDOM(elem, node) {
		for (var i=1; i<node.length; i++) {
			var child = node[i];
			switch (getType(child)) {
				case ARY:
					// append child element
					appendDOM(elem, patchDOM(createElement(child[0]), child));
					break;
				case VAL:
					// append child value
					appendDOM(elem, document.createTextNode(""+child));
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
	 * Returns result as DOM objects
	 * 
	 * @this {Result}
	 * @returns {Node}
	 */
	Result.prototype.toDOM = function() {
		try {
			return patchDOM(createElement(this.value[0]), this.value);
		} catch (ex) {
			try {
				// handle error with complete context
				var err = (typeof duel.onerror === "function") ? duel.onerror : onError;
				return err(ex, this.value);
			} catch (ex2) {
				return onError(ex2);
			}
		}
	};

