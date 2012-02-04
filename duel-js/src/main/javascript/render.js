	/* render.js --------------------*/
	
	/**
	 * Void tag lookup
	 *  
	 * @private
	 * @constant
	 * @type {Object.<boolean>}
	 */
	var VOID_TAGS = {
		'area' : true,
		'base' : true,
		'basefont' : true,
		'br' : true,
		'col' : true,
		'frame' : true,
		'hr' : true,
		'img' : true,
		'input' : true,
		'isindex' : true,
		'keygen' : true,
		'link' : true,
		'meta' : true,
		'param' : true,
		'source' : true,
		'wbr' : true
	};

	/**
	 * Boolean attribute map
	 * 
	 * @private
	 * @constant
	 * @type {Object.<number>}
	 */
	var ATTR_BOOL = {
		'async': 1,
		'checked': 1,
		'defer': 1,
		'disabled': 1,
		'hidden': 1,
		'novalidate': 1,
		'formnovalidate': 1
		// can add more attributes here as needed
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
						return '&amp;';
					case '<':
						return '&lt;';
					case '>':
						return '&gt;';
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
						return '&amp;';
					case '<':
						return '&lt;';
					case '>':
						return '&gt;';
					case '"':
						return '&quot;';
					default:
						return ch;
				}
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
						if (ATTR_BOOL[name]) {
							if (val) {
								val = name;
							} else {
								// falsey boolean attributes must not be present
								continue;
							}
						}

						buffer.append(' ', name);
						if (getType(val) !== NUL) {
							// Closure Compiler type cast
							buffer.append('="', /** @type{string} */(attrEncode(val)), '"');
						}
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
				// Closure Compiler type cast
				buffer.append(/** @type{string} */(htmlEncode(child)));
			}
		}

		if (tag && !isVoid) {
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

