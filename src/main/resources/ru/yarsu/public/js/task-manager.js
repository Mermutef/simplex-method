var cellTemplate = '<input class="form-control" type="text" value="0">';
var basisTemplate =
    '<input name="basis" class="form-control" type="number" value="0">';
var freeTemplate =
    '<input name="free" class="form-control" type="number" value="0">';

function createCell(template, val = 0) {
    let newCell = document.createElement("td");
    newCell.innerHTML = template;
    newCell.getElementsByTagName("input")[0].value = val;
    return newCell;
}

function createRow(n, template) {
    let newRow = document.createElement("tr");
    for (let i = 0; i < n; ++i) {
        newRow.append(createCell(template));
    }
    return newRow;
}

function removeFree() {
    let freeCoefficients = document
        .getElementById("free-coefficients")
        .getElementsByTagName("td");
    freeCoefficients[freeCoefficients.length - 1].remove();
}

function createTh(headerId, idx, text) {
    let header = document.getElementById(headerId).getElementsByTagName("th");
    let newColumnHeader = document.createElement("th");

    newColumnHeader.setAttribute("scope", "col");
    newColumnHeader.innerText = text;
    header[idx].before(newColumnHeader);
}

function removeByTagName(headerId, idx, tagName) {
    let header = document
        .getElementById(headerId)
        .getElementsByTagName(tagName);
    header[idx].remove();
}

function selectTagsById(fatherId, tagName) {
    return document.getElementById(fatherId).getElementsByTagName(tagName);
}

function selectMatrixRows() {
    return selectTagsById("matrix-rows", "tr");
}

function selectHeader(headerId) {
    return selectTagsById(headerId, "th");
}

function writeToJsonField(fieldId, val) {
    document.getElementById(fieldId).value = JSON.stringify(val);
}

function addRow() {
    let matrixRows = selectMatrixRows();
    let m = matrixRows.length;

    if (m >= 16) {
        alert("Хватит, уже не лезет!");
        return;
    }

    let n = selectHeader("matrix-header").length;

    if (m >= n) {
        alert("Число строк не может быть больше числа столбцов");
        return;
    }

    matrixRows[m - 1].after(createRow(n, cellTemplate));

    document
        .getElementById("basis-coefficients")
        .append(createCell(basisTemplate));
    removeFree();
}

function deleteRow() {
    let matrixRows = selectMatrixRows();
    let m = matrixRows.length;

    if (m <= 1) {
        alert("Должна остаться хотя бы одна строка");
        return;
    }

    matrixRows[m - 1].remove();

    removeByTagName("basis-coefficients", m - 1, "td");

    document
        .getElementById("free-coefficients")
        .append(createCell(freeTemplate));
}

function addColumn() {
    let matrixHeader = selectHeader("matrix-header");
    let n = matrixHeader.length;

    if (n >= 16) {
        alert("Хватит, уже не лезет!");
        return;
    }

    let matrixRows = selectMatrixRows();
    let m = matrixRows.length;

    createTh("function-header", n - 1, `x${n}`);
    createTh("matrix-header", n - 1, `x${n}`);

    for (let i = 0; i < m; ++i) {
        matrixRows[i]
            .getElementsByTagName("td")
            [n - 1].before(createCell(cellTemplate));
    }

    selectTagsById("function-coefficients", "td")[n - 1].before(
        createCell(cellTemplate)
    );

    document
        .getElementById("free-coefficients")
        .append(createCell(freeTemplate));
}

function deleteColumn() {
    let matrixHeader = selectHeader("matrix-header");
    let n = matrixHeader.length;

    if (n <= 2) {
        alert("Должно остаться хотя бы 2 столбца");
        return;
    }

    let matrixRows = selectMatrixRows();
    let m = matrixRows.length;

    if (m >= n) {
        alert("Число строк не может быть больше числа столбцов");
        return;
    }

    removeByTagName("function-header", n - 2, "th");
    removeByTagName("matrix-header", n - 2, "th");

    removeByTagName("function-coefficients", n - 2, "td");
    removeByTagName("free-coefficients", n - m - 2, "td");
    for (let i = 0; i < m; ++i) {
        matrixRows[i].getElementsByTagName("td")[n - 2].remove();
    }
}

function prepareData() {
    prepareBasis();
    prepareFree();
    prepareMatrix();
    prepareFunction();
    document.getElementById("send-task").click();
}

function prepareMatrix() {
    let matrixRows = selectMatrixRows();
    let matrix = [];
    for (let i = 0; i < matrixRows.length; ++i) {
        let row = [];
        let matrixRow = matrixRows[i].getElementsByTagName("input");
        for (let j = 0; j < matrixRow.length; ++j) {
            row.push(matrixRow[j].value);
        }
        matrix.push(row);
    }
    writeToJsonField("matrixJson", matrix);
}

function prepareFunction() {
    let functionInputs = selectTagsById("function-coefficients", "input");
    let fun = [];
    for (let i = 0; i < functionInputs.length; ++i) {
        fun.push(functionInputs[i].value);
    }
    writeToJsonField("functionJson", fun);
}

function prepareBasis() {
    let basisInputs = document.getElementsByName("basis");
    let basisIndices = [];
    for (let i = 0; i < basisInputs.length; ++i) {
        basisIndices.push(Number(basisInputs[i].value));
    }
    writeToJsonField("basisJson", basisIndices);
}

function prepareFree() {
    let checkValue = document.getElementById("inputFree").checked;
    let freeIndices = [];
    if (checkValue) {
        let freeInputs = document.getElementsByName("free");
        for (let i = 0; i < freeInputs.length; ++i) {
            freeIndices.push(Number(freeInputs[i].value));
        }
    }
    writeToJsonField("freeJson", freeIndices);
}

function processFree() {
    let checkValue = document.getElementById("inputFree").checked;
    if (!checkValue) {
        document.getElementById("freeTable").setAttribute("hidden", "hidden");
    } else {
        document.getElementById("freeTable").removeAttribute("hidden");
    }
}

function parseJsonField(fieldid) {
    return JSON.parse(document.getElementById(fieldid).value);
}

function getAndClearField(fieldId) {
    let field = document.getElementById(fieldId);
    field.innerHTML = "";
    return field;
}

function loadData() {
    loadIndices("basisJson", "basis-coefficients");
    loadIndices("freeJson", "free-coefficients");
    loadMatrix();
    loadFunction();
}

function loadIndices(jsonFieldId, indecesFieldId) {
    let indices = parseJsonField(jsonFieldId);
    let field = getAndClearField(indecesFieldId);
    for (let i = 0; i < indices.length; ++i) {
        field.append(createCell(basisTemplate, indices[i]));
    }
}

function loadMatrix() {
    let matrixIndices = parseJsonField("matrixJson");
    let matrixRows = getAndClearField("matrix-rows");
    for (let i = 0; i < matrixIndices.length; ++i) {
        let newRow = document.createElement("tr");
        for (let j = 0; j < matrixIndices[0].length; ++j) {
            newRow.append(createCell(cellTemplate, matrixIndices[i][j]));
        }
        matrixRows.append(newRow);
    }

    let matrixHeader = getAndClearField("matrix-header");
    for (let i = 0; i < matrixIndices[0].length - 1; ++i) {
        let newColumnHeaderMatrix = document.createElement("th");
        newColumnHeaderMatrix.setAttribute("scope", "col");
        newColumnHeaderMatrix.innerText = `x${i + 1}`;
        matrixHeader.append(newColumnHeaderMatrix);
    }
    let newColumnHeaderMatrix = document.createElement("th");
    newColumnHeaderMatrix.setAttribute("scope", "col");
    newColumnHeaderMatrix.innerText = `b`;
    matrixHeader.append(newColumnHeaderMatrix);
}

function loadFunction() {
    let functionIndices = parseJsonField("functionJson");
    let functionCoefficients = getAndClearField("function-coefficients");
    for (let i = 0; i < functionIndices.length; ++i) {
        functionCoefficients.append(
            createCell(cellTemplate, functionIndices[i])
        );
    }

    let functionHeader = getAndClearField("function-header");
    for (let i = 0; i < functionIndices.length - 1; ++i) {
        let newColumnHeaderFunction = document.createElement("th");
        newColumnHeaderFunction.setAttribute("scope", "col");
        newColumnHeaderFunction.innerText = `x${i + 1}`;
        functionHeader.append(newColumnHeaderFunction);
    }
    let newColumnHeaderFunction = document.createElement("th");
    newColumnHeaderFunction.setAttribute("scope", "col");
    newColumnHeaderFunction.innerText = `b`;
    functionHeader.append(newColumnHeaderFunction);
}

processFree();
loadData();
