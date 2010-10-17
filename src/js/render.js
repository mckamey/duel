	/* render.js --------------------*/
	
	/**
	 * Void tag lookup 
	 * @constant
	 * @type {Object.<boolean>}
	 */
	var VOID_TAGS = (function(names) {
			var tags = {};
			while (names.length) {
				tags[names.pop()] = true;
			}
			return tags;
		})("area,base,basefont,br,col,frame,hr,img,input,isindex,keygen,link,meta,param,source,wbr".split(','));
	
	/**
	 * Encodes invalid literal characters in strings
	 * 
	 * @param {Array|Object|string|number} val The value
	 * @returns {Array|Object|string|number}
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
	 * @param {Array|Object|string|number} val The value
	 * @returns {Array|Object|string|number}
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
	 * @param {Array} output The output container
	 * @param {Array} node The result tree
	 */
	function renderElem(output, node) {
	
		var tag = node[0],
			length = node.length,
			i = 1,
			child;
	
		if (tag) {
			// emit open tag
			output.push('<', tag);
	
			child = node[i];
			if (getType(child) === OBJ) {
				// emit attributes
				for (var name in child) {
					if (child.hasOwnProperty(name)) {
						output.push(' ', name);
						var val = child[name];
						if (val) {
							output.push('="', attrEncode(val), '"');
						}
					}
				}
				i++;
			}
			output.push('>');
		}
	
		// emit children
		for (; i<length; i++) {
			child = node[i];
			if (getType(child) === ARY) {
				renderElem(output, child);
			} else {
				// encode string literals
				output.push(htmlEncode(child));
			}
		}
	
		if (tag && !VOID_TAGS[tag]) {
			// emit close tag
			output.push('</', tag, '>');
		}
	}
	
	/**
	 * Renders the result as a string
	 * 
	 * @param {Array} view The compiled view
	 * @returns {string}
	 */
	 function render(view) {
		var output = [];
		renderElem(output, view);
		return output.join("");
	}
	
	/**
	 * Returns result as HTML text
	 * 
	 * @override
	 * @this {Result}
	 * @returns {string}
	 */
	Result.prototype.toString = function() {
		return render(this.value);
	};
	
	/**
	 * Returns result as HTML text
	 * 
	 * @override
	 * @this {Result}
	 * @returns {string}
	 */
	View.prototype.toString = function() {
		return render(this.value);
	};

