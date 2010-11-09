module("duel(data)");

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

	var actual = duel(expected)().value;

	same(actual, expected, "");
});

test("simple expressions", function() {

	var data = {
	        name: "Foo.js",
	        url: "http://example.com/foo.js",
	        size: 5.87,
	        details: "Lorem ipsum dolor sit amet"
	    };

	var view = duel(
		["div", { "class" : "download" },
			["h2",
			 	"Filename: ",
				function(data, index, count) { return data.name; }
			],
			["p",
			 	"URL: ",
			 	["a", { "href" : function(data, index, count) { return data.url; }, "target" : "_blank" },
			 	 	function(data, index, count) { return data.url ;}
			 	],
			 	" (",
			 	function(data, index, count) { return data.size ;},
			 	"KB)"
		 	],
			["p",
			 	"Description: ",
			 	function(data, index, count) { return data.details; }
			]
		]);

	var actual = view(data).value;

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
		 	["$if", { "test" : function(data, index, count) { return data.name === "Example"; } },
		 	 	["p", "True: Example === ", function(data, index, count) { return data.name; } ]
		 	],
		 	["$if", { "test" : function(data, index, count) { return data.name !== "Example"; } },
		 	 	["p", "False: Example !== ", function(data, index, count) { return data.name; } ]
		 	],
		 	["$if",
		 	 	["p", "Both: orphaned else always executes" ]
		 	]
		]);

	var data1 = { name: "Example" };
	var actual1 = view(data1).value;
	var expected1 =
		["",
		 	["p", "True: Example === Example"],
		 	["p", "Both: orphaned else always executes" ]
		];

	same(actual1, expected1, "Binding with simple if statements.");

	var data2 = { name: "Sample" };
	var actual2 = view(data2).value;
	var expected2 =
		["",
		 	["p", "False: Example !== Sample"],
		 	["p", "Both: orphaned else always executes" ]
		];

	same(actual2, expected2, "");
});

test("XOR block", function() {

	var view = duel(
	 	["$xor",
		 	["$if", { "test" : function(data, index, count) { return !data.children || !data.children.length; } },
		 	 	["p", "Has no items."]
		 	],
		 	["$if", { "test" : function(data, index, count) { return data.children && data.children.length === 1; } },
		 	 	["p", "Has only one item."]
		 	],
		 	["$if",
		 	 	["p", "Has ", function(data, index, count) { return data.children.length; }, " items."]
		 	]
	 	]);

	var data1 = { name: "Three", children: [0,2,4] };
	var actual1 = view(data1).value;
	var expected1 = ["p", "Has 3 items."];

	same(actual1, expected1, "Binding with choose block.");

	var data2 = { name: "One", children: [42] };
	var actual2 = view(data2).value;
	var expected2 = ["p", "Has only one item."];

	same(actual2, expected2, "Binding with choose block.");

	var data3 = { name: "Zero", children: [] };
	var actual3 = view(data3).value;
	var expected3 = ["p", "Has no items."];

	same(actual3, expected3, "");
});

test("for-each array", function() {

	var data = {
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
			 	function(data, index, count) { return data.title; }
			],
			["ul",
			 	["$for", { "each" : function(data, index, count) { return data.items; } },
					["li", { "class" : "item" },
						["b",
						 	function(data, index, count) { return data.name; }
						],
						": ",
						["i",
						 	function(data, index, count) { return index + 1; },
							" of ",
							function(data, index, count) { return count; }
						]
					]
			 	]
			]
		]);

	var actual = view(data).value;

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

test("for-each primitive", function() {

	var data = {
	        title: "This is the title",
	        items: "One"
	    };

	var view = duel(
		["div", { "class" : "list", "style" : "color:blue" },
			["h2",
			 	function(data, index, count) { return data.title; }
			],
			["ul",
			 	["$for", { "each" : function(data, index, count) { return data.items; } },
					["li", { "class" : "item" },
						["b",
						 	function(data, index, count) { return data; }
						],
						": ",
						["i",
						 	function(data, index, count) { return index + 1; },
							" of ",
							function(data, index, count) { return count; }
						]
					]
			 	]
			]
		]);

	var actual = view(data).value;

	var expected =
		["div", { "class" : "list", "style" : "color:blue" },
			["h2", "This is the title"],
			["ul",
				["li", { "class" : "item" },
					["b", "One"],
					": ",
					["i", "1 of 1"]
				]
			]
		];

	same(actual, expected, "");
});

test("for-in object", function() {
	var data = {
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
		 	"data => ",
		 	["dl",
				["$for", { "in" : function(data, index, count) { return data; } },
				 	["dt",
					 	function(data, index, count) { return index; },
					 	" of ",
					 	function(data, index, count) { return count; },
					 	" - ",
					 	function(data, index, count) { return data.key; },
					 	" : "],
					["dd",
					 	"(",
					 	function(data, index, count) { return (data.value instanceof Array) ? "array" : typeof data.value; },
					 	") ",
					 	function(data, index, count) { return "" + data.value; }
				 	]
			 	]
		 	]
	 	]);

	var actual = view(data).value;

	var expected =
		["",
		 	"data => ",
		 	["dl",
		 	 	["dt", "0 of 3 - name : "],
		 	 	["dd", "(string) List of items"],
		 	 	["dt", "1 of 3 - total : "],
		 	 	["dd", "(number) 5"],
		 	 	["dt", "2 of 3 - items : "],
				["dd", "(array) One,Two,Three,Four,Five"]
		 	]
	 	];

	same(actual, expected, "");
});

test("for-count", function() {
	var data = {
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
		 	"list => ",
		 	["dl",
				["$for", {
						"count" : function(data, index, count) { return 4; },
						"data" : function(data, index, count) { return data.name; }
					},
				 	["dt",
					 	function(data, index, count) { return index; },
					 	" of ",
					 	function(data, index, count) { return count; },
					 	": "],
					["dd",
					 	function(data, index, count) { return "" + data; }
				 	]
			 	]
		 	]
	 	]);

	var actual = view(data).value;

	var expected =
		["",
		 	"list => ",
		 	["dl",
		 	 	["dt", "0 of 4: "],
		 	 	["dd", "List of items"],
		 	 	["dt", "1 of 4: "],
		 	 	["dd", "List of items"],
		 	 	["dt", "2 of 4: "],
		 	 	["dd", "List of items"],
		 	 	["dt", "3 of 4: "],
				["dd", "List of items"]
		 	]
	 	];

	same(actual, expected, "");
});

test("markup data", function() {

	var data = {
	        details: "<blink>Lorem ipsum dolor sit amet</blink>"
	    };

	var view = duel(
		["div", { "class" : "test" },
			["p",
			 	"Description: ",
			 	function(data, index, count) { return duel.raw(data.details); }
			]
		]);

	var actual = view(data).value;

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

	var data = {
	        name: "Outer list",
	        items: ["One", "Two", "Three"]
	    };

	var Foo = {
			itemView: duel(
					["li",
					 	"data: ",
					 	function(data, index, count) { return data; },
					 	["br"],
					 	"index: ",
					 	function(data, index, count) { return index; },
					 	["br"],
					 	"count: ",
					 	function(data, index, count) { return count; },
					]),
			listView: duel(
					["div",
					 	["h2", function(data, index, count) { return data.name; } ],
						["ul",
						 	["$for", { "each" : function(data, index, count) { return data.items; } },
						 		["$call", {
							 			"view" : function(data, index, count) { return Foo.itemView; },
							 			"data" :  function(data, index, count) { return data; },
							 			"index" :  function(data, index, count) { return index; },
							 			"count" :  function(data, index, count) { return count; }
						 			}
						 		]
						 	]
						]
					])
			};

	var actual = Foo.listView(data).value;

	var expected = 
		["div",
		 	["h2", "Outer list" ],
			["ul",
				["li",
					"data: One",
					["br"],
					"index: 0",
					["br"],
					"count: 3"
				],
				["li",
					"data: Two",
					["br"],
					"index: 1",
					["br"],
					"count: 3"
				],
				["li",
					"data: Three",
					["br"],
					"index: 2",
					["br"],
					"count: 3"
				]
			]
		];

	same(actual, expected, "");
});

test("call wrapper view", function() {

	var data = {
	        name: "Outer list",
	        items: ["One", "Two", "Three"]
	    };

	var Foo = {
			itemView: duel(
					["li",
					 	["$part", { "name" : "itemLayout" }]
					]),
			listView: duel(
					["div",
					 	["h2", function(data, index, count) { return data.name; } ],
						["ul",
						 	["$for", { "each" : function(data, index, count) { return data.items; } },
						 		["$call", {
							 			"view" : function(data, index, count) { return Foo.itemView; },
							 			"data" :  function(data, index, count) { return data; },
							 			"index" :  function(data, index, count) { return index; },
							 			"count" :  function(data, index, count) { return count; }
						 			},
						 			["$part", { "name" : "itemLayout" },
						 			 	"data: ",
									 	function(data, index, count) { return data; },
									 	["br"],
									 	"index: ",
									 	function(data, index, count) { return index; },
									 	["br"],
									 	"count: ",
									 	function(data, index, count) { return count; }
								 	]
						 		]
						 	]
						]
					])
			};

	var actual = Foo.listView(data).value;

	var expected = 
		["div",
		 	["h2", "Outer list" ],
			["ul",
				["li",
					"data: One",
					["br"],
					"index: 0",
					["br"],
					"count: 3"
				],
				["li",
					"data: Two",
					["br"],
					"index: 1",
					["br"],
					"count: 3"
				],
				["li",
					"data: Three",
					["br"],
					"index: 2",
					["br"],
					"count: 3"
				]
			]
		];

	same(actual, expected, "");
});
