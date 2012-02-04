try {

module('Result.toDOM()');

test('nested elements with attributes', function() {

	var view = duel(
		['div', { 'class' : 'download' },
			['h2',
			 	'Filename: Foo.js'
			],
			['p',
			 	'URL: ',
			 	['br'],
			 	['a', { 'href' : 'http://example.com/foo.js', 'target' : '_blank', 'title' : 'Lorem ipsum dolor sit amet' },
			 		'http://example.com/foo.js'
		 		],
			 	' (5.87KB)'
		 	],
		 	['hr'],
			['p',
			 	'Description: Lorem ipsum dolor sit amet'
			]
		]);

	var actual = view().toDOM();

	var temp, expected = document.createElement('div');
	expected.className = 'download';

	temp = document.createElement('h2');
	temp.appendChild(document.createTextNode('Filename: Foo.js'));
	expected.appendChild(temp);

	temp = document.createElement('p');
	temp.appendChild(document.createTextNode('URL: '));
	temp.appendChild(document.createElement('br'));
	var temp2 = document.createElement('a');
	temp2.setAttribute('href', 'http://example.com/foo.js');
	temp2.setAttribute('target', '_blank');
	temp2.setAttribute('title', 'Lorem ipsum dolor sit amet');
	temp2.appendChild(document.createTextNode('http://example.com/foo.js'));
	temp.appendChild(temp2);
	temp.appendChild(document.createTextNode(' (5.87KB)'));
	expected.appendChild(temp);

	expected.appendChild(document.createElement('hr'));

	temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Description: Lorem ipsum dolor sit amet'));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), '');
});

test('boolean attributes', function() {

	var view = duel(['form',
		['input', { 'type': 'checkbox', checked: false } ],
		['input', { 'type': 'checkbox', checked: '' } ],
		['input', { 'type': 'checkbox', checked: null } ],
		['input', { 'type': 'checkbox', checked: true } ],
		['input', { 'type': 'checkbox', checked: 'checked' } ],
		['input', { 'type': 'checkbox', checked: 'yes' } ]
	]);

	var actual = view().toDOM();

	var temp, expected = document.createElement('form');

	temp = document.createElement('input');
	temp.type = 'checkbox';
	expected.appendChild(temp);

	temp = document.createElement('input');
	temp.type = 'checkbox';
	expected.appendChild(temp);

	temp = document.createElement('input');
	temp.type = 'checkbox';
	expected.appendChild(temp);

	temp = document.createElement('input');
	temp.type = 'checkbox';
	temp.defaultChecked = true;
	expected.appendChild(temp);

	temp = document.createElement('input');
	temp.type = 'checkbox';
	temp.defaultChecked = 'checked';
	expected.appendChild(temp);

	temp = document.createElement('input');
	temp.type = 'checkbox';
	temp.defaultChecked = 'yes';
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), '');

	var inputs = actual.getElementsByTagName('input');
	for (var i=inputs.length-1; i>=0; i--) {
		same(inputs[i].checked, i>=3);
	}
});

test('docFrag root', function() {

	var view = duel(
		['',
		 	['p', 'Inner child one.'],
		 	['p', 'Inner child two.' ]
		]);

	var actual = view().toDOM();

	var expected = document.createDocumentFragment();

	var temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Inner child one.'));
	expected.appendChild(temp);

	temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Inner child two.'));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), '');
});

test('docFrag inner', function() {

	var view = duel(
		['div',
			['',
			 	['p', 'Inner child one.'],
			 	['p', 'Inner child two.']
			]
		]);

	var actual = view().toDOM();

	var expected = document.createElement('div');

	var temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Inner child one.'));
	expected.appendChild(temp);

	temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Inner child two.'));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), '');
});

test('markup data', function() {

	var view = duel(
		['div', { 'class' : 'test' },
			['p',
			 	'Description: ',
			 	duel.raw('<b>Lorem </b>'),
			 	duel.raw('<blink>ipsum</blink>'),
			 	' ',
			 	duel.raw('<i>dolor sit amet</i>')
			]
		]);

	var actual = view().toDOM();

	var expected = document.createElement('div');
	expected.className = 'test';

	var temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Description: '));

	var temp2 = document.createElement('div');
	temp2.innerHTML = '<b>Lorem </b>';
	while (temp2.firstChild) {
		temp.appendChild(temp2.firstChild);
	}
	temp2.innerHTML = '<blink>ipsum</blink>';
	while (temp2.firstChild) {
		temp.appendChild(temp2.firstChild);
	}

	temp.appendChild(document.createTextNode(' '));
	temp2.innerHTML = '<i>dolor sit amet</i>';
	while (temp2.firstChild) {
		temp.appendChild(temp2.firstChild);
	}

	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), '');
});

test('falsey attribute values', function() {

	var view = duel(
		['div', { 'data-str' : '', 'data-num' : 0, 'data-bool' : false, 'data-null' : null, 'data-undef' : undefined },
		 	'Lorem ipsum'
		]);

	var actual = view().toDOM();

	var expected = document.createElement('div');
	expected.setAttribute('data-str', '');
	expected.setAttribute('data-num', 0);
	expected.setAttribute('data-bool', false);
	expected.setAttribute('data-null', '');
	expected.setAttribute('data-undef', '');
	expected.appendChild(document.createTextNode('Lorem ipsum'));

	same(toHTML(actual), toHTML(expected), '');
});

test('comment nodes', function() {

	var view = duel(
		['div',
		 	['!',
		 	 	'Comment before'],
		 	'Lorem ipsum',
		 	['!',
		 	 	'Comment after']
		]);

	var actual = view().toDOM();

	var expected = document.createElement('div');
	expected.appendChild(document.createComment('Comment before'));
	expected.appendChild(document.createTextNode('Lorem ipsum'));
	expected.appendChild(document.createComment('Comment after'));

	same(toHTML(actual), toHTML(expected), '');
});

test('ignore doctype node', function() {

	var view = duel(
		['',
		 	['!DOCTYPE',
		 	 	'html'],
	 	 	'\n',
			['html',
			 	['body',
			 	 	'Lorem ipsum.']
			]
	 	]);

	var actual = view().toDOM();

	var expected = document.createElement('html');
	var temp = document.createElement('body');
	temp.appendChild(document.createTextNode('Lorem ipsum.'));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), '');
});

test('deferred attribute binding', function() {

	var attrs = {
		'class' : function() { return 'hello'; },
		'data-foo' : function() { return 'foo'; }
	};

	var id = '_'+new Date().valueOf().toString(36);

	var expected = document.createElement('div');
	expected.setAttribute('id', id);
	expected.appendChild(document.createTextNode('Lorem ipsum'));
	expected.setAttribute('class', 'hello');
	expected.setAttribute('data-foo', 'foo');

	var actual = document.createElement('div');
	actual.setAttribute('id', id);
	actual.appendChild(document.createTextNode('Lorem ipsum'));

	duel(attrs)().toDOM(actual, 1);

	same(toHTML(actual), toHTML(expected), '');
});

test('$init function callback', function() {

	var callbackOninitResult;
	
	function callback() {
		callbackOninitResult = toHTML(this);
	}

	var view = duel(
		['div', { '$init' : function() { return callback; } },
			['p',
			 	'Lorem ipsum dolor sit amet']
		]);

	// initialize results
	callbackOninitResult = null;

	var actual = view().toDOM();

	ok(!!callbackOninitResult, "$init was called");

	var expected = document.createElement('div');

	var temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Lorem ipsum dolor sit amet'));

	expected.appendChild(temp);

	same(callbackOninitResult, toHTML(expected), '');
});

test('$init string callback', function() {

	var view = duel(
		['div', { '$init' : 'document.callbackOninitResult = toHTML(this);' },
			['p',
			 	'Lorem ipsum dolor sit amet']
		]);

	// initialize results
	document.callbackOninitResult = null;

	var actual = view().toDOM();

	ok(!!document.callbackOninitResult, "$init was called");

	var expected = document.createElement('div');

	var temp = document.createElement('p');
	temp.appendChild(document.createTextNode('Lorem ipsum dolor sit amet'));

	expected.appendChild(temp);

	same(document.callbackOninitResult, toHTML(expected), '');
});

asyncTest('$load function callback', function() {

	var callbackOnloadResult;

	function callback() {
		callbackOnloadResult = toHTML(this);
	}

	var view = duel(
		['div', { '$load' : function() { return callback; } },
			['p',
			 	'Lorem ipsum dolor sit amet']
		]);

	// initialize results
	callbackOnloadResult = null;

	var actual = view().toDOM();

	setTimeout(function() {
		start();

		ok(callbackOnloadResult, "$load was called");

		var expected = document.createElement('div');
	
		var temp = document.createElement('p');
		temp.appendChild(document.createTextNode('Lorem ipsum dolor sit amet'));
	
		expected.appendChild(temp);

		same(callbackOnloadResult, toHTML(expected), '');
	}, 100);
});

asyncTest('$load string callback', function() {

	var view = duel(
		['div', { '$load' : 'document.callbackOnloadResult = toHTML(this);' },
			['p',
			 	'Lorem ipsum dolor sit amet']
		]);

	// initialize results
	document.callbackOnloadResult = null;

	var actual = view().toDOM();

	setTimeout(function() {
		start();

		ok(document.callbackOnloadResult, "$load was called");

		var expected = document.createElement('div');
	
		var temp = document.createElement('p');
		temp.appendChild(document.createTextNode('Lorem ipsum dolor sit amet'));
	
		expected.appendChild(temp);
	
		same(document.callbackOnloadResult, toHTML(expected), '');
	}, 100);
});

}catch(ex){alert(ex);}