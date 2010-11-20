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
	var DATA = "data";

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
	var KEY = "key";

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
				var last = parent.length-1;
				if (last > 0 && getType(parent[last]) === VAL) {
					// combine string literals
					parent[last] = "" + parent[last] + child;
				} else if (child !== "") {
					// convert primitive to string literal and append
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
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function bindContent(node, data, index, count, key, parts) {
		// second item might be attributes object
		var hasAttr = (getType(node[1]) === OBJ);

		if (node.length === (hasAttr ? 3 : 2)) {
			// unwrap single nodes
			return bind(node[node.length-1], data, index, count, key, parts);
		}

		// element array, make a doc frag
		var result = [""];

		for (var i=hasAttr ? 2 : 1, length=node.length; i<length; i++) {
			append(result, bind(node[i], data, index, count, key, parts));
		}

		return result;
	}

	/**
	 * Binds the content once for each item in data
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,number,number):*} node The template subtree root
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function loop(node, data, index, count, key, parts) {
		var args = node[1] || {},
			result = [""],
			items, i, length;

		if (args.hasOwnProperty(COUNT)) {
			// evaluate for-count loop
			length = args[COUNT];
			if (isFunction(length)) {
				// execute code block
				length = length(data, index, count, key);
			}

			var d;
			if (args.hasOwnProperty(DATA)) {
				d = args[DATA];
				if (isFunction(d)) {
					// execute code block
					d = d(data, index, count, key);
				}
			} else {
				d = data;
			}

			// iterate over the items
			for (i=0; i<length; i++) {
				// Closure Compiler type cast
				append(result, bindContent(/** @type {Array} */(node), d, i, length, null, parts));
			}
			return result;
		}

		if (args.hasOwnProperty(IN)) {
			// convert for-in loop to for-each loop
			var obj = args[IN];
			if (isFunction(obj)) {
				// execute code block
				obj = obj(data, index, count, key);
			}
			if (getType(obj) === OBJ) {
				// accumulate the property keys to get count
				items = [];
				for (var k in obj) {
					if (obj.hasOwnProperty(k)) {
						items.push(k);
					}
				}

				// iterate over the keys
				for (i=0, length=items.length; i<length; i++) {
					// Closure Compiler type cast
					append(result, bindContent(/** @type {Array} */(node), obj[items[i]], i, length, items[i], parts));
				}
				return result;
			}

			// just bind to single value
			items = obj;
		} else {
			// evaluate for-each loop
			items = args[EACH];
			if (isFunction(items)) {
				// execute code block
				items = items(data, index, count, key);
			}
		}

		var type = getType(items); 
		if (type === ARY) {
			// iterate over the items
			for (i=0, length=items.length; i<length; i++) {
				// Closure Compiler type cast
				append(result, bindContent(/** @type {Array} */(node), items[i], i, length, null, parts));
			}
		} else if (type !== NUL) {
			// just bind the single value
			// Closure Compiler type cast
			result = bindContent(/** @type {Array} */(node), items, 0, 1, null, parts);
		}

		return result;
	}

	/**
	 * Binds the node to the first conditional block that evaluates to true
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} node The template subtree root
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function xor(node, data, index, count, key, parts) {
		for (var i=1, length=node.length; i<length; i++) {

			var block = node[i],
				args = block[1],
				test = args[TEST];

			if (getType(block[1]) === OBJ && test) {
				// execute test if exists
				if (isFunction(test)) {
					test = test(data, index, count, key);
				}

				if (!test) {
					continue;
				}
			}

			// process block contents
			return bindContent(block, data, index, count, key, parts);
		}

		return null;
	}

	/**
	 * Calls into another view
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,*,*,*):(Object|null)} node The template subtree root
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 * @return {Array|Object|string|number}
	 */
	function call(node, data, index, count, key) {
		var args = node[1];
		if (!args || !args[VIEW]) {
			return null;
		}

		// evaluate the arguments
		var v = bind(args[VIEW], data, index, count, key),
			d = args.hasOwnProperty(DATA) ? bind(args[DATA], data, index, count, key) : data,
			i = args.hasOwnProperty(INDEX) ? bind(args[INDEX], data, index, count, key) : index,
			c = args.hasOwnProperty(COUNT) ? bind(args[COUNT], data, index, count, key) : count,
			k = args.hasOwnProperty(KEY) ? bind(args[KEY], data, index, count, key) : key,
			p = {};

		// check for view parts
		for (var j=node.length-1; j>=2; j--) {
			var block = node[j];

			args = block[1] || {};
			if (args && args[NAME]) {
				p[args[NAME]] = block;
			}
		}

		return (v && isFunction(v.getView)) ?
			// Closure Compiler type cast
			bind(v.getView(), d, /** @type {number} */i, /** @type {number} */c, /** @type {String} */k, p) : null;
	}

	/**
	 * Replaces a place holder part with the named part from the calling view
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,*,*,*):(Object|null)} node The template subtree root
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	function part(node, data, index, count, key, parts) {
		var args = node[1] || {},
			block = args[NAME];

		block = parts && parts.hasOwnProperty(block) ? parts[block] : node;

		return bindContent(block, data, index, count, key);
	}

	/**
	 * Binds the node to data
	 * 
	 * @private
	 * @param {Array|Object|string|number|function(*,*,*,*):(Object|null)} node The template subtree root
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 * @param {Object=} parts Named replacement partial views
	 * @return {Array|Object|string|number}
	 */
	bind = function(node, data, index, count, key, parts) {

		switch (getType(node)) {
			case FUN:
				// execute code block
				// Closure Compiler type cast
				return (/** @type {function(*,*,*,*):(Object|null)} */ (node))(data, index, count, key);

			case ARY:
				// inspect element name for template commands
				/**
				 * @type {string}
				 */
				var tag = node[0] || "";
				switch (tag) {
					case FOR:
						return loop(node, data, index, count, key, parts);

					case XOR:
						return xor(node, data, index, count, key, parts);

					case IF:
						return xor([XOR, node], data, index, count, key, parts);

					case CALL:
						// parts not needed when calling another view
						return call(node, data, index, count, key);

					case PART:
						return part(node, data, index, count, key, parts);
				}

				// element array, first item is name
				var elem = [tag];
				for (var i=1, length=node.length; i<length; i++) {
					append(elem, bind(node[i], data, index, count, key, parts));
				}
				return elem;

			case OBJ:
				// attribute map
				var attr = {};
				for (var k in node) {
					if (node.hasOwnProperty(k)) {
						// parts not needed when binding attributes
						attr[k] = bind(node[k], data, index, count, key);
					}
				}
				return attr;
		}

		// literal values
		return node;
	};

