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

