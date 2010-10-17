	/* factory.js --------------------*/
	
	/**
	 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
	 * @returns {View}
	 */
	var duel = function(view) {
		return (view instanceof View) ? view : new View(view);
	};
	
	/**
	 * @param {string} value Markup text
	 * @returns {Markup}
	 */
	duel.raw = function(/*string*/ value) {
		return new Markup(value);
	};
