/**
 * @fileoverview duel: client-side template engine
 */

var duel = (function() {

	var visit, parse, render;
	
	/**
	 * Wraps a binding result with rendering methods
	 * 
	 * @constructor
	 * @this {Result}
	 * @param {Array|Object|string} view The result tree
	 */
	function Result(view) {
		/**
		 * Returns result as DOM objects
		 * 
		 * @this {Result}
		 * @param {function(DOMElement):DOMElement} filter JsonML filter function
		 * @returns {Object}
		 */
		this.toDOM = function(filter) {
			return parse(view, filter);
		};

		/**
		 * Returns result as HTML text
		 * 
		 * @this {Result}
		 * @returns {string}
		 */
		this.toString = function() {
			return render(view);
		};

		/**
		 * Returns result as JsonML
		 * 
		 * @this {Result}
		 * @returns {Array|Object|string}
		 */
		this.toJsonML = function() {
			return view;
		};
	}

	/**
	 * Wraps a template definition with binding methods
	 * 
	 * @constructor
	 * @this {Template}
	 * @param {Array|Object|string} view The template definition
	 */
	function Template(view) {
		if ("undefined" === typeof view) {
			throw new Error("View is undefined");
		}

		/**
		 * Appends a node to a parent
		 * 
		 * @this {Template}
		 * @param {*} model The data item being bound
		 * @param {Array|Object|string} child The child node
		 */
		this.bind = function(model) {
			var result = visit(view, model);
			return new Result(result);
		};
	}

	/**
	 * Wraps a value to signal no encoding
	 * 
	 * @constructor
	 * @this {Unparsed}
	 * @param {string} value The value
	 */
	function Unparsed(/*string*/ value) {
		this.value = value;
	}

	var NUL = 0,
		FUN = 1,
		ARY = 2,
		OBJ = 3,
		VAL = 4,

	//attribute name mapping
	ATTRMAP = {
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

	// attribute duplicates
	ATTRDUP = {
		enctype : "encoding",
		onscroll : "DOMMouseScroll"
		// can add more attributes here as needed
	},

	// event names
	EVTS = (function(/*string[]*/ names) {
		var evts = {};
		while (names.length) {
			var evt = names.shift();
			evts["on"+evt.toLowerCase()] = evt;
		}
		return evts;
	})("blur,change,click,dblclick,error,focus,keydown,keypress,keyup,load,mousedown,mouseenter,mouseleave,mousemove,mouseout,mouseover,mouseup,resize,scroll,select,submit,unload".split(','));

	/**
	 * Determines the type of the value
	 * 
	 * @param {*} val the object being tested
	 * @returns {string}
	 */
	function getType(val) {
		switch (typeof val) {
			case "object":
				return (val instanceof Array) ? ARY : (!val ? NUL : OBJ);
			case "function":
				return FUN;
			case "undefined":
				return NUL;
			default:
				return VAL;
		}
	}

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
		if ("string" === typeof handler) {
			/*jslint evil:true */
			handler = new Function("event", handler);
			/*jslint evil:false */
		}

		if ("function" !== typeof handler) {
			return;
		}

		elem[name] = handler;
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
						if ("undefined" !== typeof elem.style.cssText) {
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
					} else if ("string" === typeof value || "number" === typeof value || "boolean" === typeof value) {
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
	 * @param {string} value The node
	 * @returns {DOMElement}
	 */
	function hydrate(value) {
		var wrapper = document.createElement("div");
		wrapper.innerHTML = value;

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
	 * Renders an error as a text node
	 * 
	 * @param {Error} ex The exception
	 * @returns {DOMElement}
	 */
	function onError(ex) {
		return document.createTextNode("["+ex+"]");
	}

	/**
	 * Applies JsonML to DOM
	 * 
	 * @param {DOMElement} elem The element to append
	 * @param {Array} jml The JsonML structure to build
	 * @param {function(DOMElement):DOMElement} filter A filter method
	 * @returns {DOMElement}
	 */
	function patch(elem, jml, filter) {

		for (var i=1; i<jml.length; i++) {
			if (jml[i] instanceof Array || "string" === typeof jml[i]) {
				// append children
				appendChild(elem, parse(jml[i], filter));
			} else if (jml[i] instanceof Unparsed) {
				appendChild(elem, hydrate(jml[i].value));
			} else if ("object" === typeof jml[i] && jml[i] !== null && elem.nodeType === 1) {
				// add attributes
				elem = addAttributes(elem, jml[i]);
			}
		}

		return elem;
	}

	/**
	 * Builds DOM from JsonML
	 * 
	 * @param {Array} jml The JsonML structure to build
	 * @param {function(DOMElement):DOMElement} filter A filter method
	 * @returns {DOMElement}
	 */
	parse = function(jml, filter) {
		try {
			if (!jml) {
				return null;
			}
			if ("string" === typeof jml) {
				return document.createTextNode(jml);
			}
			if (jml instanceof Unparsed) {
				return hydrate(jml.value);
			}

			var tag = jml[0]; // tagName
			if (!tag) {
				// correctly handle multiple-roots
				// create a document fragment to hold elements
				var frag = document.createDocumentFragment ?
					document.createDocumentFragment() :
					document.createElement("");
				for (var i=1; i<jml.length; i++) {
					appendChild(frag, parse(jml[i], filter));
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
				patch(document.createStyleSheet(), jml, filter);
				// in IE styles are effective immediately
				return null;
			}

			var elem = patch(document.createElement(tag), jml, filter);

			// trim extraneous whitespace
			trimWhitespace(elem);
			return (elem && "function" === typeof filter) ? filter(elem) : elem;
		} catch (ex) {
			try {
				// handle error with complete context
				var err = ("function" === typeof duel.onerror) ? duel.onerror : onError;
				return err(ex, jml, filter);
			} catch (ex2) {
				return onError(ex2);
			}
		}
	};

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

			case FUN:
				// append and convert primitive to string literal
				parent.push(child);
				break;
		}
	}

	/**
	 * Binds the node once for each item in model
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
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
					append(result, visit(node, items[i], i, length));
				}
				break;
			case OBJ:
				for (var key in items) {
					if (items.hasOwnProperty(key)) {
						append(result, visit(node, items[key], key, NaN));
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
	 * @param {number=} index The index of the current data item
	 * @param {number=} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	function choose(node, model, index, count) {
		for (var i=1, length=node.length; i<length; i++) {
			var block = node[i],
				cmd = block[0],
				args = block[1];

			switch (cmd) {
				case "$if":
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
					return visit(block, model, index, count);

				case  "$else":
					// clone and bind block
					if (block.length === 2) {
						block = block[1];
					} else {
						node = [""].concat(node.slice(1));
					}
					return visit(block, model, index, count);
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
	visit = function(node, model, index, count) {
		/**
		 * @type {Array|Object|string|Template}
		 */
		var result;

		switch (getType(node)) {
			case FUN:
				// execute code block
				result = node(model, index, count);

				while (result instanceof Template) {
					// allow recursively binding templates
					// useful for creating "switcher" methods
					result = result.bind(model, index, count);
				}
				break;

			case ARY:
				// inspect element name for template commands
				switch (node[0]) {
					case "$for":
						result = foreach(node, model, index, count);
						break;
					case "$choose":
						result = choose(node, model, index, count);
						break;
					case "$if":
					case "$else":
						result = choose(["$choose", node], model, index, count);
						break;
					default:
						// element array, first item is name
						result = [node[0]];

						for (var i=1, length=node.length; i<length; i++) {
							append(result, visit(node[i], model, index, count));
						}
						break;
				}
				break;

			case OBJ:
				// attribute object
				result = {};
				for (var key in node) {
					if (node.hasOwnProperty(key)) {
						result[key] = visit(node[key], model, index, count);
					}
				}
				break;

			default:
				// ensure string
				result = "" + node;
				break;
		}

		return result;
	};

	/**
	 * Determines if a tag is self-closing
	 * 
	 * @param {string} tag The tag name
	 * @returns {boolean}
	 */
	function voidTag(tag) {
		switch (tag) {
			case "area":
			case "base":
			case "basefont":
			case "br":
			case "col":
			case "frame":
			case "hr":
			case "img":
			case "input":
			case "isindex":
			case "keygen":
			case "link":
			case "meta":
			case "param":
			case "source":
			case "wbr":
				return true;
			default:
				return false;
		}
	}

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
				// encode literals
				output.push(htmlEncode(child));
			}
		}

		if (tag) {
			if (!voidTag(tag)) {
				// render close tag
				output.push('</', tag, '>');
			}
		}
	}

	render = function(view) {
		if (getType(view) !== ARY) {
			// encode literals
			return ""+htmlEncode(view);
		}

		var output = [];
		renderElem(output, view);
		return output.join("");
	};

	/**
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} view The view template
	 * @returns {Template}
	 */
	var duel = function(view) {
		return (view instanceof Template) ? view : new Template(view);
	};
	
	duel.raw = function(/*string*/ value) {
		return new Unparsed(value);
	};

	return duel;
})();
