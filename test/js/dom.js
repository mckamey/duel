module("Result.toDOM()");

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

	var actual = view().toDOM();

	var temp, expected = document.createElement("div");
	expected.setAttribute("class", "download");

	temp = document.createElement("h2");
	temp.appendChild(document.createTextNode("Filename: Foo.js"));
	expected.appendChild(temp);

	temp = document.createElement("p");
	temp.appendChild(document.createTextNode("URL: "));
	var temp2 = document.createElement("a");
	temp2.setAttribute("href", "http://example.com/foo.js");
	temp2.setAttribute("target", "_blank");
	temp2.setAttribute("title", "Lorem ipsum dolor sit amet");
	temp2.appendChild(document.createTextNode("http://example.com/foo.js"));
	temp.appendChild(temp2);
	temp.appendChild(document.createTextNode(" (5.87KB)"));
	expected.appendChild(temp);

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
