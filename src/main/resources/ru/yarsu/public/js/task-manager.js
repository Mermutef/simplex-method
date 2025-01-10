const cellTemplate = '<input class="form-control" type="text" value="0">';

function updateVariablesState() {
    if (document.getElementById("method").value === "synthetic-basis") {
        document
            .getElementById("variablesContainer")
            .setAttribute("hidden", "hidden");
    } else {
        document.getElementById("variablesContainer").removeAttribute("hidden");
    }
    console.log(document.getElementById("method").value === "synthetic-basis")
}

function stepByStepChoosing() {
    if (document.getElementById("stepByStep").checked) {
        document.getElementById("taskForm").setAttribute("action", "/step-by-step");
    } else {
        document.getElementById("taskForm").setAttribute("action", "/");
    }
}

function createCell(template, newCellValue = 0) {
    let newCell = document.createElement("td");
    newCell.innerHTML = template;
    newCell.getElementsByTagName("input")[0].value = newCellValue + "";
    return newCell;
}

function createBasisIdx(variableIdx) {
    let div = document.createElement("div");
    div.className = "form-check form-check-inline p-0 m-1";
    let input = document.createElement("input");
    input.type = "checkbox";
    input.className = "btn-check";
    input.name = "basis";
    input.id = `variable-idx-${variableIdx}`;
    input.setAttribute("autocomplete", "off");
    input.value = variableIdx;
    let label = document.createElement("label");
    label.className = "btn btn-outline-primary underline";
    label.setAttribute("for", `variable-idx-${variableIdx}`);
    label.innerHTML = `x<sub>${variableIdx + 1}</sub>`;
    div.append(input);
    div.append(label);
    input.addEventListener("change", updateBasisIndices);
    return div;
}

function createRow(n, template) {
    let newRow = document.createElement("tr");
    for (let i = 0; i < n; ++i) {
        newRow.append(createCell(template));
    }
    return newRow;
}

function removeFree() {
    let freeCoefficients = Array.from(
        document.getElementsByName("basis")
    ).filter((elem) => !elem.checked);
    freeCoefficients[freeCoefficients.length - 1].remove();
}

function createTh(headerId, idx, htmlText) {
    let header = document.getElementById(headerId).getElementsByTagName("th");
    let newColumnHeader = document.createElement("th");

    newColumnHeader.setAttribute("scope", "col");
    newColumnHeader.innerHTML = htmlText;
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

    if (m >= n - 1) {
        alert("Число строк не может быть больше числа переменных");
        return;
    }

    matrixRows[m - 1].after(createRow(n, cellTemplate));
    updateBasisIndices();
}

function deleteRow() {
    let matrixRows = selectMatrixRows();
    let m = matrixRows.length;

    if (m <= 1) {
        alert("Должна остаться хотя бы одна строка");
        return;
    }

    matrixRows[m - 1].remove();
    updateBasisIndices();
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

    createTh("function-header", n - 1, `x<sub>${n}</sub>`);
    createTh("matrix-header", n - 1, `x<sub>${n}</sub>`);

    for (let i = 0; i < m; ++i) {
        matrixRows[i]
            .getElementsByTagName("td")
            [n - 1].before(createCell(cellTemplate));
    }

    selectTagsById("function-coefficients", "td")[n - 1].before(
        createCell(cellTemplate)
    );
    document.getElementById("basis-indices").append(createBasisIdx(n - 1));
    updateBasisIndices();
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
    for (let i = 0; i < m; ++i) {
        matrixRows[i].getElementsByTagName("td")[n - 2].remove();
    }
    let basis = document.getElementsByName("basis");
    basis[basis.length - 1].parentNode.remove();
    updateBasisIndices();
}

async function prepareData(sendData = true) {
    prepareBasis();
    prepareMatrix();
    prepareFunction();
    if (sendData) {
        document.getElementById("simplexMethodJson").value = "";
        document.getElementById("syntheticBasisJson").value = "";
        document.getElementById("send-task").click();
    }
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
    let basisInputs = Array.from(document.getElementsByName("basis"))
        .filter((elem) => elem.checked)
        .map((elem) => Number(elem.value));
    writeToJsonField("basisJson", basisInputs);
}

function parseJsonField(fieldId) {
    return JSON.parse(document.getElementById(fieldId).value);
}

function getAndClearField(fieldId) {
    let field = document.getElementById(fieldId);
    field.innerHTML = "";
    return field;
}

function loadData() {
    loadMatrix();
    loadFunction();
    loadBasisIndices();
    updateVariablesState();
}

function loadBasisIndices() {
    let n = selectHeader("matrix-header").length;
    let field = getAndClearField("basis-indices");
    for (let i = 0; i < n - 1; ++i) {
        field.append(createBasisIdx(i));
    }
    let indices = parseJsonField("basisJson");
    for (let i = 0; i < indices.length; ++i) {
        document.getElementById(`variable-idx-${indices[i]}`).checked = true;
    }
    updateBasisIndices();
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
        newColumnHeaderMatrix.innerHTML = `x<sub>${i + 1}</sub>`;
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
        newColumnHeaderFunction.innerHTML = `x<sub>${i + 1}</sub>`;
        functionHeader.append(newColumnHeaderFunction);
    }
    let newColumnHeaderFunction = document.createElement("th");
    newColumnHeaderFunction.setAttribute("scope", "col");
    newColumnHeaderFunction.innerText = `b`;
    functionHeader.append(newColumnHeaderFunction);
}

function updateBasisIndices(e) {
    let m = selectMatrixRows().length;
    let selectedBasis = Array.from(document.getElementsByName("basis")).filter(
        (elem) => elem.checked
    );
    if (selectedBasis.length >= m) {
        let allIndices = Array.from(document.getElementsByName("basis")).filter(
            (elem) => !elem.checked
        );
        for (let j = 0; j < allIndices.length; ++j) {
            allIndices[j].setAttribute("disabled", "disabled");
        }
        for (let j = m; j < selectedBasis.length; ++j) {
            selectedBasis[j].checked = false;
            selectedBasis[j].setAttribute("disabled", "disabled");
        }
    } else if (selectedBasis.length <= m - 1) {
        let allIndices = Array.from(document.getElementsByName("basis")).filter(
            (elem) => !elem.checked
        );
        for (let j = 0; j < allIndices.length; ++j) {
            allIndices[j].removeAttribute("disabled");
        }
    }
}

function addListeners() {
    let elems = document.getElementsByName("basis");
    for (let i = 0; i < elems.length; ++i) {
        elems[i].addEventListener("change", updateBasisIndices);
    }
}

async function saveToFile() {
    await prepareData();
    let form = document.getElementById("taskForm");
    let data = new URLSearchParams(new FormData(form));
    fetch("/save-to-file", {
        method: "POST",
        body: data,
    })
        .then((response) => response.json())
        .then((json) =>
            saveAs(
                new Blob([JSON.stringify(json, null, 4)], {
                    type: "application/json;charset=" + document.characterSet,
                }),
                "task.json"
            )
        );
}

function processSelection(direction) {
    document.getElementById('taskForm').setAttribute('action', `/${direction}`);
    prepareData(false);
    document.getElementById("send-task").click();
}

window.addEventListener('load', function () {
    loadData();
    addListeners();
    updateBasisIndices();
    updateVariablesState();
    stepByStepChoosing();
})
