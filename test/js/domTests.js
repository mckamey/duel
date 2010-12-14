try {

module("Result.toDOM()");

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

	var actual = view().toDOM();

	var temp, expected = document.createElement("div");
	expected.className = "download";

	temp = document.createElement("h2");
	temp.appendChild(document.createTextNode("Filename: Foo.js"));
	expected.appendChild(temp);

	temp = document.createElement("p");
	temp.appendChild(document.createTextNode("URL: "));
	temp.appendChild(document.createElement("br"));
	var temp2 = document.createElement("a");
	temp2.setAttribute("href", "http://example.com/foo.js");
	temp2.setAttribute("target", "_blank");
	temp2.setAttribute("title", "Lorem ipsum dolor sit amet");
	temp2.appendChild(document.createTextNode("http://example.com/foo.js"));
	temp.appendChild(temp2);
	temp.appendChild(document.createTextNode(" (5.87KB)"));
	expected.appendChild(temp);

	expected.appendChild(document.createElement("hr"));

	temp = document.createElement("p");
	temp.appendChild(document.createTextNode("Description: Lorem ipsum dolor sit amet"));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), "");
});

test("docFrag root", function() {

	var view = duel(
		["",
		 	["p", "Inner child one."],
		 	["p", "Inner child two." ]
		]);

	var actual = view().toDOM();

	var expected = document.createDocumentFragment();

	var temp = document.createElement("p");
	temp.appendChild(document.createTextNode("Inner child one."));
	expected.appendChild(temp);

	temp = document.createElement("p");
	temp.appendChild(document.createTextNode("Inner child two."));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), "");
});

test("docFrag inner", function() {

	var view = duel(
		["div",
			["",
			 	["p", "Inner child one."],
			 	["p", "Inner child two." ]
			]
		]);

	var actual = view().toDOM();

	var expected = document.createElement("div");

	var temp = document.createElement("p");
	temp.appendChild(document.createTextNode("Inner child one."));
	expected.appendChild(temp);

	temp = document.createElement("p");
	temp.appendChild(document.createTextNode("Inner child two."));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), "");
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

	var actual = view().toDOM();

	var expected = document.createElement("div");
	expected.className = "test";

	var temp = document.createElement("p");
	temp.appendChild(document.createTextNode("Description: "));

	var temp2 = document.createElement("div");
	temp2.innerHTML = "<b>Lorem </b>";
	while (temp2.firstChild) {
		temp.appendChild(temp2.firstChild);
	}
	temp2.innerHTML = "<blink>ipsum</blink>";
	while (temp2.firstChild) {
		temp.appendChild(temp2.firstChild);
	}

	temp.appendChild(document.createTextNode(" "));
	temp2.innerHTML = "<i>dolor sit amet</i>";
	while (temp2.firstChild) {
		temp.appendChild(temp2.firstChild);
	}

	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), "");
});

test("falsey attribute values", function() {

	var view = duel(
		["div", { "data-str" : "", "data-num" : 0, "data-bool" : false, "data-null" : null, "data-undef" : undefined },
		 	"Lorem ipsum"
		]);

	var actual = view().toDOM();

	var expected = document.createElement("div");
	expected.setAttribute("data-str", "");
	expected.setAttribute("data-num", 0);
	expected.setAttribute("data-bool", false);
	expected.setAttribute("data-null", "");
	expected.setAttribute("data-undef", "");
	expected.appendChild(document.createTextNode("Lorem ipsum"));

	same(toHTML(actual), toHTML(expected), "");
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

	var actual = view().toDOM();

	var expected = document.createElement("div");
	expected.appendChild(document.createComment("Comment before"));
	expected.appendChild(document.createTextNode("Lorem ipsum"));
	expected.appendChild(document.createComment("Comment after"));

	same(toHTML(actual), toHTML(expected), "");
});

test("ignore doctype node", function() {

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

	var actual = view().toDOM();

	var expected = document.createElement("html");
	var temp = document.createElement("body");
	temp.appendChild(document.createTextNode("Lorem ipsum."));
	expected.appendChild(temp);

	same(toHTML(actual), toHTML(expected), "");
});

test("deferred attribute binding", function() {

	var attrs = {
		"class" : function() { return "hello"; },
		"data-foo" : function() { return "foo"; }
	};

	var id = "__"+new Date().valueOf();

	var expected = document.createElement("div");
	expected.setAttribute("id", id);
	expected.appendChild(document.createTextNode("Lorem ipsum"));
	expected.setAttribute("class", "hello");
	expected.setAttribute("data-foo", "foo");

	var actual = document.createElement("div");
	actual.setAttribute("id", id);
	actual.appendChild(document.createTextNode("Lorem ipsum"));

	duel.attr(actual, attrs);
	
	same(toHTML(actual), toHTML(expected), "");
});

} catch (ex) {
	alert(ex);
}