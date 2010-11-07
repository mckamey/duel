	/* bind.js --------------------*/

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var FOR = "$for";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var XOR = "$xor";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var IF = "$if";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var CALL = "$call";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var PART = "$part";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var TEST = "test";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var EACH = "each";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var IN = "in";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var VIEW = "view";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var MODEL = "model";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var INDEX = "index";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var COUNT = "count";

	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var NAME = "name";

	var bind;

	/**
	 * Appends a node to a parent
	 * 
	 * @private
	 * @param {Array} parent The parent node
	 * @param {Array|Object|string|number} child The child node
	 */
	function append(parent, child) {
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
	 * Binds the child nodes ignoring parent element and attributes
	 * 
	 * @private
	 * @param {Array} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function bindContent(node, model, index, count, parts) {
		// second item might be attributes object
		var hasAttr = (getType(node[1]) === OBJ);

		if (node.length === (hasAttr ? 3 : 2)) {
			// unwrap single nodes
			return bind(node[node.length-1], model, index, count, parts);
		}

		// element array, make a doc frag
		var result = [""];

		for (var i=hasAttr ? 2 : 1, length=node.length; i<length; i++) {
			append(result, bind(node[i], model, index, count, parts));
		}

		return result;
	}

	/**
	 * Binds the content once for each item in model
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,number,number):*} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function loop(node, model, index, count, parts) {
		var args = node[1] || {},
			obj = args[IN],
			each,
			result = [""];

		// first rule out for-in loop
		if (typeof obj !== "undefined") {
			if (isFunction(obj)) {
				// execute code block
				obj = obj(model, index, count);
			}
			if (getType(obj) === OBJ) {
				// iterate over the properties
				each = [];
				for (var key in obj) {
					if (obj.hasOwnProperty(key)) {
						each.push({ key: key, value: obj[key] });
					}
				}
			} else {
				each = obj;
			}
		} else {
			each = args[EACH];
			if (isFunction(each)) {
				// execute code block
				each = each(model, index, count);
			}
		}

		if (getType(each) === ARY) {
			// iterate over the items
			for (var i=0, length=each.length; i<length; i++) {
				// Closure Compiler type cast
				append(result, bindContent(/** @type {Array} */(node), each[i], i, length, parts));
			}
		} else {
			// just bind the single value
			// Closure Compiler type cast
			result = bindContent(/** @type {Array} */(node), each, 0, 1, parts);
		}

		return result;
	}

	/**
	 * Binds the node to the first conditional block that evaluates to true
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function xor(node, model, index, count, parts) {
		for (var i=1, length=node.length; i<length; i++) {

			var block = node[i],
				args = block[1],
				test = args[TEST];

			if (getType(block[1]) === OBJ && test) {
				// execute test if exists
				if (isFunction(test)) {
					test = test(model, index, count);
				}

				if (!test) {
					continue;
				}
			}

			// process block contents
			return bindContent(block, model, index, count, parts);
		}

		return null;
	}

	/**
	 * Calls into another view
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,*,*):(Object|null)} node The template subtree root
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
			c = /** @type {number} */ (bind(args[COUNT], model, index, count)),
			p = {};

		// check for view parts
		for (var j=2, length=node.length; j<length; j++) {
			var block = node[j];
				args = block[1] || {};

			if (args && args[NAME]) {
				p[args[NAME]] = block;
			}
		}

		return (v && isFunction(v.getView)) ?
			bind(v.getView(), m, i, c, p) : null;
	}

	/**
	 * Replaces a part place holder with the named part from the calling view
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,*,*):(Object|null)} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function part(node, model, index, count, parts) {
		var args = node[1] || {},
			block = args[NAME];

		if (!parts || !parts[block]) {
			block = node;
		} else {
			block = parts[block];
		}

		return bindContent(block, model, index, count);
	}

	/**
	 * Binds the node to model
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,*,*):(Object|null)} node The template subtree root
	 * @param {*} model The data item being bound
	 * @param {number|string} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	bind = function(node, model, index, count, parts) {
		/**
		 * @type {Array|Object|string|number}
		 */
		var result;
	
		switch (getType(node)) {
			case FUN:
				// execute code block
				// Closure Compiler type cast
				result = (/** @type {function(*,*,*):(Object|null)} */ (node))(model, index, count);
				break;

			case ARY:
				// inspect element name for template commands
				/**
				 * @type {string}
				 */
				var tag = node[0] || "";
				switch (tag) {
					case FOR:
						result = loop(node, model, index, count, parts);
						break;
					case XOR:
						result = xor(node, model, index, count, parts);
						break;
					case IF:
						result = xor([XOR, node], model, index, count, parts);
						break;
					case CALL:
						// parts not needed when calling another view
						result = call(node, model, index, count);
						break;
					case PART:
						result = part(node, model, index, count, parts);
						break;
					default:
						// element array, first item is name
						result = [tag];
	
						for (var i=1, length=node.length; i<length; i++) {
							append(result, bind(node[i], model, index, count, parts));
						}
						break;
				}
				break;

			case OBJ:
				// attribute map
				result = {};
				for (var key in node) {
					if (node.hasOwnProperty(key)) {
						// parts not needed when binding attributes
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

