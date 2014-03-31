	/* render.js --------------------*/

	/**
	 * Void tag lookup
	 *  
	 * @private
	 * @constant
	 * @type {Object.<number>}
	 */
	var VOID_TAGS = {
		'area': 1,
		'base': 1,
		'basefont': 1,
		'br': 1,
		'col': 1,
		'frame': 1,
		'embed': 1,
		'hr': 1,
		'img': 1,
		'input': 1,
		'isindex': 1,
		'keygen': 1,
		'link': 1,
		'menuitem': 1,
		'meta': 1,
		'param': 1,
		'source': 1,
		'track': 1,
		'wbr': 1

		// update elements as spec changes
		// http://www.w3.org/TR/html51/single-page.html#void-elements
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
		this.value = Buffer.FAST ? '' : [];
	}

	/**
	 * IE<9 benefits from Array.join() for large strings
	 * 
	 * @private
	 * @constant
	 * @type {boolean}
	 */
	Buffer.FAST = !(scriptEngine && scriptEngine() < 9);

	/**
	 * Appends to the internal value
	 * 
	 * @public
	 * @this {Buffer}
	 * @param {string} a
	 * @param {string=} b
	 * @param {string=} c
	 */
	Buffer.prototype.append = function(a, b, c) {
		var args = arguments;

		if (Buffer.FAST) {
			var len = args.length;
			if (len > 1) {
				if (len > 2) {
					b += c;
				}
				a += b;
			}
			this.value += a;

		} else {
			this.value.push.apply(
				// Closure Compiler type cast
				/** @type{Array} */(this.value),
				args);
		}
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
			this.value.join('');
	};

//	/**
//	 * Resets the internal value
//	 * 
//	 * @public
//	 * @this {Buffer}
//	 */
//	Buffer.prototype.clear = function() {
//		this.value = Buffer.FAST ? '' : [];
//	};

	/**
	 * Encodes invalid literal characters in strings
	 * 
	 * @private
	 * @param {Array|Object|string|number} val The value
	 * @return {string}
	 */
	function htmlEncode(val) {
		if (!isString(val)) {
			return (val !== null && val !== undef) ? ''+val : '';
		}

		var map = {
			'&': '&amp;',
			'<': '&lt;',
			'>': '&gt;'
		};

		return val.replace(/[&<>]/g, function(ch) {
			return map[ch] || ch;
		});
	}

	/**
	 * Encodes invalid attribute characters in strings
	 * 
	 * @private
	 * @param {Array|Object|string|number} val The value
	 * @return {string}
	 */
	function attrEncode(val) {
		if (!isString(val)) {
			return (val !== null && val !== undef) ? ''+val : '';
		}

		var map = {
			'&': '&amp;',
			'<': '&lt;',
			'>': '&gt;',
			'"': '&quot;'
		};

		return val.replace(/[&<>"]/g, function(ch) {
			return map[ch] || ch;
		});
	}

	/**
	 * Renders the comment as a string
	 * 
	 * @private
	 * @param {Buffer} buffer The output buffer
	 * @param {Array} node The result tree
	 */
	function renderComment(buffer, node) {
		if (node[0] === '!DOCTYPE') {
			// emit doctype
			buffer.append('<!DOCTYPE ', node[1], '>');

		} else {
			// emit HTML comment
			buffer.append('<!--', node[1], '-->');
		}
	}

	/**
	 * Renders the element as a string
	 * 
	 * @private
	 * @param {Buffer} buffer The output buffer
	 * @param {Array} node The result tree
	 */
	function renderElem(buffer, node) {

		var tag = node[0] || '',
			length = node.length,
			i = 1,
			child,
			isVoid = VOID_TAGS[tag];

		if (tag.charAt(0) === '!') {
			renderComment(buffer, node);
			return;
		}
		if (tag) {
			// emit open tag
			buffer.append('<', tag);

			child = node[i];
			if (getType(child) === OBJ) {
				// emit attributes
				for (var name in child) {
					if (child.hasOwnProperty(name)) {
						var val = child[name];
						if (ATTR_BOOL[name.toLowerCase()]) {
							if (val) {
								val = name;

							} else {
								// falsey boolean attributes must not be present
								continue;
							}
						}
						if (getType(val) === NUL) {
							// null/undefined removes attributes
							continue;
						}

						buffer.append(' ', name);
						// Closure Compiler type cast
						buffer.append('="', attrEncode(val), '"');
					}
				}
				i++;
			}
			if (isVoid) {
				buffer.append(' /');
			}
			buffer.append('>');
		}

		// emit children
		for (; i<length; i++) {
			child = node[i];
			if (isArray(child)) {
				renderElem(buffer, child);

			} else {
				// encode string literals
				buffer.append(htmlEncode(child));
			}
		}

		if (tag && !isVoid) {
			// emit close tag
			buffer.append('</', tag, '>');
		}

		return buffer;
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
			return renderElem(new Buffer(), view).toString();

		} catch (ex) {
			// handle error with context
			var errValue = onError(ex);

			if (errValue instanceof Result) {
				return render(errValue.value);

			} else {
				// render the error as a string
				return (''+errValue);
			}
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

	/**
	 * Immediately writes the resulting value to the document
	 * 
	 * @public
	 * @this {Result}
	 * @param {Document} doc optional Document reference
	 */
	Result.prototype.write = function(doc) {
		/*jslint evil:true*/
		(doc||document).write(''+this);
		/*jslint evil:false*/
	};

