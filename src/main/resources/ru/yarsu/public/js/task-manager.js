var cellTemplate = '<input class="form-control" type="text" value="0">';

function addRow() {
	let m = document
		.getElementById("matrix-rows")
		.getElementsByTagName("tr").length;

	if (m >= 16) {
		alert("Хватит, уже не лезет!");
		return;
	}

	let n = document
		.getElementById("matrix-header")
		.getElementsByTagName("th").length;

	if (m >= n) {
		alert("Число строк не может быть больше числа столбцов");
		return;
	}

	let newRow = document.createElement("tr");
	for (let i = 0; i < n; ++i) {
		let newCell = document.createElement("td");
		newCell.innerHTML = cellTemplate;
		newRow.append(newCell);
	}
	document.getElementById("matrix-rows").append(newRow);
	let newCell = document.createElement("td");
	newCell.innerHTML = cellTemplate;
	document.getElementById("basis-coefficients").append(newCell);
}

function deleteRow() {
	let matrixRows = document
		.getElementById("matrix-rows")
		.getElementsByTagName("tr");
	let m = matrixRows.length;

	if (m <= 1) {
		alert("Должна остаться хотя бы одна строка");
		return;
	}

	matrixRows[m - 1].remove();

	document
		.getElementById("basis-coefficients")
		.getElementsByTagName("td")
		[m - 1].remove();
}

function addColumn() {
	let matrixHeader = document
		.getElementById("matrix-header")
		.getElementsByTagName("th");
	let n = matrixHeader.length;

	if (n >= 16) {
		alert("Хватит, уже не лезет!");
		return;
	}

	let matrixRows = document
		.getElementById("matrix-rows")
		.getElementsByTagName("tr");
	let m = matrixRows.length;

	let functionHeader = document
		.getElementById("function-header")
		.getElementsByTagName("th");

	let newColumnHeaderMatrix = document.createElement("th");
	newColumnHeaderMatrix.setAttribute("scope", "col");
	newColumnHeaderMatrix.innerText = `x${n}`;
	matrixHeader[n - 1].before(newColumnHeaderMatrix);

	let newColumnHeaderFunction = document.createElement("th");
	newColumnHeaderFunction.setAttribute("scope", "col");
	newColumnHeaderFunction.innerText = `x${n}`;
	functionHeader[n - 1].before(newColumnHeaderFunction);

	for (let i = 0; i < m; ++i) {
		let row = matrixRows[i].getElementsByTagName("td");
		let newCell = document.createElement("td");
		newCell.innerHTML = cellTemplate;
		row[n - 1].before(newCell);
	}
	let row = document
		.getElementById("function-coefficients")
		.getElementsByTagName("td");
	let newCell = document.createElement("td");
	newCell.innerHTML = cellTemplate;
	row[n - 1].before(newCell);
}

function deleteColumn() {
	let matrixHeader = document
		.getElementById("matrix-header")
		.getElementsByTagName("th");
	let n = matrixHeader.length;

	if (n <= 2) {
		alert("Должно остаться хотя бы 2 столбца");
		return;
	}

	let matrixRows = document
		.getElementById("matrix-rows")
		.getElementsByTagName("tr");
	let m = matrixRows.length;

	if (m >= n) {
		alert("Число строк не может быть больше числа столбцов");
		return;
	}

	let functionHeader = document
		.getElementById("function-header")
		.getElementsByTagName("th");
	matrixHeader[n - 2].remove();
	functionHeader[n - 2].remove();
	document
		.getElementById("function-coefficients")
		.getElementsByTagName("td")
		[n - 2].remove();
	for (let i = 0; i < m; ++i) {
		let row = matrixRows[i].getElementsByTagName("td");
		row[n - 2].remove();
	}
}

function prepareData() {}

function prepareMatrix() {}

function prepareFunction() {}
