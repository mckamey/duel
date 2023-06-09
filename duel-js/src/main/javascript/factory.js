	/* factory.js --------------------*/

	/**
	 * Renders an error directly as text
	 * 
	 * @private
	 * @param {Error} ex The exception
	 * @return {string|Result}
	 */
	var onError = function(ex) {
		return '[ '+ex+' ]';
	};

	/**
	 * Wraps a view definition with binding method
	 * 
	 * @private
	 * @param {Array|Object|string|number} view The template definition
	 * @return {function(*)}
	 */
	function factory(view) {
		if (getType(view) !== ARY) {
			// ensure is rooted element
			view = ['', view];
		}

		/**
		 * Binds and wraps the result
		 * 
		 * @public
		 * @param {*} data The data item being bound
		 * @param {number} index The index of the current data item
		 * @param {number} count The total number of data items
		 * @param {string|null} key The current property name
		 * @return {Result}
		 */
		var self = function(data, index, count, key) {
			try {
				var result = bind(
					// Closure Compiler type cast
					/** @type {Array} */(view),
					data,
					isFinite(index) ? index : 0,
					isFinite(count) ? count : 1,
					isString(key) ? key : null);
				return new Result(result);

			} catch (ex) {
				// handle error with context
				var errValue = onError(ex);

				if (errValue instanceof Result) {
					return errValue;

				} else {
					// render the error as a text node
					return new Result(''+errValue);
				}
			}
		};

		/**
		 * Gets the internal view definition
		 * 
		 * @private
		 * @return {Array}
		 */
		self.getView = function() {
			// Closure Compiler type cast
			return /** @type {Array} */(view);
		};

		return self;
	}

	/**
	 * @public
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
	 * @return {Array|Object|string|number}
	 */
	var duel = function(view) {
		return (isFunction(view) && isFunction(view.getView)) ? view : factory(view);
	};

	/**
	 * @public
	 * @param {string} value error callback
	 */
	duel.onerror = function(value) {
		if (isFunction(value)) {
			onError = value;
		}
	};

	/**
	 * @public
	 * @param {string} value onbind filter callback
	 */
	duel.onbind = function(value) {
		if (isFunction(value)) {
			bindFilter = value;
		}
	};

	/**
	 * @public
	 * @param {string} value Markup text
	 * @return {Markup}
	 */
	duel.raw = function(value) {
		return new Markup(value);
	};

