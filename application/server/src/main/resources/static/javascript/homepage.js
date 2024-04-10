// Refresh the page by this interval in milliseconds
var pageRefreshInterval = requestPeriod ?? 5000;

// Removing the DIV by UUID and all its children
function removeDiv(uuid) {

    const divToRemove = document.getElementById(uuid);
    document.getElementById(uuid).parentNode.removeChild(divToRemove);

}

// Display control lists
var lastRaspberryPiUUIDs = [];
var lastESP32UUIDs = [];
var newRaspberryPiUUIDs = [];
var newESP32UUIDs = [];

// Check and return value regarding type of the value
function checkTypeAndReturnValue(value, valueType) {

    // If values is a float round it to two decimals, if it's boolean return ON or OFF and if neither just return the variable
    if(valueType === 'VAL_FLOAT') {
        return value.toFixed(2);
    } else if(valueType === 'VAL_BOOLEAN') {
        if(value === 0.0) {
            return 'OFF'
        } else {
            return 'ON'
        }
    } else {
        return Math.round(value);
    }

}

// Check if data is in acceptable ranges
function checkIfInRange(value, valueMin, valueMax) {

    if(value >= valueMin && value <= valueMax) return true;
    else return false;

}

// Parse string to value
function parseValue(valueString, valueMinString, valueMaxString, valueType) {

    let valueMin = parseFloat(valueMinString);
    let valueMax = parseFloat(valueMaxString);

    // Parse the value
    let value;
    if(valueType === 'VAL_FLOAT') {

        value = parseFloat(valueString);
        if(value === null) {
            alert('Please enter a decimal number');
            return null;
        }

    } else {

        value = parseInt(valueString);
        if(value === null) {
            alert('Please enter a whole number');
            return null;
        }

    }

    // Check range
    if(checkIfInRange(value, valueMin, valueMax)) return value;
    else return null;

}

// Convert into JSON
function convertIntoJSON(subStrings, value) {

    // Assign the values
    let raspberryPiUUID = subStrings[0];
    let esp32UUID = subStrings[1];
    let id = subStrings[2];

    // Create a JSON
    let dataToSend = { "raspberryPiUUID":raspberryPiUUID, "esp32UUID":esp32UUID, "id":id, "value":value };
    let jsonToSend = JSON.stringify(dataToSend);

    return jsonToSend;

}

// Send the JSON
function postJSON(jsonToSend) {

    // POST the JSON
    fetch('/post', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: jsonToSend
    })
    .then(response => {
        if(!response.ok) {
            console.error('Server returned error status: ', response.status);
        }
    })
    .catch(error => {
        console.error('Error posting data:', error);
    });

}

// For set button
function postDataValue(valueInfo) {

    // Divide the string
    let subStrings = valueInfo.split(':');
    let valueType = subStrings[3];

    // Get the current value in the text box
    let valueLocation = subStrings.slice(0, -1).join(':');
    let valueString = document.getElementById(valueLocation).value;

    // Get minimum and maximum allowed value
    let valueMinString = document.getElementById(valueLocation).getAttribute('data-value-min');
    let valueMaxString = document.getElementById(valueLocation).getAttribute('data-value-max');

    // Parse data
    value = parseValue(valueString, valueMinString, valueMaxString, valueType);
    if(value === null) return;

    // Convert into JSON
    let jsonToSend = convertIntoJSON(subStrings, value);
    if(jsonToSend === null) {
        console.error('Error in JSON conversion');
        return;
    }

    // POST data
    postJSON(jsonToSend);

}

// Get the new boolean value
function getNewValueFromBoolean(valueBoolean) {

    if(valueBoolean === 'ON') return 0.0;
    else return 1.0;

}

// For ON/OFF button
function postDataBoolean(valueLocation) {

    // Divide the string
    let subStrings = valueLocation.split(':');

    // Get the current value ON/OFF and set the opposite of that
    let valueBoolean = document.getElementById(valueLocation).textContent;
    let value = getNewValueFromBoolean(valueBoolean);

    // Create a JSON
    let jsonToSend = convertIntoJSON(subStrings, value);
    if(jsonToSend === null) {
        console.error('Error in JSON conversion');
        return;
    }

    // POST data
    postJSON(jsonToSend);

}

// Value send button
function buildActuateValue(raspberryPiUUID, esp32UUID, id, valueType, valueMin, valueMax) {

    // Create the sub-DIV
    const divActuate = document.createElement('div');
    divActuate.classList.add('measuring-component-actuate');

    // Create the sub-sub-DIV
    const divValue = document.createElement('div');
    divValue.classList.add('measuring-component-actuate-value');

    // Create the sub-sub-sub-DIV
    const buttonValue = document.createElement('button');
    buttonValue.textContent = `Set [${valueMin} to ${valueMax}]`;
    buttonValue.setAttribute('onclick', `postDataValue('${raspberryPiUUID}:${esp32UUID}:${id}:${valueType}')`);

    // Create the sub-sub-sub-DIV
    const inputValue = document.createElement('input');
    inputValue.setAttribute('type', 'text');
    inputValue.classList.add('measuring-component-actuate-input');
    inputValue.setAttribute('id', `${raspberryPiUUID}:${esp32UUID}:${id}`);
    inputValue.setAttribute('name', 'number input');
    inputValue.setAttribute('data-value-min', `${valueMin}`);
    inputValue.setAttribute('data-value-max', `${valueMax}`);

    // Append the sub-sub-sub-DIV's to the sub-sub-DIV
    divValue.appendChild(buttonValue);
    divValue.appendChild(inputValue);

    // Append the sub-sub-DIV to the sub-DIV
    divActuate.appendChild(divValue);

    // Append the sub-DIV to the current DIV
    document.getElementById(`${esp32UUID}:${id}`).appendChild(divActuate);

}

// Boolean ON/OFF button
function buildActuateBoolean(raspberryPiUUID, esp32UUID, id) {

    // Create the sub-DIV
    const divActuate = document.createElement('div');
    divActuate.classList.add('measuring-component-actuate');

    // Create the sub-sub-DIV
    const divBoolean = document.createElement('div');
    divBoolean.classList.add('measuring-component-actuate-boolean');

    // Create the sub-sub-sub-DIV
    const buttonBoolean = document.createElement('button');
    buttonBoolean.textContent = 'ON/OFF';
    buttonBoolean.setAttribute('onclick', `postDataBoolean('${raspberryPiUUID}:${esp32UUID}:${id}:value')`);

    // Append the sub-sub-sub-DIV to the sub-sub-DIV
    divBoolean.appendChild(buttonBoolean);

    // Append the sub-sub-DIV to the sub-DIV
    divActuate.appendChild(divBoolean);

    // Append the sub-DIV to the current DIV
    document.getElementById(`${esp32UUID}:${id}`).appendChild(divActuate);

}

// No button
function buildNoActuate(esp32UUID, id) {

    // Create the sub-DIV
    const divActuate = document.createElement('div');
    divActuate.classList.add('measuring-component-actuate');

    // Append the sub-DIV to the current DIV
    document.getElementById(`${esp32UUID}:${id}`).appendChild(divActuate);

}

// Build the measurement DIV
function buildMeasuringComponent(raspberryPiUUID, esp32UUID, measuringComponent) {

    // Create the sub-DIV
    const divMeasuringComponent = document.createElement('div');
    divMeasuringComponent.classList.add('measuring-component');
    divMeasuringComponent.setAttribute('id', `${esp32UUID}:${measuringComponent.id}`);

    // Create the sub-sub-DIV
    const divMeasuringComponentDisplay = document.createElement('div');
    divMeasuringComponentDisplay.classList.add('measuring-component-display');

    // Create the sub-sub-sub-SPAN
    const divMeasuringComponentName = document.createElement('span');
    divMeasuringComponentName.classList.add('measuring-component-name');
    divMeasuringComponentName.textContent = measuringComponent.name + ': ';

    // Create the sub-sub-sub-SPAN
    const divMeasuringComponentValue = document.createElement('span');
    divMeasuringComponentValue.classList.add('measuring-component-value');
    divMeasuringComponentValue.setAttribute('id', `${raspberryPiUUID}:${esp32UUID}:${measuringComponent.id}:value`);
    divMeasuringComponentValue.setAttribute('data-component-type', `${measuringComponent.componentType}`);
    divMeasuringComponentValue.textContent = checkTypeAndReturnValue(measuringComponent.value, measuringComponent.valueType);

    // Append the sub-sub-sub-SPAN to the sub-sub-DIV
    divMeasuringComponentDisplay.appendChild(divMeasuringComponentName);
    divMeasuringComponentDisplay.appendChild(divMeasuringComponentValue);

    // Append the sub-sub-DIV to the sub-DIV
    divMeasuringComponent.appendChild(divMeasuringComponentDisplay);

    // Append sub-sub-DIV's to sub-DIV's depending on the type od measurement
    if(measuringComponent.componentType === 'COMPONENT_SENSOR') {

        // Append the sub-DIV to the current DIV
        document.getElementById(`${esp32UUID}:measuring-component-list`).appendChild(divMeasuringComponent);
        buildNoActuate(esp32UUID, measuringComponent.id);

    } else {

        if(measuringComponent.valueType === 'VAL_BOOLEAN') {

            // Append the sub-DIV to the current DIV
            document.getElementById(`${esp32UUID}:measuring-component-list`).appendChild(divMeasuringComponent);
            buildActuateBoolean(raspberryPiUUID, esp32UUID, measuringComponent.id);

        } else {

            // Append the sub-DIV to the current DIV
            document.getElementById(`${esp32UUID}:measuring-component-list`).appendChild(divMeasuringComponent);
            buildActuateValue(raspberryPiUUID, esp32UUID, measuringComponent.id, measuringComponent.valueType, measuringComponent.valueMin, measuringComponent.valueMax);

        }

    }

}

// Create new measurement list DIV
function buildMeasuringComponentList(raspberryPiUUID, esp32UUID, measuringComponentList) {

    // Create the sub-DIV
    const divMeasuringComponentList = document.createElement('div');
    divMeasuringComponentList.classList.add('measuring-component-list');
    divMeasuringComponentList.setAttribute('id', `${esp32UUID}:measuring-component-list`);

    // Append the sub-DIV to the current DIV
    document.getElementById(`${esp32UUID}`).appendChild(divMeasuringComponentList);

    // Update the sub-DIV by adding more sub-DIV's
    measuringComponentList.forEach((measuringComponent) => {

        buildMeasuringComponent(raspberryPiUUID, esp32UUID, measuringComponent);

    });

}

// Just update existing reports
function updateMeasurementList(raspberryPiUUID, esp32UUID, measuringComponentList) {

    // Iterate over the connected sensors and actuators and assign them their new arrived values
    measuringComponentList.forEach(measuringComponent => {

        document.getElementById(`${raspberryPiUUID}:${esp32UUID}:${measuringComponent.id}:value`).textContent = checkTypeAndReturnValue(measuringComponent.value, measuringComponent.valueType);

    });

}

// Build the ESP32 DIV
function buildESP32Report(raspberryPiUUID, esp32Report) {

    // Create the sub-DIV
    const divESP32Report = document.createElement('div');
    divESP32Report.classList.add('esp32-report');
    divESP32Report.setAttribute('id', `${esp32Report.uuid}`);

    // Create the sub-sub-DIV
    const h3ESP32ReportName = document.createElement('h3');
    h3ESP32ReportName.classList.add('esp32-report-name');
    h3ESP32ReportName.textContent = esp32Report.name;

    // Append the sub-sub-DIV's to the sub-DIV
    divESP32Report.appendChild(h3ESP32ReportName);

    // Add to the current DIV
    document.getElementById(`${raspberryPiUUID}:esp32-report-list`).appendChild(divESP32Report);

    // Update the sub-sub-DIV by adding more sub-DIV's
    buildMeasuringComponentList(raspberryPiUUID, esp32Report.uuid, esp32Report.measuringComponentList);

}

// Add new report or update existing
function updateESP32ReportList(raspberryPiUUID, esp32ReportList) {

    // Iterate on the connected ESP32's
    esp32ReportList.forEach(esp32Report => {

        if(document.getElementById(`${esp32Report.uuid}`) === null) {

            // Build the lower DIR
            buildESP32Report(raspberryPiUUID, esp32Report);

            // Add new element in the new list
            newESP32UUIDs.push(esp32Report.uuid);

        } else {

            // Update the already existing structure
            updateMeasurementList(raspberryPiUUID, esp32Report.uuid, esp32Report.measuringComponentList);

            // Add new element in the new list
            newESP32UUIDs.push(esp32Report.uuid);

            // Remove element from the past list
            lastESP32UUIDs = lastESP32UUIDs.filter(uuid => uuid !== esp32Report.uuid);

        }


    });

}

// Build the Raspberry Pi DIV
function buildRaspberryPiReport(raspberryPiReport) {

    // Create the sub-DIV
    const divRaspberryPiReport = document.createElement('div');
    divRaspberryPiReport.classList.add('raspberry-pi-report');
    divRaspberryPiReport.setAttribute('id', `${raspberryPiReport.uuid}`);

    // Create the sub-sub-DIV
    const h2RaspberryPiReportName = document.createElement('h2');
    h2RaspberryPiReportName.classList.add('raspberry-pi-report-name');
    h2RaspberryPiReportName.textContent = raspberryPiReport.name;

    // Create another sub-sub-DIV
    const divESP32ReportList = document.createElement('div');
    divESP32ReportList.classList.add('esp32-report-list');

    // Set the unique ID so that the DIV can be refrenced
    divESP32ReportList.setAttribute('id', `${raspberryPiReport.uuid}:esp32-report-list`);

    // Append the sub-sub-DIV's to the sub-DIV
    divRaspberryPiReport.appendChild(h2RaspberryPiReportName);
    divRaspberryPiReport.appendChild(divESP32ReportList);

    // Add to the current DIV
    document.getElementById(`raspberry-pi-report-list`).appendChild(divRaspberryPiReport);

    // Update the sub-sub-DIV by adding more sub-DIV's
    updateESP32ReportList(raspberryPiReport.uuid, raspberryPiReport.esp32ReportList);

}

// Add new report or update existing
function updateRaspberryPiReportList(raspberryPiReportList) {

    // Iterate on the connected Raspberry Pi's
    raspberryPiReportList.forEach(raspberryPiReport => {

        if(document.getElementById(`${raspberryPiReport.uuid}`) === null) {

            // Build the lower DIR
            buildRaspberryPiReport(raspberryPiReport);

            // Add new element in the new list
            newRaspberryPiUUIDs.push(raspberryPiReport.uuid);

        } else {

            // Update the already existing structure
            updateESP32ReportList(raspberryPiReport.uuid, raspberryPiReport.esp32ReportList);

            // Add new element in the new list
            newRaspberryPiUUIDs.push(raspberryPiReport.uuid);

            // Remove element from the past list
            lastRaspberryPiUUIDs = lastRaspberryPiUUIDs.filter(uuid => uuid !== raspberryPiReport.uuid);

        }

    });

}

// Function to render the JSON data
function renderData(data) {

    // Now render everything based on the newly arrived JSON
    updateRaspberryPiReportList(data);

    // Remove leftovers from the past, beggining from the lowest in the hierarchy
    lastESP32UUIDs.forEach(esp32UUID => removeDiv(esp32UUID));
    lastRaspberryPiUUIDs.forEach(raspberryPiUUID => removeDiv(raspberryPiUUID));

    // The present is now the past
    lastESP32UUIDs = [...newESP32UUIDs];
    lastRaspberryPiUUIDs = [...newRaspberryPiUUIDs];

    // The present is now empty, waiting for new actions
    newESP32UUIDs = [];
    newRaspberryPiUUIDs = [];

}

// Function to fetch JSON data and render it
function fetchDataAndRender() {

    // GET endpoint
    fetch('/refresh')
        .then(response => response.json())
        .then(data => renderData(data))
        .catch(error => console.error('Error fetching data:', error));

}

// Initial rendering
fetchDataAndRender();

// Update every second
setInterval(fetchDataAndRender, pageRefreshInterval);