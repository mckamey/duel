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
	 * @type {boolean}
	 */
	Buffer.FAST = (window.navigator.userAgent.indexOf('MSIE') < 0);

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

			if (typeof v2 !== "undefined") {
				this.value += v2;

				if (typeof v3 !== "undefined") {
					this.value += v3;
				}
			}
		} else {
			this.value.push(v1);

			if (typeof v2 !== "undefined") {
				this.value.push(v2);

				if (typeof v3 !== "undefined") {
					this.value.push(v3);
				}
			}
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
		if (typeof val !== "string") {
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
		if (typeof val !== "string") {
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
						if (val) {
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
		var buffer = new Buffer();
		renderElem(buffer, view);
		return buffer.toString();
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
	
	/**
	 * Returns result as HTML text
	 * 
	 * @public
	 * @override
	 * @this {Result}
	 * @return {string}
	 */
	View.prototype.toString = function() {
		return render(this.value);
	};

