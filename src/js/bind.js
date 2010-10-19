	/* bind.js --------------------*/
	
	/**
	 * @private
	 * @constant
	 * @type {string}
	 */
	var BIND_EXTERN = "bind";

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
	var CHOOSE = "$choose";

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
	var ELSE = "$else";

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


