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
	 * @public
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
	 * @returns {View}
	 */
	var duel = window[DUEL_EXTERN] = function(view) {
		return (view instanceof View) ? view : new View(view);
	};
	
	/**
	 * @public
	 * @param {string} value Markup text
	 * @returns {Markup}
	 */
	duel[RAW_EXTERN] = duel.raw = function(/*string*/ value) {
		return new Markup(value);
	};
