# [DUEL][1]: The 'V' in MVC

DUEL is a [dual-side templating][3] engine using HTML for layout and pure JavaScript as the binding language. Views may be executed directly in the browser (client-side template) or on the server (server-side template).

---

# Syntax

The DUEL grammar is an intentionally small, familiar syntax. Each term is intended to be intuitive to remember and is short without abbreviations. DUEL views are defined as HTML/CSS/JavaScript with a small set of special markup tags to control flow and JavaScript code blocks to bind the view template to model data.

The goal is to deliberately keep the syntax minimal. It should be easy to remember and nothing should need to be looked up during usage.

## Markup

The *complete* set of DUEL markup is:

### View declaration `<%@ view name="…" %>`

Sits at the top of a view to define its metadata. The `name` attribute contains a string literal which defines the name of view type.

### Loop construct `<for each="...">...</for>`

Wrapped around content to be repeated once per item. The `each` attribute contains an `Array` expression defining the list of items to iterate over. The `model` value will contain the current item, and the `index` and `count` values (see below) will be updated with the current item index and total count respectively.

If the `each` attribute contains an `object` expression, then the loop will iterate over the properties of that object. In this case, `model` will contain the `object` itself, `index` will contain the property name and `count` will not be used.

### Conditional blocks `<if test="...">...<else if="...">...<else>...</if>`

Wrapped around conditional content. The `test` attribute contains a `boolean` expression indicating if contents should be included in result. Any truthy value will cause the contents of that section to be emitted.

`<else>` tags sit inside an `<if></if>` block without a closing tag. The `if` attribute (alternatively `<else test="...">` may be used for symmetry with `<if test="...">`) contains a `boolean` expression indicating if contents should be included in result.

### Single element conditional `<div if="…">…</div>`

May be applied to any HTML tag to make it conditionally render. The `if` attribute contains a `boolean` expression indicating if contents should be included in result.

### Embed other views `<call view="…" model="…" index="…" count="…" />`

Calls another template specifying the data to bind. The `view` attribute is the name of the view to bind, the `model` attribute defines the data to bind, and optionally `index` and `count` attributes may be specified to indicate which item of a list is being bound (item `index` of `count` items).

### Partial views `<part name="…">…</part>`

Sits inside a view as a placeholder for replacement content, or within a `<call></call>` block to define the replacement content. The `name` attribute is a `string` expression specifying the name of the part to replace.

## Code Blocks

Code blocks contain 100% pure JavaScript, so there isn't a new language to learn. There are three types of code blocks available:

### Statement Blocks `<% … %>`

When code within a statement block is executed, if any value is returned it will be emitted as part of the output.

### Expression Blocks `<%= … %>`

When code within an expression block is executed, the result of will be emitted as part of the output, interpretted as plain text. Expression blocks are equivalent to `<% return (…); %>`.

### Markup Expression Block `<%# … %>`

When code within a markup expression block is executed, the result will be emitted as part of the output, interpreted as HTML. NOTE: these blocks are rarely used (only when data itself contains markup).

## Data Values

The data values available inside the JavaScript code blocks are:

- `model` contains the model data being bound to the view template
- `index` used within loops to indicate the index of the current item being bound
- `count` used within loops to indicate total number of items being bound

== Markup attributes ==

The attributes in each of the markup elements are implicitly code blocks. This means you can add or leave off the code block syntax based on preference:

	<if test="index % 2 === 0">
		<%= index %> is even.
	<else>
		<%= index %> is odd.
	</if>

is equivalent to:

	<if test="<%= index % 2 === 0 %>">
		<%= index %> is even.
	<else>
		<%= index %> is odd.
	</if>

The only exception to this shorthand is the `name` attribute of `<part>`: since it is always a string,

	<part name="foo"></part>

is equivalent to:

	<part name="<%= "foo" %>"></part>

---

## Example

Here is a complete example where the result differs if there is zero, one, or many items.

	<%@ view name="foo.bar" %>

	<div class="items">
		<h3><%= model.title %></h3>

		<if test="!model.items || model.items.length === 0">
			<call view="foo.warning" model="model">
				<part name="messageArea">
					<p class="item empty"><%= model.title %> contains no items.</p>
				</part>
			</call>

		<else if="model.items.length === 1">
			<p class="item">
				<b><%= model[0].name %>: </b><i><%= model[0].detail %></i>
			</p>

		<else>
			<ul><for each="model.items">
				<li class="item">
					<b><%= model.name %>: </b><i><%= model.detail %></i>
				</li>
			</for></ul>
		</if>
	</div>

To invoke this view from JavaScript, just call it as a function:

	var model = { title: "Hello world!", items: [ { name: "One", detail: 101 },  { name: "two", detail: 2.718 } ] };

	var markup = foo.bar(model).toString(); // either get a string as output
	document.getElementById("baz").innerHTML = markup;

	var result = foo.bar(model).toDOM(); // or get DOM objects as output
	document.getElementById("baz").appendChild(result);

Which would output:

	<div class="items">
		<h3>Hello world!</h3>

		<ul>
			<li class="item">
				<b>One: </b><i>101</i>
			</li>
			<li class="item">
				<b>Two: </b><i>2.718</i>
			</li>
		</ul>
	</div>

---

## Licensed under the [MIT license][2]
Copyright &copy;2006-2010 Stephen M. McKamey.

  [1]: http://duelengine.org
  [2]: http://duelengine.org/license.txt
  [3]: http://ajaxpatterns.org/Dual-Side_Templating
