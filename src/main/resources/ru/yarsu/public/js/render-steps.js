function loadStepData() {
    for (let i = 0; i < stepsToRender.length; ++i) {
        loadStepMatrix(stepsToRender[i]);
        loadStepFunction(stepsToRender[i]);
    }
}

function loadStepMatrix(currentStepId) {
    let matrixIndices = parseJsonField(`matrixJson-${currentStepId}`);
    let matrixRows = getAndClearField(`matrix-rows-${currentStepId}`);
    for (let i = 0; i < matrixIndices.length; ++i) {
        let newRow = document.createElement("tr");
        for (let j = 0; j < matrixIndices[0].length; ++j) {
            newRow.append(createCell(cellTemplate, matrixIndices[i][j]));
        }
        matrixRows.append(newRow);
    }
    let matrixHeader = getAndClearField(`matrix-header-${currentStepId}`);
    let indices = JSON.parse(
        document.getElementById(`basisJson-${currentStepId}`).value
    );
    indices.push.apply(
        indices,
        JSON.parse(document.getElementById(`freeJson-${currentStepId}`).value)
    );
    for (let i = 0; i < indices.length; ++i) {
        let newColumnHeaderMatrix = document.createElement("th");
        newColumnHeaderMatrix.setAttribute("scope", "col");
        newColumnHeaderMatrix.innerText = `x${indices[i] + 1}`;
        matrixHeader.append(newColumnHeaderMatrix);
    }
    let newColumnHeaderMatrix = document.createElement("th");
    newColumnHeaderMatrix.setAttribute("scope", "col");
    newColumnHeaderMatrix.innerText = `b`;
    matrixHeader.append(newColumnHeaderMatrix);
}

function loadStepFunction(currentStepId) {
    let functionIndices = parseJsonField(`functionJson-${currentStepId}`);
    let newRow = document.createElement("tr");
    let indices = JSON.parse(
        document.getElementById(`basisJson-${currentStepId}`).value
    );
    indices.push.apply(
        indices,
        JSON.parse(document.getElementById(`freeJson-${currentStepId}`).value)
    );
    indices.push(indices.length);
    for (let i = 0; i < functionIndices.length; ++i) {
        newRow.append(createCell(cellTemplate, functionIndices[indices[i]]));
    }
    document.getElementById(`matrix-rows-${currentStepId}`).append(newRow);
}

loadStepData();
