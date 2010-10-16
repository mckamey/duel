/**
 * @fileOverview  duel.js: client-side template engine
 * 
 * http://duelengine.org
 * 
 * Copyright (c) 2006-2010 Stephen M. McKamey
 * Licensed under the MIT License (http://duelengine.org/license.txt)
 */

var duel = (function() {

	/**
	 * Binds the node to model
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	var bind,

	/**
	 * Renders the result as a string
	 * 
	 * @param {Array|Object|string} view The bound view
	 * @returns {String}
	 */
	render,

	/**
	 * Builds the result as DOM
	 * 
	 * @param {Array} view The bound view
	 * @returns {DOMElement}
	 */
	build;

	/* Types --------------------*/

	/**
	 * Wraps a binding result with rendering methods
	 * 
	 * @constructor
	 * @this {Result}
	 * @param {Array|Object|string} view The result tree
	 */
	function Result(view) {
		this.value = view;
	}

	/**
	 * Returns result as DOM objects
	 * 
	 * @this {Result}
	 * @returns {Object}
	 */
	Result.prototype.toDOM = function() {
		return build(this.value);
//		return toDOM(render(this.value));
	};

	/**
	 * Returns result as HTML text
	 * 
	 * @this {Result}
	 * @returns {string}
	 */
	Result.prototype.toString = function() {
		return render(this.value);
	};

	/**
	 * Wraps a template definition with binding methods
	 * 
	 * @constructor
	 * @this {View}
	 * @param {Array|Object|string} view The template definition
	 */
	function View(view) {
		/**
		 * Appends a node to a parent
		 * 
		 * @this {View}
		 * @param {*} model The data item being bound
		 * @param {Array|Object|string} child The child node
		 */
		this.bind = function(model) {
			var result = bind(view, model);
			return new Result(result);
		};
	}

	/**
	 * Wraps a data value to maintain as raw markup in output
	 * 
	 * @constructor
	 * @this {Markup}
	 * @param {string} value The value
	 */
	function Markup(value) {
		this.value = value;
	}

	/**
	 * Renders the value
	 * 
	 * @this {Markup}
	 * @returns {string} value
	 */
	Markup.prototype.toString = function() {
		return this.value;
	};

	/**
	 * @constant
	 */
	var NUL = 0,
	/**
	 * @constant
	 */
	FUN = 1,
	/**
	 * @constant
	 */
	ARY = 2,
	/**
	 * @constant
	 */
	OBJ = 3,
	/**
	 * @constant
	 */
	VAL = 4,
	/**
	 * @constant
	 */
	RAW = 5,

	/**
	 * @constant
	 */
	FOR = "$for",
	/**
	 * @constant
	 */
	CHOOSE = "$choose",
	/**
	 * @constant
	 */
	IF = "$if",
	/**
	 * @constant
	 */
	ELSE = "$else",
	/**
	 * @constant
	 */
	INIT = "$init",
	/**
	 * @constant
	 */
	LOAD = "$load";

	/**
	 * Determines the type of the value
	 * 
	 * @param {*} val the object being tested
	 * @returns {number}
	 */
	function getType(val) {
		switch (typeof val) {
			case "object":
				return !val ? NUL : ((val instanceof Array) ? ARY : ((val instanceof Markup) ? RAW : OBJ));
			case "function":
				return FUN;
			case "undefined":
				return NUL;
			default:
				return VAL;
		}
	}

	/* Binding methods --------------------*/

	/**
	 * Appends a node to a parent
	 * 
	 * @param {Array} parent The parent node
	 * @param {Array|Object|string} child The child node
	 */
	function append(parent, child) {
		if (getType(parent) !== ARY) {
			// invalid
			return;
		}

		switch (getType(child)) {
			case ARY:
				if (child[0] === "") {
					// child is documentFragment
					// directly append children, skip fragment identifier
					for (var i=1, length=child.length; i<length; i++) {
						append(parent, child[i]);
					}
				} else {
					// child is an element array
					parent.push(child);
				}
				break;

			case OBJ:
				// child is attributes object
				var old = parent[1];
				if (getType(old) === OBJ) {
					// merge attribute objects
					for (var key in child) {
						if (child.hasOwnProperty(key)) {
							old[key] = child[key];
						}
					}
				} else {
					// insert attributes object
					parent.splice(1, 0, child);
				}
				break;

			case VAL:
				var last = parent.length - 1;
				if (last > 0 && getType(parent[last]) === VAL) {
					// combine string literals
					parent[last] = "" + parent[last] + child;
				} else {
					// append and convert primitive to string literal
					parent.push("" + child);
				}
				break;

			case NUL:
				// cull empty values
				break;

			default:
				// directly append
				parent.push(child);
				break;
		}
	}

	/**
	 * Binds the node once for each item in model
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array}
	 */
	function foreach(node, model, index, count) {
		var args = node[1];
		if (!args || !args.each) {
			return null;
		}

		// execute code block
		var items = (getType(args.each) === FUN) ?
			args.each(model, index, count) : args.each;

		if (node.length === 3) {
			node = node[2];
		} else {
			node = [""].concat(node.slice(2));
		}

		var result = [""];
		switch (getType(items)) {
			case ARY:
				for (var i=0, length=items.length; i<length; i++) {
					append(result, bind(node, items[i], i, length));
				}
				break;
			case OBJ:
				for (var key in items) {
					if (items.hasOwnProperty(key)) {
						append(result, bind(node, items[key], key, NaN));
					}
				}
				break;
		}

		return result;
	}

	/**
	 * Binds the node to the first child block which evaluates to true
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	function choose(node, model, index, count) {
		for (var i=1, length=node.length; i<length; i++) {
			var block = node[i],
				cmd = block[0],
				args = block[1];

			switch (cmd) {
				case IF:
					var test = args && args.test;
					if (getType(args.test) === FUN) {
						test = test(model, index, count);
					}
	
					if (!test) {
						continue;
					}

					// clone and bind block
					if (block.length === 3) {
						block = block[2];
					} else {
						node = [""].concat(node.slice(2));
					}
					return bind(block, model, index, count);

				case ELSE:
					// clone and bind block
					if (block.length === 2) {
						block = block[1];
					} else {
						node = [""].concat(node.slice(1));
					}
					return bind(block, model, index, count);
			}
		}

		return null;
	}

	/**
	 * Binds the node to model
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	bind = function(node, model, index, count) {
		/**
		 * @type {Array|Object|string|View}
		 */
		var result;

		switch (getType(node)) {
			case FUN:
				// execute code block
				result = node(model, index, count);

				while (result instanceof View) {
					// allow recursively binding templates
					// useful for creating "switcher" methods
					result = result.bind(model, index, count);
				}
				break;

			case ARY:
				// inspect element name for template commands
				/**
				 * @type {string}
				 */
				var tag = node[0] || "";
				switch (tag) {
					case FOR:
						result = foreach(node, model, index, count);
						break;
					case CHOOSE:
						result = choose(node, model, index, count);
						break;
					case IF:
					case ELSE:
						result = choose([CHOOSE, node], model, index, count);
						break;
					default:
						// element array, first item is name
						result = [tag];

						for (var i=1, length=node.length; i<length; i++) {
							append(result, bind(node[i], model, index, count));
						}
						break;
				}
				break;

			case OBJ:
				// attribute map
				result = {};
				for (var key in node) {
					if (node.hasOwnProperty(key)) {
						result[key] = bind(node[key], model, index, count);
					}
				}
				break;

			default:
				result = node;
				break;
		}

		return result;
	};

	/* Rendering methods --------------------*/

	/**
	 * Void tag lookup 
	 * @constant
	 * @type {Object}
	 */
	var VOID_TAGS = (function(names) {
			var tags = {};
			while (names.length) {
				tags[names.pop()] = true;
			}
			return tags;
		})("area,base,basefont,br,col,frame,hr,img,input,isindex,keygen,link,meta,param,source,wbr".split(','));

	/**
	 * Encodes invalid literal characters in strings
	 * 
	 * @param {Array|Object|string} val The value
	 * @returns {Array|Object|string}
	 */
	function htmlEncode(val) {
		return (typeof val !== "string") ? val : val.split('&').join('&amp;').split('<').join('&lt;').split('>').join('&gt;');
	}

	/**
	 * Encodes invalid attribute characters in strings
	 * 
	 * @param {Array|Object|string} val The value
	 * @returns {Array|Object|string}
	 */
	function attrEncode(val) {
		return (typeof val !== "string") ? val : val.split('&').join('&amp;').split('<').join('&lt;').split('>').join('&gt;').split('"').join('&quot;').split("'").join('&apos;');
	}

	/**
	 * Renders the result as a string
	 * 
	 * @param {Array} output The output container
	 * @param {Array} node The result tree
	 */
	function renderElem(output, node) {

		var tag = node[0],
			length = node.length,
			i = 1,
			child;

		if (tag) {
			// render open tag
			output.push('<', tag);

			child = node[i];
			if (getType(child) === OBJ) {
				// render attributes
				for (var name in child) {
					if (child.hasOwnProperty(name)) {
						output.push(' ', name);
						var val = child[name];
						if (val) {
							output.push('="', attrEncode(val), '"');
						}
					}
				}
				i++;
			}
			output.push('>');
		}

		// render children
		for (; i<length; i++) {
			child = node[i];
			if (getType(child) === ARY) {
				renderElem(output, child);
			} else {
				// encode string literals
				output.push(htmlEncode(child));
			}
		}

		if (tag && !VOID_TAGS[tag]) {
			// render close tag
			output.push('</', tag, '>');
		}
	}

	/**
	 * Renders the result as a string
	 * 
	 * @param {Array|Object|string} view The compiled view
	 * @returns {String}
	 */
	render = function(view) {
		if (getType(view) !== ARY) {
			// encode string literals
			return "" + htmlEncode(view);
		}

		var output = [];
		renderElem(output, view);
		return output.join("");
	};

	/* DOM Building methods --------------------*/

	/**
	 * Attribute name map
	 * @constant
	 * @type {Object}
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
	},

	/**
	 * Attribute duplicates map
	 * @constant
	 * @type {Object}
	 */
	ATTRDUP = {
		enctype : "encoding",
		onscroll : "DOMMouseScroll"
		// can add more attributes here as needed
	},

	/**
	 * Event names map
	 * @constant
	 * @type {Object}
	 */
	EVTS = (function(names) {
		var evts = {};
		while (names.length) {
			var evt = names.pop();
			evts["on"+evt.toLowerCase()] = evt;
		}
		return evts;
	})("blur,change,click,dblclick,error,focus,keydown,keypress,keyup,load,mousedown,mouseenter,mouseleave,mousemove,mouseout,mouseover,mouseup,resize,scroll,select,submit,unload".split(','));

	/**
	 * Appends a child to an element
	 * 
	 * @param {DOMElement} elem The parent element
	 * @param {DOMElement} child The child
	 */
	function appendChild(elem, child) {
		if (child) {
			if (elem.tagName && elem.tagName.toLowerCase() === "table" && elem.tBodies) {
				if (!child.tagName) {
					// must unwrap documentFragment for tables
					if (child.nodeType === 11) {
						while (child.firstChild) {
							appendChild(elem, child.removeChild(child.firstChild));
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
						tBody = document.createElement(childTag === "th" ? "thead" : "tbody");
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
	 * @param {DOMElement} elem The element
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
	 * @param {DOMElement} elem The element
	 * @param {Object} attr Attributes object
	 * @returns {DOMElement}
	 */
	function addAttributes(elem, attr) {
		if (attr.name && document.attachEvent) {
			try {
				// IE fix for not being able to programatically change the name attribute
				var alt = document.createElement("<"+elem.tagName+" name='"+attr.name+"'>");
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
	 * @param {DOMElement} node The node
	 * @returns {boolean}
	 */
	function isWhitespace(node) {
		return node && (node.nodeType === 3) && (!node.nodeValue || !/\S/.exec(node.nodeValue));
	}

	/**
	 * Removes leading and trailing whitespace nodes
	 * 
	 * @param {DOMElement} elem The node
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
	 * @returns {DOMElement}
	 */
	function toDOM(value) {
		var wrapper = document.createElement("div");
		wrapper.innerHTML = ""+value;

		// trim extraneous whitespace
		trimWhitespace(wrapper);

		// eliminate wrapper for single nodes
		if (wrapper.childNodes.length === 1) {
			return wrapper.firstChild;
		}

		// create a document fragment to hold elements
		var frag = document.createDocumentFragment ?
			document.createDocumentFragment() :
			document.createElement("");

		while (wrapper.firstChild) {
			frag.appendChild(wrapper.firstChild);
		}
		return frag;
	}

	/**
	 * Retrieve and remove method
	 * 
	 * @param {DOMElement} elem The element
	 * @param {string} key The callback name
	 * @returns {function(DOMElement)}
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
	 * @param {DOMElement} elem The element
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
	 * @returns {DOMElement}
	 */
	function onError(ex) {
		return document.createTextNode("["+ex+"]");
	}

	/**
	 * Applies node to DOM
	 * 
	 * @param {DOMElement} elem The element to append
	 * @param {Array} node The node to build
	 * @returns {DOMElement}
	 */
	function patch(elem, node) {

		for (var i=1; i<node.length; i++) {
			var child = node[i];
			switch (getType(child)) {
				case ARY:
				case VAL:
					// append children
					appendChild(elem, build(child));
					break;
				case OBJ:
					if (child instanceof Markup) {
						appendChild(elem, toDOM(child));
					} else if (elem.nodeType === 1) {
						// add attributes
						elem = addAttributes(elem, child);
					}
					break;
			}
		}

		return elem;
	}

	/**
	 * Builds the result as DOM
	 * 
	 * @param {Array} view The bound view
	 * @returns {DOMElement}
	 */
	build = function(view) {
		try {
			if (!view) {
				return null;
			}
			if (typeof view === "string") {
				return document.createTextNode(view);
			}
			if (view instanceof Markup) {
				return toDOM(view);
			}

			var tag = view[0]; // tagName
			if (!tag) {
				// correctly handle multiple-roots
				// create a document fragment to hold elements
				var frag = document.createDocumentFragment ?
					document.createDocumentFragment() :
					document.createElement("");
				for (var i=1; i<view.length; i++) {
					appendChild(frag, build(view[i]));
				}

				// trim extraneous whitespace
				trimWhitespace(frag);

				// eliminate wrapper for single nodes
				if (frag.childNodes.length === 1) {
					return frag.firstChild;
				}
				return frag;
			}

			if (tag.toLowerCase() === "style" && document.createStyleSheet) {
				// IE requires this interface for styles
				patch(document.createStyleSheet(), view);
				// in IE styles are effective immediately
				return null;
			}

			var elem = patch(document.createElement(tag), view);

			// trim extraneous whitespace
			trimWhitespace(elem);

			// trigger callbacks
			onInit(elem);

			return elem;
		} catch (ex) {
			try {
				// handle error with complete context
				var err = (typeof duel.onerror === "function") ? duel.onerror : onError;
				return err(ex, view);
			} catch (ex2) {
				return onError(ex2);
			}
		}
	};

	/* Factory methods --------------------*/

	/**
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} view The view template
	 * @returns {View}
	 */
	var duel = function(view) {
		return (view instanceof View) ? view : new View(view);
	};

	/**
	 * @param {string} value Markup text
	 * @returns {Markup}
	 */
	duel.raw = function(/*string*/ value) {
		return new Markup(value);
	};

	return duel;
})();
