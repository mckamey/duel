	/* factory.js --------------------*/

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var DUEL_EXTERN = "duel";

	/**
	 * @private
	 * @const
	 * @type {string}
	 */
	var RAW_EXTERN = "raw";

	/**
	 * Renders an error as text
	 * 
	 * @private
	 * @param {Error} ex The exception
	 * @return {string}
	 */
	function onError(ex) {
		return "["+ex+"]";
	}

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
			view = ["", view];
		}

		/**
		 * Binds and wraps the result
		 * 
		 * @public
		 * @param {*} model The data item being bound
		 * @return {Result}
		 */
		var self = function(model) {
			try {
				// Closure Compiler type cast
				var result = bind(/** @type {Array} */(view), model, 0, 1);
				return new Result(result);
			} catch (ex) {
				// handle error with context
				return onError(ex);
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
	 * @return {function(*)}
	 */
	var duel = window[DUEL_EXTERN] = function(view) {
		return (isFunction(view) && isFunction(view.getView)) ? view : factory(view);
	};

	/**
	 * @public
	 * @param {string} value Markup text
	 * @return {Markup}
	 */
	duel[RAW_EXTERN] = duel.raw = function(/*string*/ value) {
		return new Markup(value);
	};

