var cellTemplate = '<input class="form-control" type="text" value="0">';
var basisTemplate =
    '<input name="basis" class="form-control" type="number" value="0">';
var freeTemplate =
    '<input name="free" class="form-control" type="number" value="0">';

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
    newCell.innerHTML = basisTemplate;
    document.getElementById("basis-coefficients").append(newCell);

    document
        .getElementById("free-coefficients")
        .getElementsByTagName("td")
        [n - m - 2].remove();
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

    let newCell2 = document.createElement("td");
    newCell2.innerHTML = freeTemplate;
    document.getElementById("free-coefficients").append(newCell2);
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
    let newCell2 = document.createElement("td");
    newCell2.innerHTML = freeTemplate;
    document.getElementById("free-coefficients").append(newCell2);
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
    document
        .getElementById("free-coefficients")
        .getElementsByTagName("td")
        [n - m - 2].remove();
    for (let i = 0; i < m; ++i) {
        let row = matrixRows[i].getElementsByTagName("td");
        row[n - 2].remove();
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
    let matrixRows = document
        .getElementById("matrix-rows")
        .getElementsByTagName("tr");
    let matrix = [];
    for (let i = 0; i < matrixRows.length; ++i) {
        let row = [];
        let matrixRow = matrixRows[i].getElementsByTagName("input");
        for (let j = 0; j < matrixRow.length; ++j) {
            row.push(matrixRow[j].value);
        }
        matrix.push(row);
    }
    document.getElementById("matrixJson").value = JSON.stringify(matrix);
}

function prepareFunction() {
    let functionInputs = document
        .getElementById("function-coefficients")
        .getElementsByTagName("input");
    let fun = [];
    for (let i = 0; i < functionInputs.length; ++i) {
        fun.push(functionInputs[i].value);
    }
    document.getElementById("functionJson").value = JSON.stringify(fun);
}

function prepareBasis() {
    let basisInputs = document.getElementsByName("basis");
    let basisIndices = [];
    for (let i = 0; i < basisInputs.length; ++i) {
        basisIndices.push(Number(basisInputs[i].value));
    }
    document.getElementById("basisJson").value = JSON.stringify(basisIndices);
}

function prepareFree() {
    let checkValue = document.getElementById("inputFree").checked;
    if (checkValue) {
        let freeInputs = document.getElementsByName("free");
        let freeIndices = [];
        for (let i = 0; i < freeInputs.length; ++i) {
            freeIndices.push(Number(freeInputs[i].value));
        }
        document.getElementById("freeJson").value = JSON.stringify(freeIndices);
    } else {
        document.getElementById("freeJson").value = JSON.stringify([]);
    }
}

function processFree() {
    let checkValue = document.getElementById("inputFree").checked;
    if (!checkValue) {
        document.getElementById("freeTable").setAttribute("hidden", "hidden");
    } else {
        document.getElementById("freeTable").removeAttribute("hidden");
    }
}

function loadData() {
    loadBasis();
    loadFree();
    loadMatrix();
    loadFunction();
}

function loadBasis() {
    let basisIndices = JSON.parse(document.getElementById("basisJson").value);
    let basisField = document.getElementById("basis-coefficients");
    basisField.innerHTML = "";
    for (let i = 0; i < basisIndices.length; ++i) {
        let basisIdx = basisIndices[i];
        let newCell = document.createElement("td");
        newCell.innerHTML = basisTemplate;
        newCell.getElementsByTagName("input")[0].value = basisIdx;
        basisField.append(newCell);
    }
}

function loadFree() {
    let freeIndices = JSON.parse(document.getElementById("freeJson").value);
    let freeField = document.getElementById("free-coefficients");
    freeField.innerHTML = "";
    for (let i = 0; i < freeIndices.length; ++i) {
        let freeIdx = freeIndices[i];
        let newCell = document.createElement("td");
        newCell.innerHTML = freeTemplate;
        newCell.getElementsByTagName("input")[0].value = freeIdx;
        freeField.append(newCell);
    }
}

function loadMatrix() {
    let matrixIndices = JSON.parse(document.getElementById("matrixJson").value);
    let matrixRows = document.getElementById("matrix-rows");
    matrixRows.innerHTML = "";
    for (let i = 0; i < matrixIndices.length; ++i) {
        let newRow = document.createElement("tr");
        for (let j = 0; j < matrixIndices[0].length; ++j) {
            let matrixIdx = matrixIndices[i][j];
            let newCell = document.createElement("td");
            newCell.innerHTML = cellTemplate;
            newCell.getElementsByTagName("input")[0].value = matrixIdx;
            newRow.append(newCell);
        }
        matrixRows.append(newRow);
    }

    let matrixHeader = document.getElementById("matrix-header");
    matrixHeader.innerHTML = "";
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
    let functionIndices = JSON.parse(
        document.getElementById("functionJson").value
    );
    let functionCoefficients = document.getElementById("function-coefficients");
    functionCoefficients.innerHTML = "";
    for (let i = 0; i < functionIndices.length; ++i) {
        let functionCoefficient = functionIndices[i];
        let newCell = document.createElement("td");
        newCell.innerHTML = cellTemplate;
        newCell.getElementsByTagName("input")[0].value = functionCoefficient;
        functionCoefficients.append(newCell);
    }

    let functionHeader = document.getElementById("function-header");
    functionHeader.innerHTML = "";
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
