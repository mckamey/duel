/**
 * @fileoverview duel: client-side template engine
 */

var duel = (function() {

	var NUL = 0,
		FUN = 1,
		ARY = 2,
		OBJ = 3,
		VAL = 4;

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

			if (cmd === "$if" && args && args.test) {
				var test = (getType(args.test) === FUN) ?
					args.test(model, index, count) : args.test;

				if (test) {
					// clone and bind block
					if (block.length === 3) {
						block = block[2];
					} else {
						node = [""].concat(node.slice(2));
					}
					return visit(block, model, index, count);
				}
			} else if (cmd === "$else") {
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
	 * @param {number=|string=} index The index of the current data item
	 * @param {number=} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	function visit(node, model, index, count) {
		/**
		 * @type {Array|Object|string}
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
	}

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
	 * @param {Array|Object|string} view The JsonML result tree
	 * @returns {string}
	 */
	function render(view) {
		var name, i,
			stack = [view],
			output = [];

		while (stack.length) {
			var top = stack.pop();

			if (top instanceof Array) {
				name = top[0];
				if (name) {

					output.push('<', name);
					if (voidTag(name)) {
						stack.push(' />');
						for (i=top.length-1; i>0; i--) {
							stack.push(htmlEncode(top[i]));
						}
					} else {
						stack.push('>', name, '</');
						for (i=top.length-1; i>1; i--) {
							stack.push(htmlEncode(top[i]));
						}

						var attr = top[1];
						if (typeof attr === "object" && !(attr instanceof Array)) {
							stack.push('>');
							stack.push(attr);
						} else {
							stack.push(attr);
							stack.push('>');
						}
					}
				}
			} else if (typeof top === "object") {
				for (name in top) {
					if (top.hasOwnProperty(name)) {
						output.push(" ", name);
						var val = top[name];
						if (val) {
							output.push('="', attrEncode(val), '"');
						}
					}
				}
			} else {
				output.push(top);
			}
		}

		return output.join("");
	}

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
		 * @param {function} filter JsonML filter function
		 * @returns {Object}
		 */
		this.toDOM = function(filter) {
			return JsonML.parse(view, filter);
		};

		/**
		 * Returns result as HTML text
		 * 
		 * @this {Result}
		 * @returns {string}
		 */
		this.toString = function() {
			return view ? render(view) : "";
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
			return new Result(visit(view, model));
		};
	}

	/**
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} view The view template
	 * @returns {Template}
	 */
	return function(view) {
		return (view instanceof Template) ? view : new Template(view);
	};
})();
