/*global window */

/**
 * @license DUEL v@duel-version@ http://duelengine.org
 * Copyright (c)2006-2012 Stephen M. McKamey.
 * Licensed under The MIT License.
 */
/*jshint smarttabs:true */

/**
 * @public
 * @param {Array|Object|string|number|function(*,number,number):Array|Object|string} view The view template
 * @return {function(*)}
 */
var duel = (
	/**
	 * @param {Document} document Document reference
	 * @param {function()} scriptEngine script engine version
	 * @param {*=} undef undefined
	 */
	function(document, scriptEngine, undef) {

	'use strict';

