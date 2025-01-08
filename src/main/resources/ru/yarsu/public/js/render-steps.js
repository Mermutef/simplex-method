var cellTemplateDisabled =
	'<input class="form-control" type="text" value="0" readonly>';

function loadStepData() {
	console.log(syntheticSteps.map((elem) => elem.r));
	console.log(syntheticSteps.map((elem) => elem.s));
	for (let i = 0; i < syntheticSteps.length; ++i) {
		if (i > 0) {
			loadStepMatrix(
				syntheticSteps[i].idx,
				syntheticSteps[i].s,
				syntheticSteps[i].r,
				syntheticSteps
					.slice(0, i)
					.map((elem) => elem.r)
					.filter(
						(elem) =>
							!(
								elem in
								syntheticSteps
									.slice(0, i + 1)
									.map((elem) => elem.s)
							)
					),
				"synthetic-"
			);
			loadStepFunction(
				syntheticSteps[i].idx,
				syntheticSteps[i].s,
				syntheticSteps[i].r,
				syntheticSteps
					.slice(0, i)
					.map((elem) => elem.r)
					.filter(
						(elem) =>
							!(
								elem in
								syntheticSteps
									.slice(0, i + 1)
									.map((elem) => elem.s)
							)
					),
				"synthetic-"
			);
		} else {
			loadStepMatrix(
				syntheticSteps[i].idx,
				syntheticSteps[i].s,
				syntheticSteps[i].r,
				[],
				"synthetic-"
			);
			loadStepFunction(
				syntheticSteps[i].idx,
				syntheticSteps[i].s,
				syntheticSteps[i].r,
				[],
				"synthetic-"
			);
		}
	}
	for (let i = 0; i < trueSteps.length; ++i) {
		loadStepMatrix(
			trueSteps[i].idx,
			trueSteps[i].s,
			trueSteps[i].r,
			[],
			""
		);
		loadStepFunction(
			trueSteps[i].idx,
			trueSteps[i].s,
			trueSteps[i].r,
			[],
			""
		);
	}
}

function loadStepMatrix(currentStepId, s, r, prevRs, prefix = "") {
	let matrixIndices = parseJsonField(`${prefix}matrixJson-${currentStepId}`);
	let matrixRows = getAndClearField(`${prefix}matrix-rows-${currentStepId}`);
	let basis = JSON.parse(
		document.getElementById(`${prefix}basisJson-${currentStepId}`).value
	);
	let free = JSON.parse(
		document.getElementById(`${prefix}freeJson-${currentStepId}`).value
	);
	let indices = basis.slice();
	indices.push.apply(indices, free);
	let trueIndices = parseJsonField(`basisJson`);
	trueIndices.push.apply(trueIndices, parseJsonField(`freeJson`));
	indices.push(indices.length);
	for (let i = 0; i < basis.length; ++i) {
		let newRow = document.createElement("tr");
		let basisIdx = document.createElement("th");
		basisIdx.setAttribute("scope", "col");
		basisIdx.innerHTML = `x<sub>${basis[i] + 1}</sub>`;
		if (basis[i] == r) {
			basisIdx.classList.add("table-primary");
		}
		newRow.append(basisIdx);
		for (let j = 0; j < free.length; ++j) {
			if (trueIndices.includes(free[j]) || !prevRs.includes(free[j])) {
				let newCell = createCell(
					cellTemplateDisabled,
					matrixIndices[i][indices.indexOf(free[j])]
				);
				if (basis[i] == r && free[j] == s) {
					newCell.classList.add("table-danger");
					newCell
						.getElementsByTagName("input")[0]
						.classList.add("input-danger");
				} else if (basis[i] == r || free[j] == s) {
					newCell.classList.add("table-primary");
					newCell
						.getElementsByTagName("input")[0]
						.classList.add("input-primary");
				}
				newRow.append(newCell);
			}
		}
		let newCell = createCell(
			cellTemplateDisabled,
			matrixIndices[i][indices.length - 1]
		);
		if (basis[i] == r) {
			newCell.classList.add("table-primary");
			newCell
				.getElementsByTagName("input")[0]
				.classList.add("input-primary");
		}
		newRow.append(newCell);
		matrixRows.append(newRow);
	}
	let matrixHeader = getAndClearField(
		`${prefix}matrix-header-${currentStepId}`
	);
	let newColumnHeaderMatrix = document.createElement("th");
	newColumnHeaderMatrix.setAttribute("scope", "col");
	newColumnHeaderMatrix.innerHTML = `x<sup>(${currentStepId})</sup>`;
	matrixHeader.append(newColumnHeaderMatrix);
	for (let i = 0; i < free.length; ++i) {
		if (trueIndices.includes(free[i]) || !prevRs.includes(free[i])) {
			let newColumnHeaderMatrix = document.createElement("th");
			newColumnHeaderMatrix.setAttribute("scope", "col");
			newColumnHeaderMatrix.innerHTML = `x<sub>${free[i] + 1}</sub>`;
			if (free[i] == s) {
				newColumnHeaderMatrix.classList.add("table-primary");
			}
			matrixHeader.append(newColumnHeaderMatrix);
		}
	}
	newColumnHeaderMatrix = document.createElement("th");
	newColumnHeaderMatrix.setAttribute("scope", "col");
	newColumnHeaderMatrix.innerText = `b`;
	matrixHeader.append(newColumnHeaderMatrix);
}

function loadStepFunction(currentStepId, s, r, prevRs, prefix = "") {
	console.log("UPD\n");
	let functionIndices = parseJsonField(
		`${prefix}functionJson-${currentStepId}`
	);
	let newRow = document.createElement("tr");
	let free = JSON.parse(
		document.getElementById(`${prefix}freeJson-${currentStepId}`).value
	);
	free.push(functionIndices.length - 1);
	let basisIdx = document.createElement("th");
	basisIdx.setAttribute("scope", "col");
	basisIdx.innerText = `f`;
	newRow.append(basisIdx);
	let trueIndices = parseJsonField(`basisJson`);
	trueIndices.push.apply(trueIndices, parseJsonField(`freeJson`));
	trueIndices.push(functionIndices.length - 1);
	for (let i = 0; i < free.length; ++i) {
		console.log(free[i]);
		console.log(trueIndices.includes(free[i]));
		console.log(prevRs.includes(free[i]));
		console.log(trueIndices);
		console.log(prevRs);
		if (trueIndices.includes(free[i]) || !prevRs.includes(free[i])) {
			let newCell = createCell(
				cellTemplateDisabled,
				functionIndices[free[i]]
			);
			if (free[i] == s) {
				newCell.classList.add("table-primary");
				newCell
					.getElementsByTagName("input")[0]
					.classList.add("input-primary");
			}
			newRow.append(newCell);
		}
	}
	document
		.getElementById(`${prefix}matrix-rows-${currentStepId}`)
		.append(newRow);
}

loadStepData();
