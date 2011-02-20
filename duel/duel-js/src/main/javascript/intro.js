/*global window */

/**
 * @fileoverview duel.js: client-side template engine
 * 
 * http://duelengine.org
 * 
 * Copyright (c) 2006-2010 Stephen M. McKamey
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
	 */
	function(window) {

	"use strict";

	/**
	 * @type {Document} document Document reference
	 */
	var document = window.document;

