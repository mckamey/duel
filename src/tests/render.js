module("Result.toString()");

test("nested elements with attributes", function() {

	var view = duel(
		["div", { "class" : "download" },
			["h2",
			 	"Filename: Foo.js"
			],
			["p",
			 	"URL: ",
			 	["a", { "href" : "http://example.com/foo.js", "target" : "_blank", "title" : "Lorem ipsum dolor sit amet" },
			 		"http://example.com/foo.js"
		 		],
			 	" (5.87KB)"
		 	],
			["p",
			 	"Description: Lorem ipsum dolor sit amet"
			]
		]);

	var actual = view.bind().toString();

	var expected =
		'<div class="download"><h2>Filename: Foo.js</h2>'+
		'<p>URL: <a href="http://example.com/foo.js" target="_blank" title="Lorem ipsum dolor sit amet">http://example.com/foo.js</a> (5.87KB)</p>'+
		'<p>Description: Lorem ipsum dolor sit amet</p>'+
		'</div>';

	same(actual, expected, "");
});

test("docFrag root", function() {

	var view = duel(
		["",
		 	["p", "Inner child one."],
		 	["p", "Inner child two." ]
		]);

	var actual = view.bind().toString();
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

	var actual = view.bind().toString();
	var expected = '<div><p>Inner child one.</p><p>Inner child two.</p></div>';

	same(actual, expected, "");
});

test("encoding literal", function() {

	var view = duel(
		["p",
		 	'&hello"foo<bar><&>'
		]);

	var actual = view.bind().toString();

	var expected =  '<p>&amp;hello"foo&lt;bar&gt;&lt;&amp;&gt;</p>';
	
	same(actual, expected, "");
});

test("encoding attributes", function() {

	var view = duel(
		["p", { "title" : '&hello"foo<bar><&>' },
		 	"Encoded attributes"
		]);

	var actual = view.bind().toString();

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

	var actual = view.bind().toString();

	var expected = 
		'<div class="test">'+
		'<p>Description: <b>Lorem </b><blink>ipsum</blink> <i>dolor sit amet</i></p>'+
		'</div>';
	same(actual, expected, "");
});
