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

