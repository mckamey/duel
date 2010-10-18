module("View.bind()");

test("static view", function() {

	var expected =
		["div", { "class" : "list", "style" : "color:blue" },
			["h2", "This is the title"],
			["ul",
				["li", { "class" : "item" },
					["b", "Example"],
					": ",
					["i", "First!"]
				],
				["li", { "class" : "item" },
					["b", "Sample"],
					": ",
					["i", "Last!"]
				]
			]
		];

	var actual = duel(expected).bind().value;

	same(actual, expected, "");
});

test("simple expressions", function() {

	var model = {
	        name: "Foo.js",
	        url: "http://example.com/foo.js",
	        size: 5.87,
	        details: "Lorem ipsum dolor sit amet"
	    };

	var view = duel(
		["div", { "class" : "download" },
			["h2",
			 	"Filename: ",
				function(model, index, count) { return model.name; }
			],
			["p",
			 	"URL: ",
			 	["a", { "href" : function(model, index, count) { return model.url; }, "target" : "_blank" },
			 	 	function(model, index, count) { return model.url ;}
			 	],
			 	" (",
			 	function(model, index, count) { return model.size ;},
			 	"KB)"
		 	],
			["p",
			 	"Description: ",
			 	function(model, index, count) { return model.details; }
			]
		]);

	var actual = view.bind(model).value;

	var expected = 
		["div", { "class" : "download" },
			["h2",
			 	"Filename: Foo.js"
			],
			["p",
			 	"URL: ",
			 	["a", { "href" : "http://example.com/foo.js", "target" : "_blank" },
			 		"http://example.com/foo.js"
		 		],
			 	" (5.87KB)"
		 	],
			["p",
			 	"Description: Lorem ipsum dolor sit amet"
			]
		];

	same(actual, expected, "");
});

test("simple orphaned if/else", function() {

	var view = duel(
		["",
		 	["$if", { "test" : function(model, index, count) { return model.name === "Example"; } },
		 	 	["p", "True: Example === ", function(model, index, count) { return model.name; } ]
		 	],
		 	["$if", { "test" : function(model, index, count) { return model.name !== "Example"; } },
		 	 	["p", "False: Example !== ", function(model, index, count) { return model.name; } ]
		 	],
		 	["$else",
		 	 	["p", "Both: orphaned else always executes" ]
		 	]
		]);

	var model1 = { name: "Example" };
	var actual1 = view.bind(model1).value;
	var expected1 =
		["",
		 	["p", "True: Example === Example"],
		 	["p", "Both: orphaned else always executes" ]
		];

	same(actual1, expected1, "Binding with simple if statements.");

	var model2 = { name: "Sample" };
	var actual2 = view.bind(model2).value;
	var expected2 =
		["",
		 	["p", "False: Example !== Sample"],
		 	["p", "Both: orphaned else always executes" ]
		];

	same(actual2, expected2, "");
});

test("choose", function() {

	var view = duel(
	 	["$choose",
		 	["$if", { "test" : function(model, index, count) { return !model.children || !model.children.length; } },
		 	 	["p", "Has no items."]
		 	],
		 	["$if", { "test" : function(model, index, count) { return model.children && model.children.length === 1; } },
		 	 	["p", "Has only one item."]
		 	],
		 	["$else",
		 	 	["p", "Has ", function(model, index, count) { return model.children.length; }, " items."]
		 	]
	 	]);

	var model1 = { name: "Three", children: [0,2,4] };
	var actual1 = view.bind(model1).value;
	var expected1 = ["p", "Has 3 items."];

	same(actual1, expected1, "Binding with choose block.");

	var model2 = { name: "One", children: [42] };
	var actual2 = view.bind(model2).value;
	var expected2 = ["p", "Has only one item."];

	same(actual2, expected2, "Binding with choose block.");

	var model3 = { name: "Zero", children: [] };
	var actual3 = view.bind(model3).value;
	var expected3 = ["p", "Has no items."];

	same(actual3, expected3, "");
});

test("foreach array", function() {

	var model = {
	        title: "This is the title",
	        items: [
	            { name: "One" },
	            { name: "Two" },
	            { name: "Three" },
	            { name: "Four" },
	            { name: "Five" }
	        ]
	    };

	var view = duel(
		["div", { "class" : "list", "style" : "color:blue" },
			["h2",
			 	function(model, index, count) { return model.title; }
			],
			["ul",
			 	["$for", { "each" : function(model, index, count) { return model.items; } },
					["li", { "class" : "item" },
						["b",
						 	function(model, index, count) { return model.name; }
						],
						": ",
						["i",
						 	function(model, index, count) { return index + 1; },
							" of ",
							function(model, index, count) { return count; }
						]
					]
			 	]
			]
		]);

	var actual = view.bind(model).value;

	var expected =
		["div", { "class" : "list", "style" : "color:blue" },
			["h2", "This is the title"],
			["ul",
				["li", { "class" : "item" },
					["b", "One"],
					": ",
					["i", "1 of 5"]
				],
				["li", { "class" : "item" },
					["b", "Two"],
					": ",
					["i", "2 of 5" ]
				],
				["li", { "class" : "item" },
					["b", "Three"],
					": ",
					["i", "3 of 5"]
				],
				["li", { "class" : "item" },
					["b", "Four"],
					": ",
					["i", "4 of 5"]
				],
				["li", { "class" : "item" },
					["b", "Five"],
					": ",
					["i", "5 of 5"]
				]
			]
		];

	same(actual, expected, "");
});

test("foreach object", function() {
	var model = {
	        name: "List of items",
	        total: 5,
	        items: [
	            "One",
	            "Two",
	            "Three",
	            "Four",
	            "Five"
	        ]
	    };

	var view = duel(
		["",
		 	"model => ",
		 	["dl",
				["$for", { "each" : function(model, index, count) { return model; } },
				 	["dt",
					 	function(model, index, count) { return index; },
					 	" : "],
					["dd",
					 	"(",
					 	function(model, index, count) { return (model instanceof Array) ? "array" : typeof model; },
					 	") ",
					 	function(model, index, count) { return "" + model; }
				 	]
			 	]
		 	]
	 	]);

	var actual = view.bind(model).value;

	var expected =
		["",
		 	"model => ",
		 	["dl",
		 	 	["dt", "name : "],
		 	 	["dd", "(string) List of items"],
		 	 	["dt", "total : "],
		 	 	["dd", "(number) 5"],
		 	 	["dt", "items : "],
				["dd", "(array) One,Two,Three,Four,Five"]
		 	]
	 	];

	same(actual, expected, "");
});

test("markup data", function() {

	var model = {
	        details: "<blink>Lorem ipsum dolor sit amet</blink>"
	    };

	var view = duel(
		["div", { "class" : "test" },
			["p",
			 	"Description: ",
			 	function(model, index, count) { return duel.raw(model.details); }
			]
		]);

	var actual = view.bind(model).value;

	var expected = 
		["div", { "class" : "test" },
			["p",
			 	"Description: ",
			 	duel.raw("<blink>Lorem ipsum dolor sit amet</blink>")
			]
		];

	same(actual, expected, "");
});

test("call view", function() {

	var model = {
	        name: "Outer list",
	        items: ["One", "Two", "Three"]
	    };

	var Foo = {
			itemView: duel(
					["li",
					 	"model: ",
					 	function(model, index, count) { return model; },
					 	["br"],
					 	"index: ",
					 	function(model, index, count) { return index; },
					 	["br"],
					 	"count: ",
					 	function(model, index, count) { return count; },
					]),
			listView: duel(
					["div",
					 	["h2", function(model, index, count) { return model.name; } ],
						["ul",
						 	["$for", { "each" : function(model, index, count) { return model.items; } },
						 		["$call", {
							 			"view" : function(model, index, count) { return Foo.itemView; },
							 			"model" :  function(model, index, count) { return model; },
							 			"index" :  function(model, index, count) { return index; },
							 			"count" :  function(model, index, count) { return count; }
						 			}
						 		]
						 	]
						]
					])
			};

	var actual = Foo.listView.bind(model).value;

	var expected = 
		["div",
		 	["h2", "Outer list" ],
			["ul",
				["li",
					"model: One",
					["br"],
					"index: 0",
					["br"],
					"count: 3"
				],
				["li",
					"model: Two",
					["br"],
					"index: 1",
					["br"],
					"count: 3"
				],
				["li",
					"model: Three",
					["br"],
					"index: 2",
					["br"],
					"count: 3"
				]
			]
		];

	same(actual, expected, "");
});
