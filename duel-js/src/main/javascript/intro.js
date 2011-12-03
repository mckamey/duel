/*global window */

/**
 * @fileoverview duel.js: client-side engine
 * @version DUEL v@duel-version@ http://duelengine.org
 * 
 * Copyright (c) 2006-2011 Stephen M. McKamey
 * Licensed under the MIT License (http://duelengine.org/license.txt)
 */

/**
 * @public
 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
 * @return {function(*)}
 */
var duel = (
	/**
	 * @param {Window} window Window reference
	 * @param {Document} document Document reference
	 * @param {*=} undef undefined
	 */
	function(window, document, undef) {

	'use strict';

