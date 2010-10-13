/**
 * @fileoverview duel: client-side template engine
 */

var duel = (function() {

	/**
	 * Appends a node to a parent
	 * 
	 * @param {Array} parent The parent node
	 * @param {Array|Object|string} child The child node
	 */
	function append(parent, child) {
		if (!(parent instanceof Array) || typeof child === "undefined" || child === null) {
			// ignore
			return;
		}

		if (child instanceof Array) {
			if (child[0] === "") {
				// child is multiple sub-trees (i.e. documentFragment)
				child.shift();// remove fragment identifier

				// directly append children
				while (child.length) {
					append(parent, child.shift());
				}
			} else {
				// child is an element array
				parent.push(child);
			}

		} else if (typeof child === "object") {
			// child is attributes object
			var old = parent[1];
			if (!old || old instanceof Array || typeof old !== "object") {
				// insert attributes
				var name = parent.shift();
				parent.unshift(child);
				parent.unshift(name || "");
			} else {
				// merge attribute objects
				for ( var key in child) {
					if (child.hasOwnProperty(key)) {
						old[key] = child[key];
					}
				}
			}

		} else {
			// convert primitive to string literal
			child = "" + child;

			var last = parent.length - 1;
			if (last > 0 && typeof parent[last] === "string") {
				// combine string literals
				parent[last] += child;
			} else {
				// append
				parent.push(child);
			}
		}
	}

	/**
	 * Binds the node once for each item in model
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node
	 *            The template subtree root
	 * @param {*} model The data item being bound
	 * @returns {Array|Object|string}
	 */
	function foreach(node, model, index, count) {

		var args = node[1];
		if (!args || !args.each) {
			return null;
		}

		var output = [""];
		// execute code block
		var items = (typeof args.each !== "function") ?
			args.each :
			args.each(model, index, count);
		node = node.slice(2);
		node.unshift("");

		if (items instanceof Array) {
			for (var i = 0, length = items.length; i < length; i++) {
				append(output, visit(node, items[i], i, length));
			}
		} else if (typeof items === "object" && items) {
			for (var key in items) {
				if (items.hasOwnProperty(key)) {
					append(output, visit(node, items[key], key, NaN));
				}
			}
		}

		return output;
	}

	/**
	 * Binds the node to the first child block which evaluates to true
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node
	 *            The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number=} index The index of the current data item
	 * @param {number=} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	function choose(node, model, index, count) {
		for (var i = 1, length = node.length; i < length; i++) {
			var block = node[i], cmd = block[0], args = block[1];

			if (cmd === "$if" && args && args.test) {
				var test = (typeof args.test !== "function") ? args.test : args
				        .test(model, index, count);
				if (test) {
					// clone and bind block
					block = block.slice(2);
					block.unshift("");
					return visit(block, model, index, count);
				}
			} else if (cmd === "$else") {
				// clone and bind block
				block = block.slice(1);
				block.unshift("");
				return visit(block, model, index, count);
			}
		}

		return null;
	}

	/**
	 * Binds the node to model
	 * 
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} node
	 *            The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number=|string=} index The index of the current data item
	 * @param {number=} count The total number of data items
	 * @returns {Array|Object|string}
	 */
	function visit(node, model, index, count) {
		var output, length, i;

		if (typeof node === "function") {
			// execute code block
			output = node(model, index, count);

			if (output instanceof Template) {
				// allow recursively binding templates
				// useful for creating "switcher" methods
				output = Template.bind(model, index, count);
			}
		}

		// visit each child
		else if (node instanceof Array) {
			// inspect element name for template commands
			switch (node[0]) {
				case "$for":
					output = foreach(node, model, index, count);
					break;
				case "$choose":
					output = choose(node, model, index, count);
					break;
				case "$if":
					output = choose([
					        "$choose", node
					], model, index, count);
					break;
				default:
					// element array, first item is name
					output = [
						node[0]
					];

					for (i = 1, length = node.length; i < length; i++) {
						append(output, visit(node[i], model, index, count));
					}
					break;
			}

		} else if (typeof node === "object" && node) {
			// attribute object
			output = {};
			for (var key in node) {
				if (node.hasOwnProperty(key)) {
					output[key] = visit(node[key], model, index, count);
				}
			}

		} else {
			// ensure string
			output = "" + node;
		}

		return output;
	}

	function voidTag(tag) {
		switch (tag)
		{
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

	function htmlEncode(val) {
		return (""+val)
			.split('&').join('&amp;')
			.split('<').join('&lt;')
			.split('>').join('&gt;');
	}

	function attrEncode(val) {
		return (""+val)
			.split('&').join('&amp;')
			.split('<').join('&lt;')
			.split('>').join('&gt;')
			.split('"').join('&quot;')
			.split("'").join('&apos;');
	}

	function render(view) {
		var stack = [view],
			output = [],
			name;

		while (stack.length) {
			var top = stack.pop();

			if (top instanceof Array) {
				name = top.shift();
				if (name) {
					output.push('<', name);
					if (voidTag(name)) {
						stack.push(' />');
						stack = stack.concat(top.reverse());
					} else {
						stack.push('>', name, '</');
						stack = stack.concat(top.reverse());

						var attr = stack.pop();
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
	
	function Result(view) {
		this.toDOM = function(filter) {
			return JsonML.parse(view, filter);
		};

		this.toString = function() {
			return view ? render(view) : "";
		};
	}

	function Template(view) {
		if ("undefined" === typeof view) {
			throw new Error("View is undefined");
		}

		this.bind = function(model) {
			return new Result( visit(view, model) );
		};
	}

	/**
	 * @param {Array|Object|string|function(*,number,number):Array|Object|string} view
	 *			The view template
	 */
	return function(view) {
		return (view instanceof Template) ? view : new Template(view);
	};
})();
