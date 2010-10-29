# [DUEL][1]: The 'V' in MVC

DUEL is a [dual-side templating][3] engine using HTML for layout and pure JavaScript as the binding language. Views may be executed directly in the browser (client-side template) or on the server (server-side template).

## Syntax

The DUEL grammar is an intentionally small and in familiar syntax. Each term is intended to be intuitive to remember and is short without abbreviations. DUEL views are defined as HTML/CSS/JavaScript with a select set of special markup tags to control flow and JavaScript code blocks to bind the view template to model data.

The *complete* set of DUEL markup is:

- `<%@ view name="…" %>` is the declaration at the top of a view to define its metadata. The `name` attribute contains a string literal which defines the name of view type.
- `<for each="…">…</for>` is wrapped around content to be repeated once per item. The `each` attribute contains an `object` expression defining the list of items to iterate over.
- `<if test="…">…</if>` is wrapped around conditional content. The `test` attribute contains a `boolean` expression indicating if contents should be included in result.
- `<else if="…">` or `<else>` sits inside an `<if></if>` block without a closing tag (alternatively `<else test="…">` for symmetry with `<if test="…">`). The `if` attributes (alternatively `test` may be used) contains a `boolean` expression indicating if contents should be included in result.
- `<div if="…">…</div>` may be applied to any HTML tag to make it conditionally render. The `if` attribute contains a `boolean` expression indicating if contents should be included in result.
- `<call view="…" model="…" index="…" count="…"></call>` calls another template specifying the data to bind. The `view` attribute is the name of the view to bind, the `model` attribute defines the data to bind, and optionally `index` and `count` attributes may be specified to indicate which item of a list is being bound (item `index` of `count` items).
- `<part name="…">…</part>` sits inside a view as a placeholder for replacement content, or within a `<call></call>` block to define the replacement content. The `name` attribute is a `string` expression specifying the name of the part to replace.

Code blocks contain only pure JavaScript, so there isn't a new language to learn. There are three types of code blocks available:

- If any value is returned from a **statement block** `<% … %>` it will be emitted as part of the output.
- The result of an **expression block** `<%= … %>` will be emitted as part of the output, interpretted as plain text (equivalent to `<% return (…); %>`).
- The result of an **markup expression block** `<%# … %>` will be emitted as part of the output, interpreted as HTML (rare; used only when data itself contains markup).

The keywords available inside the JavaScript code blocks are:

- `model` represents the model data being bound to the view template
- `index` used within loops to indicate which item is being bound
- `count` used within loops to indicate total number of items being bound

The goal is to deliberately keep the syntax minimal. It should be easy to remember and nothing should need to be looked up during usage.

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

To invoke this view from JavaScript, call it like a function:

	var model = { title: "Hello world!", items: [ { name: "One", detail: 101 },  { name: "two", detail: 2.718 } ] };
	var markup = foo.bar(model).toString(); // get a string as output
	var elem = foo.bar(model).toDOM(); // get DOM objects as output

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

## Licensed under the [MIT license][2]

  [1]: http://duelengine.org
  [2]: http://duelengine.org/license.txt
  [3]: http://ajaxpatterns.org/Dual-Side_Templating
