/*global window */

/**
 * @fileoverview duel.js: client-side template engine
 * 
 * http://duelengine.org
 * 
 * Copyright (c) 2006-2010 Stephen M. McKamey
 * Licensed under the MIT License (http://duelengine.org/license.txt)
 */

var duel = (

/**
 * @param {Window} window Window reference
 * @param {*=} undefn undefined constant
 */
function(window, undefn) {

	"use strict";

	/**
	 * @type {Document} document Document reference
	 */
	var document = window.document;

	/* types.js --------------------*/

	/**
	 * @private
	 * @const
	 * @type {number}
	 */
	var NUL = 0;

	/**
	 * @private
	 * @const
	 * @type {number}
	 */
	var FUN = 1;

	/**
	 * @private
	 * @const
	 * @type {number}
	 */
	var ARY = 2;

	/**
	 * @private
	 * @const
	 * @type {number}
	 */
	var OBJ = 3;

	/**
	 * @private
	 * @const
	 * @type {number}
	 */
	var VAL = 4;

	/**
	 * @private
	 * @const
	 * @type {number}
	 */
	var RAW = 5;

	/**
	 * Wraps a data value to maintain as raw markup in output
	 * 
	 * @private
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
	 * @public
	 * @override
	 * @this {Markup}
	 * @return {string} value
	 */
	Markup.prototype.toString = function() {
		return this.value;
	};

	/**
	 * Determines the type of the value
	 * 
	 * @private
	 * @param {*} val the object being tested
	 * @return {number}
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
	 * @private
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
	 * @private
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
	
	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var BIND_EXTERN = "bind";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var FOR = "$for";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var CHOOSE = "$choose";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var IF = "$if";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var ELSE = "$else";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var CALL = "$call";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var INIT = "$init";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var LOAD = "$load";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var EACH = "each";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var TEST = "test";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var VIEW = "view";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var MODEL = "model";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var INDEX = "index";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var COUNT = "count";

	var bind;

	/**
	 * Appends a node to a parent
	 * 
	 * @private
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
	 * @private
	 * @param {Array|Object|string|number|function(*,number,number):*} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @return {Array}
	 */
	function foreach(node, model, index, count) {
		var each = node[1] && node[1][EACH];

		// execute code block
		if (getType(each) === FUN) {
			each = each(model, index, count);
		}
	
		if (node.length === 3) {
			node = node[2];
		} else {
			node = [""].concat(node.slice(2));
		}
	
		var result = [""];
		switch (getType(each)) {
			case ARY:
				for (var i=0, length=each.length; i<length; i++) {
					append(result, bind(node, each[i], i, length));
				}
				break;
			case OBJ:
				for (var key in each) {
					if (each.hasOwnProperty(key)) {
						append(result, bind(node, each[key], key, 0));
					}
				}
				break;
		}
	
		return result;
	}
	
	/**
	 * Binds the node to the first child block which evaluates to true
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @return {Array|Object|string|number}
	 */
	function choose(node, model, index, count) {
		for (var i=1, length=node.length; i<length; i++) {
			
			var block = node[i],
				cmd = block[0],
				test = block[1] && block[1][TEST];
	
			switch (cmd) {
				case IF:
					if (getType(test) === FUN) {
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
	 * @private
	 * @param {Array|Object|string|number|function (*, *, *): (Object|null)} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @return {Array|Object|string|number}
	 */
	function call(node, model, index, count) {
		var args = node[1];
		if (!args || !args[VIEW]) {
			return null;
		}

		// evaluate the arguments
		var v = bind(args[VIEW], model, index, count),
			m = bind(args[MODEL], model, index, count),
			// Closure Compiler type cast
			i = /** @type {number|string} */ (bind(args[INDEX], model, index, count)),
			// Closure Compiler type cast
			c = /** @type {number} */ (bind(args[COUNT], model, index, count));

		return bind(duel(v).value, m, i, c);
	}
	
	/**
	 * Binds the node to model
	 * 
	 * @private
	 * @param {Array|Object|string|number|function (*, *, *): (Object|null)} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @return {Array|Object|string|number}
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
	 * @public
	 * @this {View}
	 * @param {*} model The data item being bound
	 */
	View.prototype[BIND_EXTERN] = View.prototype.bind = function(model) {
		var result = bind(this.value, model, 0, 1);
		return new Result(result);
	};


	/* render.js --------------------*/
	
	/**
	 * Void tag lookup
	 *  
	 * @private
	 * @constant
	 * @type {Object.<boolean>}
	 */
	var VOID_TAGS = {
		"area" : true,
		"base" : true,
		"basefont" : true,
		"br" : true,
		"col" : true,
		"frame" : true,
		"hr" : true,
		"img" : true,
		"input" : true,
		"isindex" : true,
		"keygen" : true,
		"link" : true,
		"meta" : true,
		"param" : true,
		"source" : true,
		"wbr" : true
	};
	
	/**
	 * Encodes invalid literal characters in strings
	 * 
	 * @private
	 * @param {Array|Object|string|number} val The value
	 * @return {Array|Object|string|number}
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
	 * @private
	 * @param {Array|Object|string|number} val The value
	 * @return {Array|Object|string|number}
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
	 * @private
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
	 * @private
	 * @param {Array} view The compiled view
	 * @return {string}
	 */
	 function render(view) {
		var output = [];
		renderElem(output, view);
		return output.join("");
	}
	
	/**
	 * Returns result as HTML text
	 * 
	 * @public
	 * @override
	 * @this {Result}
	 * @return {string}
	 */
	Result.prototype.toString = function() {
		return render(this.value);
	};
	
	/**
	 * Returns result as HTML text
	 * 
	 * @public
	 * @override
	 * @this {Result}
	 * @return {string}
	 */
	View.prototype.toString = function() {
		return render(this.value);
	};

	/* dom.js --------------------*/

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var TODOM = "toDOM";

	/**
	 * Attribute name map
	 * 
	 * @private
	 * @constant
	 * @type {Object.<string>}
	 */
	var ATTRMAP = {
		"rowspan" : "rowSpan",
		"colspan" : "colSpan",
		"cellpadding" : "cellPadding",
		"cellspacing" : "cellSpacing",
		"tabindex" : "tabIndex",
		"accesskey" : "accessKey",
		"hidefocus" : "hideFocus",
		"usemap" : "useMap",
		"maxlength" : "maxLength",
		"readonly" : "readOnly",
		"contenteditable" : "contentEditable"
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
		"enctype" : "encoding",
		"onscroll" : "DOMMouseScroll"
		// can add more attributes here as needed
	};

	/**
	 * Event names map
	 * 
	 * @private
	 * @constant
	 * @type {Object.<boolean>}
	 */
	var EVTS = {
		"onblur" : true,
		"onchange" : true,
		"onclick" : true,
		"ondblclick" : true,
		"onerror" : true,
		"onfocus" : true,
		"onkeydown" : true,
		"onkeypress" : true,
		"onkeyup" : true,
		"onload" : true,
		"onmousedown" : true,
		"onmouseenter" : true,
		"onmouseleave" : true,
		"onmousemove" : true,
		"onmouseout" : true,
		"onmouseover" : true,
		"onmouseup" : true,
		"onresize" : true,
		"onscroll" : true,
		"onselect" : true,
		"onsubmit" : true,
		"onunload" : true
		// can add more events here as needed
	};

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
	 * @private
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
	 * @private
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
	 * @private
	 * @param {Node} elem The element
	 * @param {Object} attr Attributes object
	 * @return {Node}
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
	 * @private
	 * @param {Node} node The node
	 * @return {boolean}
	 */
	function isWhitespace(node) {
		return !!node && (node.nodeType === 3) && (!node.nodeValue || !/\S/.exec(node.nodeValue));
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
			while (isWhitespace(elem.lastChild)) {
				// trim trailing whitespace text nodes
				elem.removeChild(elem.lastChild);
			}
		}
	}

	/**
	 * Removes leading and trailing whitespace nodes
	 * 
	 * @private
	 * @param {string|Markup} value The node
	 * @return {Node}
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
				elem[key] = undefn;
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
	 * Renders an error as a text node
	 * 
	 * @private
	 * @param {Error} ex The exception
	 * @return {Node}
	 */
	function onError(ex) {
		return document.createTextNode("["+ex+"]");
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
	 * @public
	 * @this {Result}
	 * @return {Node}
	 */
	Result.prototype[TODOM] = Result.prototype.toDOM = function() {
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
	 * @private
	 * @const
	 * @type {string}
	 */
	var DUEL_EXTERN = "duel";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var RAW_EXTERN = "raw";

	/**
	 * @public
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
	 * @return {View}
	 */
	var duel = window[DUEL_EXTERN] = function(view) {
		return (view instanceof View) ? view : new View(view);
	};
	
	/**
	 * @public
	 * @param {string} value Markup text
	 * @return {Markup}
	 */
	duel[RAW_EXTERN] = duel.raw = function(/*string*/ value) {
		return new Markup(value);
	};

	return duel;

})(window);
