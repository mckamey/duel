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
	 * @private
	 * @constant
	 * @type {string}
	 */
	var MSIE = "ScriptEngineMajorVersion";
	
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

	/**
	 * Encodes invalid literal characters in strings
	 * 
	 * @private
	 * @param {Array|Object|string|number} val The value
	 * @return {Array|Object|string|number}
	 */
	function htmlEncode(val) {
		if (!isString(val)) {
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
		if (!isString(val)) {
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
	 * @param {Buffer} buffer The output buffer
	 * @param {Array} node The result tree
	 */
	function renderElem(buffer, node) {

		var tag = node[0],
			length = node.length,
			i = 1,
			child;

		if (tag) {
			// emit open tag
			buffer.append('<', tag);

			child = node[i];
			if (getType(child) === OBJ) {
				// emit attributes
				for (var name in child) {
					if (child.hasOwnProperty(name)) {
						buffer.append(' ', name);
						var val = child[name];
						if (getType(val) !== NUL) {
							buffer.append('="', attrEncode(val), '"');
						}
					}
				}
				i++;
			}
			buffer.append('>');
		}

		// emit children
		for (; i<length; i++) {
			child = node[i];
			if (getType(child) === ARY) {
				renderElem(buffer, child);
			} else {
				// encode string literals
				buffer.append(htmlEncode(child));
			}
		}

		if (tag && !VOID_TAGS[tag]) {
			// emit close tag
			buffer.append('</', tag, '>');
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
		try {
			var buffer = new Buffer();
			renderElem(buffer, view);
			return buffer.toString();
		} catch (ex) {
			// handle error with context
			return onError(ex);
		}
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

