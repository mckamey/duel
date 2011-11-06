function toHTML(node) {
	var root = document.createElement('div');
	root.appendChild(node);
	return root.innerHTML;
}
