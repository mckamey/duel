/**
 * @fileoverview duel.js: client-side template engine
 * 
 * http://duelengine.org
 * 
 * Copyright (c) 2006-2010 Stephen M. McKamey
 * Licensed under the MIT License (http://duelengine.org/license.txt)
 */

/*jslint browser: true, undef: true, eqeqeq: true, regexp: true, newcap: true */

var duel = (

/**
 * @param {Document} document Document sandboxed to correct window
 * @param {*=} undef undefined
 */
function(document, undef) {

	/* types.js --------------------*/
	
	/**
	 * @type {string}
	 * @const
	 */
	var FOR = "$for";
	/**
	 * @type {string}
	 * @const
	 */
	var CHOOSE = "$choose";
	/**
	 * @type {string}
	 * @const
	 */
	var IF = "$if";
	/**
	 * @type {string}
	 * @const
	 */
	var ELSE = "$else";
	/**
	 * @type {string}
	 * @const
	 */
	var CALL = "$call";
	/**
	 * @type {string}
	 * @const
	 */
	var INIT = "$init";
	/**
	 * @type {string}
	 * @const
	 */
	var LOAD = "$load";
	
	/**
	 * @type {number}
	 * @const
	 */
	var NUL = 0;
	/**
	 * @type {number}
	 * @const
	 */
	var FUN = 1;
	/**
	 * @type {number}
	 * @const
	 */
	var ARY = 2;
	/**
	 * @type {number}
	 * @const
	 */
	var OBJ = 3;
	/**
	 * @type {number}
	 * @const
	 */
	var VAL = 4;
	/**
	 * @type {number}
	 * @const
	 */
	var RAW = 5;
	
	/**
	 * Wraps a data value to maintain as raw markup in output
	 * 
	 * @this {Markup}
	 * @param {string} value The value
	 * @constructor
	 */
	function Markup(value) {
		/**
		 * @type {string}
		 * @const
		 * @protected
		 */
		this.value = value;
	}
	
	/**
	 * Renders the value
	 * 
	 * @override
	 * @this {Markup}
	 * @returns {string} value
	 */
	Markup.prototype.toString = function() {
		return this.value;
	};
	
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
	
	/**
	 * Wraps a binding result with rendering methods
	 * 
	 * @this {Result}
	 * @param {Array|Object|string|number} view The result tree
	 * @constructor
	 */
	function Result(view) {
		if (getType(view) !== ARY) {
			// ensure is rooted element
			view = ["", view];
		}
	
		/**
		 * @type {Array}
		 * @const
		 * @protected
		 */
		// Closure Compiler type cast
		this.value = /** @type {Array} */(view);
	}
	
	/**
	 * Wraps a template definition with binding methods
	 * 
	 * @this {View}
	 * @param {Array|Object|string|number} view The template definition
	 * @constructor
	 */
	function View(view) {
		if (getType(view) !== ARY) {
			// ensure is rooted element
			view = ["", view];
		}
	
		/**
		 * @type {Array}
		 * @const
		 * @protected
		 */
		// Closure Compiler type cast
		this.value = /** @type {Array} */(view);
	}

	/* bind.js --------------------*/
	
	var bind;
	
	/**
	 * Appends a node to a parent
	 * 
	 * @param {Array} parent The parent node
	 * @param {Array|Object|string|number} child The child node
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
	 * @param {Array|Object|string|number|function(*,number,number):*} node The template subtree root
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
						append(result, bind(node, items[key], key, 0));
					}
				}
				break;
		}
	
		return result;
	}
	
	/**
	 * Binds the node to the first child block which evaluates to true
	 * 
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array|Object|string|number}
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
	
					// clone and process block
					if (block.length === 3) {
						block = block[2];
					} else {
						node = [""].concat(node.slice(2));
					}
					return bind(block, model, index, count);
	
				case ELSE:
					// clone and process block
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
	 * Calls into another view
	 * 
	 * @param {Array|Object|string|number|function (*, *, *): (Object|null)} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array|Object|string|number}
	 */
	function call(node, model, index, count) {
		var args = node[1];
		if (!args) {
			return null;
		}
	
		// evaluate the arguments
		var v = bind(args.view, model, index, count),
			m = bind(args.model, model, index, count),
			// Closure Compiler type cast
			i = /** @type {number|string} */ (bind(args.index, model, index, count)),
			// Closure Compiler type cast
			c = /** @type {number} */ (bind(args.count, model, index, count));
	
		return bind(duel(v).value, m, i, c);
	}
	
	/**
	 * Binds the node to model
	 * 
	 * @param {Array|Object|string|number|function (*, *, *): (Object|null)} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @returns {Array|Object|string|number}
	 */
	bind = function(node, model, index, count) {
		/**
		 * @type {Array|Object|string|number}
		 */
		var result;
	
		switch (getType(node)) {
			case FUN:
				// execute code block
				// Closure Compiler type cast
				result = (/** @type {function (*, *, *): (Object|null)} */ (node))(model, index, count);
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
					case CALL:
						result = call(node, model, index, count);
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
	
	/**
	 * Binds and wraps the result
	 * 
	 * @this {View}
	 * @param {*} model The data item being bound
	 */
	View.prototype.bind = function(model) {
		var result = bind(this.value, model, 0, 1);
		return new Result(result);
	};

	/* render.js --------------------*/
	
	/**
	 * Void tag lookup 
	 * @constant
	 * @type {Object.<boolean>}
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
	 * @param {Array|Object|string|number} val The value
	 * @returns {Array|Object|string|number}
	 */
	function htmlEncode(val) {
		if (typeof val !== "string") {
			return val;
		}
	
		return val.replace(/[&<>]/g,
			function(ch) {
				switch(ch) {
					case '&':
						return "&amp;";
					case '<':
						return "&lt;";
					case '>':
						return "&gt;";
					default:
						return ch;
				}
			});
	}
	
	/**
	 * Encodes invalid attribute characters in strings
	 * 
	 * @param {Array|Object|string|number} val The value
	 * @returns {Array|Object|string|number}
	 */
	function attrEncode(val) {
		if (typeof val !== "string") {
			return val;
		}
	
		return val.replace(/[&<>"]/g,
			function(ch) {
				switch(ch) {
					case '&':
						return "&amp;";
					case '<':
						return "&lt;";
					case '>':
						return "&gt;";
					case '"':
						return "&quot;";
					default:
						return ch;
				}
			});
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
			// emit open tag
			output.push('<', tag);
	
			child = node[i];
			if (getType(child) === OBJ) {
				// emit attributes
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
	
		// emit children
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
			// emit close tag
			output.push('</', tag, '>');
		}
	}
	
	/**
	 * Renders the result as a string
	 * 
	 * @param {Array} view The compiled view
	 * @returns {string}
	 */
	 function render(view) {
		var output = [];
		renderElem(output, view);
		return output.join("");
	}
	
	/**
	 * Returns result as HTML text
	 * 
	 * @override
	 * @this {Result}
	 * @returns {string}
	 */
	Result.prototype.toString = function() {
		return render(this.value);
	};
	
	/**
	 * Returns result as HTML text
	 * 
	 * @override
	 * @this {Result}
	 * @returns {string}
	 */
	View.prototype.toString = function() {
		return render(this.value);
	};

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

	/* factory.js --------------------*/
	
	/**
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
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

})(document);