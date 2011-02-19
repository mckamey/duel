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
	 * @private
	 * @constant
	 * @type {string}
	 */
	var MSIE = "ScriptEngineMajorVersion";

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
	 * Determines if the value is an Array
	 * 
	 * @private
	 * @param {*} val the object being tested
	 * @return {boolean}
	 */
	var isArray = Array.isArray || function(val) {
		return (val instanceof Array);
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
				return !val ? NUL : (isArray(val) ? ARY : ((val instanceof Markup) ? RAW : ((val instanceof Date) ? VAL : OBJ)));
			case "function":
				return FUN;
			case "undefined":
				return NUL;
			default:
				return VAL;
		}
	}

	/**
	 * Determines if the value is a string
	 * 
	 * @private
	 * @param {*} val the object being tested
	 * @return {boolean}
	 */
	function isString(val) {
		return (typeof val === "string");
	}

	/**
	 * Determines if the value is a function
	 * 
	 * @private
	 * @param {*} val the object being tested
	 * @return {boolean}
	 */
	function isFunction(val) {
		return (typeof val === "function");
	}

	/**
	 * String buffer
	 * 
	 * @private
	 * @this {Buffer}
	 * @constructor
	 */
	function Buffer() {
		/**
		 * @type {Array|string}
		 * @private
		 */
		this.value = Buffer.FAST ? "" : [];
	}

	/**
	 * @private
	 * @constant
	 * @type {boolean}
	 */
	Buffer.FAST = !window[MSIE];

	/**
	 * Appends to the internal value
	 * 
	 * @public
	 * @this {Buffer}
	 * @param {string} v1
	 * @param {string} v2
	 * @param {string} v3
	 */
	Buffer.prototype.append = function(v1, v2, v3) {
		if (Buffer.FAST) {
			this.value += v1;

			/*jslint eqeqeq: false */
			if (v2 != null) {
				this.value += v2;

				if (v3 != null) {
					this.value += v3;
				}
			}
			/*jslint eqeqeq: true */
		} else {
			this.value.push.apply(
				// Closure Compiler type cast
				/** @type{Array} */(this.value),
				arguments);
		}
	};

	/**
	 * Clears the internal value
	 * 
	 * @public
	 * @this {Buffer}
	 */
	Buffer.prototype.clear = function() {
		this.value = Buffer.FAST ? "" : [];
	};

	/**
	 * Renders the value
	 * 
	 * @public
	 * @override
	 * @this {Buffer}
	 * @return {string} value
	 */
	Buffer.prototype.toString = function() {
		return Buffer.FAST ?
			// Closure Compiler type cast
			/** @type{string} */(this.value) :
			this.value.join("");
	};

	function digits(n) {
        return (n < 10) ? '0'+n : n;
    }

	/**
	 * Formats the value as a string
	 * 
	 * @private
	 * @param {*} val the object being tested
	 * @return {string|null}
	 */
	function asString(val) {
		var buffer, needsDelim;
		switch (getType(val)) {
			case VAL:
				if (val instanceof Date) {
					// YYYY-MM-DD HH:mm:ss Z
					return val.getUTCFullYear()+'-'+
						digits(val.getUTCMonth()+1)+'-'+
						digits(val.getUTCDate())+' '+
						digits(val.getUTCHours())+':'+
						digits(val.getUTCMinutes())+':'+
						digits(val.getUTCSeconds())+" Z";
				}
				return ""+val;
			case NUL:
				return "";
			case ARY:
				// flatten into simple list
				buffer = new Buffer();
				for (var i=0, length=val.length; i<length; i++) {
					if (needsDelim) {
						buffer.append(", ");
					} else {
						needsDelim = true;
					}
					buffer.append(asString(val[i]));
				}
				return buffer.toString();
			case OBJ:
				// format JSON-like
				buffer = new Buffer();
				buffer.append('{');
				for (var key in val) {
					if (val.hasOwnProperty(key)) {
						if (needsDelim) {
							buffer.append(", ");
						} else {
							needsDelim = true;
						}
						buffer.append(key, '=', asString(val[key]));
					}
				}
				buffer.append('}');
				return buffer.toString();
		}

		// Closure Compiler type cast
		return /** @type{string} */(val);
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
		if (!isArray(view)) {
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

