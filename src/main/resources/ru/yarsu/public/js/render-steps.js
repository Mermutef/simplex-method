var cellTemplateDisabled =
    '<input class="form-control" type="text" value="0" readonly>';

function loadStepData() {
    for (let i = 0; i < stepsToRender.length; ++i) {
        loadStepMatrix(
            stepsToRender[i].idx,
            stepsToRender[i].s,
            stepsToRender[i].r
        );
        loadStepFunction(stepsToRender[i].idx, stepsToRender[i].s);
    }
}

function loadStepMatrix(currentStepId, s, r) {
    let matrixIndices = parseJsonField(`matrixJson-${currentStepId}`);
    let matrixRows = getAndClearField(`matrix-rows-${currentStepId}`);
    let basis = JSON.parse(
        document.getElementById(`basisJson-${currentStepId}`).value
    );
    let free = JSON.parse(
        document.getElementById(`freeJson-${currentStepId}`).value
    );
    let indices = basis.slice();
    indices.push.apply(indices, free);
    indices.push(indices.length);
    console.log(indices);
    console.log(basis);
    console.log(free);
    for (let i = 0; i < basis.length; ++i) {
        let newRow = document.createElement("tr");
        let basisIdx = document.createElement("th");
        basisIdx.setAttribute("scope", "col");
        basisIdx.innerText = `x${basis[i] + 1}`;
        if (basis[i] == r) {
            basisIdx.classList.add("table-primary");
        }
        newRow.append(basisIdx);
        for (let j = 0; j < free.length; ++j) {
            let newCell = createCell(
                cellTemplateDisabled,
                matrixIndices[i][indices.indexOf(free[j])]
            );
            if (basis[i] == r && free[j] == s) {
                newCell.classList.add("table-danger");
            } else if (basis[i] == r || free[j] == s) {
                newCell.classList.add("table-primary");
            }
            newRow.append(newCell);
        }
        let newCell = createCell(
            cellTemplateDisabled,
            matrixIndices[i][indices.length - 1]
        );
        if (basis[i] == r) {
            newCell.classList.add("table-primary");
        }
        newRow.append(newCell);
        matrixRows.append(newRow);
    }
    let matrixHeader = getAndClearField(`matrix-header-${currentStepId}`);
    matrixHeader.append(document.createElement("th"));
    for (let i = 0; i < free.length; ++i) {
        let newColumnHeaderMatrix = document.createElement("th");
        newColumnHeaderMatrix.setAttribute("scope", "col");
        newColumnHeaderMatrix.innerText = `x${free[i] + 1}`;
        if (free[i] == s) {
            newColumnHeaderMatrix.classList.add("table-primary");
        }
        matrixHeader.append(newColumnHeaderMatrix);
    }
    let newColumnHeaderMatrix = document.createElement("th");
    newColumnHeaderMatrix.setAttribute("scope", "col");
    newColumnHeaderMatrix.innerText = `b`;
    matrixHeader.append(newColumnHeaderMatrix);
}

function loadStepFunction(currentStepId, s) {
    let functionIndices = parseJsonField(`functionJson-${currentStepId}`);
    let newRow = document.createElement("tr");
    let free = JSON.parse(
        document.getElementById(`freeJson-${currentStepId}`).value
    );
    free.push(functionIndices.length - 1);
    let basisIdx = document.createElement("th");
    basisIdx.setAttribute("scope", "col");
    basisIdx.innerText = `f`;
    newRow.append(basisIdx);
    for (let i = 0; i < free.length; ++i) {
        let newCell = createCell(
            cellTemplateDisabled,
            functionIndices[free[i]]
        );
        if (free[i] == s) {
            newCell.classList.add("table-primary");
        }
        newRow.append(newCell);
    }
    document.getElementById(`matrix-rows-${currentStepId}`).append(newRow);
}

loadStepData();
