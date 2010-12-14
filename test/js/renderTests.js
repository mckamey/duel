try {

module("Result.toString()");

test("nested elements with attributes", function() {

	var view = duel(
		["div", { "class" : "download" },
			["h2",
			 	"Filename: Foo.js"
			],
			["p",
			 	"URL: ",
			 	["br"],
			 	["a", { "href" : "http://example.com/foo.js", "target" : "_blank", "title" : "Lorem ipsum dolor sit amet" },
			 		"http://example.com/foo.js"
		 		],
			 	" (5.87KB)"
		 	],
		 	["hr"],
			["p",
			 	"Description: Lorem ipsum dolor sit amet"
			]
		]);

	var actual = view().toString();

	var expected =
		'<div class="download"><h2>Filename: Foo.js</h2>'+
		'<p>URL: <br /><a href="http://example.com/foo.js" target="_blank" title="Lorem ipsum dolor sit amet">http://example.com/foo.js</a> (5.87KB)</p>'+
		'<hr />'+
		'<p>Description: Lorem ipsum dolor sit amet</p>'+
		'</div>';

	same(actual, expected, "");
});

test("native toString", function() {

	var view = duel(
		["div", { "class" : "download" },
			["h2",
			 	"Filename: Foo.js"
			],
			["p",
			 	"URL: ",
			 	["br"],
			 	["a", { "href" : "http://example.com/foo.js", "target" : "_blank", "title" : "Lorem ipsum dolor sit amet" },
			 		"http://example.com/foo.js"
		 		],
			 	" (5.87KB)"
		 	],
		 	["hr"],
			["p",
			 	"Description: Lorem ipsum dolor sit amet"
			]
		]);

	var actual = ""+view();

	var expected =
		'<div class="download"><h2>Filename: Foo.js</h2>'+
		'<p>URL: <br /><a href="http://example.com/foo.js" target="_blank" title="Lorem ipsum dolor sit amet">http://example.com/foo.js</a> (5.87KB)</p>'+
		'<hr />'+
		'<p>Description: Lorem ipsum dolor sit amet</p>'+
		'</div>';

	same(actual, expected, "");
});

test("innerHTML toString", function() {

	var view = duel(
		["div", { "class" : "download" },
			["h2",
			 	"Filename: Foo.js"
			],
			["p",
			 	"URL: ",
			 	["br"],
			 	["a", { "href" : "http://example.com/foo.js", "target" : "_blank", "title" : "Lorem ipsum dolor sit amet" },
			 		"http://example.com/foo.js"
		 		],
			 	" (5.87KB)"
		 	],
		 	["hr"],
			["p",
			 	"Description: Lorem ipsum dolor sit amet"
			]
		]);

	var actual = document.createElement("div");
	actual.innerHTML = view();

	var expected = document.createElement("div");
	expected.innerHTML =
		'<div class="download"><h2>Filename: Foo.js</h2>'+
		'<p>URL: <br /><a href="http://example.com/foo.js" target="_blank" title="Lorem ipsum dolor sit amet">http://example.com/foo.js</a> (5.87KB)</p>'+
		'<hr />'+
		'<p>Description: Lorem ipsum dolor sit amet</p>'+
		'</div>';

	same(actual.innerHTML, expected.innerHTML, "");
});

test("docFrag root", function() {

	var view = duel(
		["",
		 	["p", "Inner child one."],
		 	["p", "Inner child two." ]
		]);

	var actual = view().toString();
	var expected = '<p>Inner child one.</p><p>Inner child two.</p>';

	same(actual, expected, "");
});

test("docFrag inner", function() {

	var view = duel(
		["div",
			["",
			 	["p", "Inner child one."],
			 	["p", "Inner child two." ]
			]
		]);

	var actual = view().toString();
	var expected = '<div><p>Inner child one.</p><p>Inner child two.</p></div>';

	same(actual, expected, "");
});

test("encoding literal", function() {

	var view = duel(
		["p",
		 	'&hello"foo<bar><&>'
		]);

	var actual = view().toString();

	var expected =  '<p>&amp;hello"foo&lt;bar&gt;&lt;&amp;&gt;</p>';
	
	same(actual, expected, "");
});

test("encoding attributes", function() {

	var view = duel(
		["p", { "title" : '&hello"foo<bar><&>' },
		 	"Encoded attributes"
		]);

	var actual = view().toString();

	var expected =  '<p title="&amp;hello&quot;foo&lt;bar&gt;&lt;&amp;&gt;">Encoded attributes</p>';
	
	same(actual, expected, "");
});

test("markup data", function() {

	var view = duel(
		["div", { "class" : "test" },
			["p",
			 	"Description: ",
			 	duel.raw("<b>Lorem </b>"),
			 	duel.raw("<blink>ipsum</blink>"),
			 	" ",
			 	duel.raw("<i>dolor sit amet</i>")
			]
		]);

	var actual = view().toString();

	var expected = 
		'<div class="test">'+
		'<p>Description: <b>Lorem </b><blink>ipsum</blink> <i>dolor sit amet</i></p>'+
		'</div>';
	same(actual, expected, "");
});

test("falsey attribute values", function() {

	var view = duel(
		["div", { "data-str" : "", "data-num" : 0, "data-bool" : false, "data-null" : null, "data-undef" : undefined },
		 	"Lorem ipsum"
		]);

	var actual = view().toString();

	var expected = 
		'<div data-str="" data-num="0" data-bool="false" data-null data-undef>'+
		'Lorem ipsum'+
		'</div>';
	same(actual, expected, "");
});

test("comment nodes", function() {

	var view = duel(
		["div",
		 	["!",
		 	 	"Comment before"],
		 	"Lorem ipsum",
		 	["!",
		 	 	"Comment after"]
		]);

	var actual = view().toString();

	var expected = 
		'<div>'+
		'<!--Comment before-->'+
		'Lorem ipsum'+
		'<!--Comment after-->'+
		'</div>';
	same(actual, expected, "");
});

test("doctype node", function() {

	var view = duel(
		["",
		 	["!DOCTYPE",
		 	 	"html"],
	 	 	"\n",
			["html",
			 	["body",
			 	 	"Lorem ipsum."]
			]
	 	]);

	var actual = view().toString();

	var expected = 
		'<!DOCTYPE html>\n'+
		'<html>'+
		'<body>'+
		'Lorem ipsum.'+
		'</body>'+
		'</html>';
	same(actual, expected, "");
});

} catch (ex) {
	alert(ex);
}