// JavaScript code for the web frontend.

// Ensure that the JavaScript code runs after the DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the map.
    const map = L.map('map').setView([37.9838, 23.7275], 13); // initiate on Athens, Greece
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    // Maintain a dictionary of markers (both IoT and users).
    const markers = {};

    // Maintain a count of IoT devices and users.
    let iotCount = 0;
    let userCount = 0;

    // Determines the severity based on sensor thresholds.
    function determineSeverity(data) {
        // Thresholds adjusted to match backend
        const thresholds = {
            smoke: 0.14,
            gas: 9.15,
            temperature: 50.0,
            uv: 6
        };

        const smokeExceeded = data.smoke !== null && data.smoke > thresholds.smoke;
        const gasExceeded = data.gas !== null && data.gas > thresholds.gas;
        const temperatureExceeded = data.temperature !== null && data.temperature > thresholds.temperature;
        const uvExceeded = data.uv !== null && data.uv > thresholds.uv;

        // Mirror the logic from AlertSpotter's `determineSeverity` method
        if (gasExceeded || (gasExceeded && smokeExceeded) || (gasExceeded && smokeExceeded && uvExceeded && temperatureExceeded)) {
            return "High";
        } else if (temperatureExceeded && uvExceeded) {
            return "Moderate";
        } else {
            return "None";
        }
    }

    // Function to create a custom icon with text.
    // Modified to return dimensions along with the data URL
    function createCustomIcon(iconUrl, text, bgColor, callback) {
        const img = new Image();
        img.src = iconUrl;
        img.onload = () => {
            const textHeight = 15; // Space for text
            const canvas = document.createElement('canvas');
            canvas.width = img.width;
            canvas.height = img.height + textHeight;

            const ctx = canvas.getContext('2d');

            if (bgColor !== null) {
                // Draw background circle with transparency
                ctx.beginPath();
                ctx.arc(canvas.width / 2, img.height / 2, img.width / 2, 0, 2 * Math.PI);
                ctx.fillStyle = bgColor === 'red' ? 'rgba(255, 0, 0, 0.3)' : 'rgba(0, 255, 0, 0.3)'; // Lighter and transparent
                ctx.fill();
            }

            // Draw the icon image
            ctx.drawImage(img, 0, 0, img.width, img.height);

            // Text styling
            ctx.font = 'bold 12px Arial';
            ctx.textAlign = 'center';
            ctx.fillStyle = 'black';
            // Position text at the bottom area
            ctx.fillText(text, canvas.width / 2, img.height + textHeight - 5);

            // Pass back both URL and dimensions
            callback({
                iconUrl: canvas.toDataURL(),
                width: canvas.width,
                height: canvas.height
            });
        };
    }

    // Store IoT devices with active danger levels
    const dangerIoTDevices = [];

    // Layer group for dangerous areas (to manage/delete as needed)
    const dangerLayer = L.layerGroup().addTo(map);


    // Update or create a marker for an IoT device.
    function updateIotMarker(data) {
        const id = data.id;

        // Determine the severity based on the sensor values
        const severity = determineSeverity(data);

        // Store devices with "High" or "Moderate" severity
        if (severity === "High" || severity === "Moderate") {
            const existingDeviceIndex = dangerIoTDevices.findIndex(device => device.id === id);
            if (existingDeviceIndex !== -1) {
                dangerIoTDevices[existingDeviceIndex] = { id, lat: data.lat, lng: data.lng, severity };
            } else {
                dangerIoTDevices.push({ id, lat: data.lat, lng: data.lng, severity });
            }            // Show immediate info about this IoT in the side panel
            const message = `
               <b>${severity} Severity Alert!</b><br>
               Device ID: ${data.id}<br>
               Severity: ${severity}<br>
               Location: (${data.lat}, ${data.lng})<br>
               Smoke: ${data.smoke}<br>
               Gas: ${data.gas}<br>
               Temperature: ${data.temperature}<br>
               UV: ${data.uv}
           `;
            updateSeverityInfo(message, true); // Make severity info visible
        } else {
            // Remove the device from the danger list if it no longer has a high or moderate severity
            const index = dangerIoTDevices.findIndex(device => device.id === id);
            if (index !== -1) {
                dangerIoTDevices.splice(index, 1);
            }
            if (dangerIoTDevices.length === 0) {
                updateSeverityInfo('', false); // Hide severity info if there are no more danger devices
            }
        }

        // Clear the danger layer if there are fewer than 2 danger devices
        if (dangerIoTDevices.length < 2) {
            dangerLayer.clearLayers();
        }

        const content = `
        ID: ${id}<br>
        Latitude: ${data.lat}<br>
        Longitude: ${data.lng}<br>
        Battery: ${data.battery}<br>
        Smoke: ${data.smoke}<br>
        Gas: ${data.gas}<br>
        Temperature: ${data.temperature}<br>
        UV: ${data.uv}<br>
        Severity: ${severity} <!-- Display severity -->
    `;

        // Determine the icon based on severity level
        let iconUrl = 'icons/IoT.png'; // Default IoT icon
        if (severity === 'High') {
            iconUrl = 'icons/AlertHigh.png'; // High alert severity icon
        } else if (severity === 'Moderate') {
            iconUrl = 'icons/AlertMid.png'; // Moderate alert severity icon
        }

        // Determine background color (for additional visual cues)
        const sensors = [data.smoke, data.gas, data.temperature, data.uv];
        let bgColor;
        if (sensors.every(sensor => sensor === null)) {
            bgColor = 'red';
        } else {
            bgColor = 'green';
        }

        if (markers[id]) {
            // If marker exists, update it
            if (markers[id].bgColor !== bgColor || markers[id].iconUrl !== iconUrl) {
                createCustomIcon(iconUrl, `IoT${iotCount}`, bgColor, (result) => {
                    const iotIcon = L.icon({
                        iconUrl: result.iconUrl,
                        iconSize: [result.width, result.height],
                        iconAnchor: [result.width / 2, result.height],
                        popupAnchor: [0, -result.height + 10]
                    });
                    markers[id].setIcon(iotIcon);
                    markers[id].bgColor = bgColor; /* Update stored values */
                    markers[id].iconUrl = iconUrl;
                });
            }
            markers[id].setLatLng([data.lat, data.lng]);
            markers[id].getPopup().setContent(content);
        } else {
            // If marker does not exist, create it
            iotCount++;
            document.getElementById('iot-count').textContent = iotCount; // Update the IoT count in the UI
            createCustomIcon(iconUrl, id, bgColor, (result) => {
                const iotIcon = L.icon({
                    iconUrl: result.iconUrl,
                    iconSize: [result.width, result.height],
                    iconAnchor: [result.width / 2, result.height],
                    popupAnchor: [0, -result.height + 10]
                });
                markers[id] = L.marker([data.lat, data.lng], { icon: iotIcon })
                    .addTo(map)
                    .bindPopup(content);
                markers[id].bgColor = bgColor;
                markers[id].iconUrl = iconUrl; /* Store icon URL */
            });
        }

        // Trigger visualization logic if at least two danger devices exist
        if (dangerIoTDevices.length >= 2) {
            visualizeDangerArea();
        }
    }

    // Function to dynamically update the side window with severity details
    function updateSeverityInfo(message, isVisible = true) {
        const severityDiv = document.getElementById('severity-info');
        const severityText = document.getElementById('severity-text');

        if (isVisible) {
            const timestamp = new Date().toLocaleString();
            // Set message and make the notification box visible
            severityText.innerHTML = `${message}<br><small>Time: ${timestamp}</small>`;
            severityDiv.style.display = 'block';
        } else {
            // Hide the notification box if there's no severe event
            severityDiv.style.display = 'none';
            severityText.innerHTML = '';
        }
    }

    // Visualize the danger area when conditions are met
    function visualizeDangerArea() {
        if (dangerIoTDevices.length < 2) return; // Need at least 2 devices

        // Get the two most recent devices from the danger list
        const device1 = dangerIoTDevices[dangerIoTDevices.length - 2];
        const device2 = dangerIoTDevices[dangerIoTDevices.length - 1];

        // Check if the severity combinations meet the conditions:
        // - Both devices are "Moderate", OR
        // - One is "High" and the other is "Moderate", OR
        // - Both devices are "High"
        const validDangerConditions = (
            (device1.severity === "Moderate" && device2.severity === "Moderate") ||
            (device1.severity === "High" && device2.severity === "Moderate") ||
            (device1.severity === "Moderate" && device2.severity === "High") ||
            (device1.severity === "High" && device2.severity === "High")
        );

        if (!validDangerConditions) return; // If conditions aren't met, no visualization

        // Display severity info in the side panel
        let message = '';
        if (device1.id == device2.id) {
            message = `
            <b>Warning!</b><br>
            Dangerous area detected in device:<br>
            - ${device1.id} (Severity: ${device1.severity})<br>
            `;
        } else {
            message = `
            <b>Warning!</b><br>
            Dangerous area detected between devices:<br>
            - ${device1.id} (Severity: ${device1.severity})<br>
            - ${device2.id} (Severity: ${device2.severity})<br>
            `;
        }
        updateSeverityInfo(message, true);

        // Calculate the coordinates of the rectangle's corners
        const bounds = [
            [device1.lat, device1.lng], // First device
            [device1.lat, device2.lng], // Top-right corner
            [device2.lat, device2.lng], // Second device
            [device2.lat, device1.lng], // Bottom-left corner
        ];

        // Clear any existing danger zone before drawing a new one
        dangerLayer.clearLayers();

        // Draw the rectangle (polygon)
        const dangerBlock = L.polygon(bounds, {
            color: '#ff4545',    // Lighter red border
            weight: 1,           // Smaller border width
            fillColor: 'red',    // Red fill
            fillOpacity: 0.3,    // 30% transparency for the fill
        }).addTo(dangerLayer);

        // Bind a popup to the rectangle (popup appears only when the block is clicked)
        const popupContent = `
        <b>Warning!</b><br>
        Dangerous area detected.<br>
        Devices: ${device1.id} (${device1.severity}) and ${device2.id} (${device2.severity})
    `;
        dangerBlock.bindPopup(popupContent); // Attach the popup to the block
    }


    // Update or create a marker for a user.
    function updateUserMarker(data) {
        const id = data.id;
        const content = `
            ID: ${id}<br>
            Latitude: ${data.lat}<br>
            Longitude: ${data.lng}
        `;
        if (markers[id]) {
            markers[id].setLatLng([data.lat, data.lng]);
            markers[id].getPopup().setContent(content);
        } else {
            userCount++;
            const bgColor = null; // Default background color for user markers
            createCustomIcon('icons/User.png', id, bgColor, (result) => { // Pass `bgColor` as the third argument
                const userIcon = L.icon({
                    iconUrl: result.iconUrl,
                    iconSize: [result.width, result.height],
                    iconAnchor: [result.width / 2, result.height],
                    popupAnchor: [0, -result.height + 10]
                });
                markers[id] = L.marker([data.lat, data.lng], { icon: userIcon })
                    .addTo(map)
                    .bindPopup(content);
                document.getElementById('user-count').textContent = userCount;
            });
        }
    }

    // Function to open and manage the SSE connection
    function connectToSSE() {
        console.log("Connecting to SSE...");
        const evtSource = new EventSource('/sse');

        evtSource.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                if (data.type === "iot") {
                    updateIotMarker(data);
                } else if (data.type === "user") {
                    updateUserMarker(data);
                }
            } catch (err) {
                console.error("Error processing SSE data:", err);
                console.error("Raw event data:", event.data); // Log raw data for debugging
            }
        };

        evtSource.onerror = function(err) {
            console.error("SSE connection error:", err);
            evtSource.close(); // Close the current connection
            console.warn("Reconnecting to SSE in 3 seconds...");
            setTimeout(connectToSSE, 3000); // Attempt reconnection after 3 seconds
        };
    }

    // Start SSE connection on page load
    connectToSSE();
});

