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
	 * @const
	 * @type {string}
	 */
	var WRITE_EXTERN = "write";

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

		var tag = node[0] || "",
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
						buffer.append(' ', name);
						var val = child[name];
						if (getType(val) !== NUL) {
							buffer.append('="', attrEncode(val), '"');
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
				buffer.append(htmlEncode(child));
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
	 * @public
	 * @param {Array|Object|string|number|function(*,*,*,*):(Object|null)} view The view to replace
	 * @param {*} data The data item being bound
	 * @param {number} index The index of the current data item
	 * @param {number} count The total number of data items
	 * @param {string|null} key The current property name
	 */
	duel[WRITE_EXTERN] = duel.write = function(view, data, index, count, key) {
		// bind node
		view = duel(view).getView();
		// Closure Compiler type cast
		view = bind(/** @type {Array} */(view), data, index, count, key);
		/*jslint evil:true*/
		document.write(render(view));
		/*jslint evil:false*/
	};

