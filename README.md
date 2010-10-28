# [duel][1]
## Licensed under the [MIT license][2]

The duel grammar is intentionally small, each term is short without abbreviation and each term is intended to be intuitive to remember.

The **entire** set of duel markup is:

- `<for each="…"></for>` attribute `each` is an `object` expression. Wrap around looped content.
- `<if test="…"></if>` attribute `if` is a `boolean` expression. Wrap around conditional content.
- `<else if="…">` or `<else>` (alternatively `<else test="…">`, for symmetry with `<if test="…">`) attributes `if` and `test` are `boolean` expressions. Sits inside an `<if></if>` block without a closing tag
- `<call view="…" model="…" index="…" count="…"></call>` attribute `view` is an expression for the name of the view. 
- `<part name="…"></part>` attribute `name` is a `string` expression. Sits inside a `<call></call>` block.
- `<div if="…"></div>` attribute `if` is a `boolean` expression. Attribute applied to any HTML tag to make it conditional.

The types of code blocks available are these:

- Declaration block `<%@ view="…" %>` at the top of a view defines the view's metadata. The `view` attribute is the name of the view.
- Statement block `<% … %>` any returned value will be emitted as part of the output
- Expression block `<%= … %>` the contents will be emitted as part of the output interpretted as plain text
- Markup block `<%# … %>` the contents will be emitted as part of the output, interpreted as HTML (rare)

The keywords accessible inside any code blocks are:

- `model`
- `index`
- `count`

The goal is to deliberately keep the syntax minimal. It should be easy to remember and nothing should need to be looked up during usage.

  [1]: http://duelengine.org
  [2]: http://duelengine.org/license.txt
