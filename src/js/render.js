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
	 * @private
	 * @param {Array} view The compiled view
	 * @return {string}
	 */
	 function render(view) {
		var output = [];
		renderElem(output, view);
		return output.join("");
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

