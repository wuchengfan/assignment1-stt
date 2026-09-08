const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("statusText");
const transcriptionText = document.getElementById("transcriptionText");
const errorMessage = document.getElementById("errorMessage");

let microphoneStream = null;

function setStatus(message) {
    statusText.textContent = message;
}

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.hidden = false;
}

function clearError() {
    errorMessage.textContent = "";
    errorMessage.hidden = true;
}

function setReadyState() {
    setStatus("Ready");
    startButton.disabled = false;
    stopButton.disabled = true;
}

function handleMicrophoneError(error) {
    switch (error.name) {
        case "NotAllowedError":
            showError("Microphone access was denied. Please allow microphone access and try again.");
            break;

        case "NotFoundError":
            showError("No microphone was found on this device.");
            break;

        case "NotReadableError":
            showError("The microphone is currently unavailable or being used by another application.");
            break;

        default:
            showError("Unable to access the microphone. Please try again.");
            break;
    }
}

async function requestMicrophoneAccess() {
    clearError();
    setStatus("Requesting microphone access...");
    startButton.disabled = true;
    stopButton.disabled = true;

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        showError("Microphone recording is not supported by this browser.");
        setReadyState();
        return;
    }

    try {
        microphoneStream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        setStatus("Microphone ready");

        transcriptionText.textContent =
            "Microphone access granted. Recording will be implemented next.";

        // Release the microphone after this permission check so it is not left
        // active before the actual recording functionality is implemented.
        microphoneStream.getTracks().forEach(track => track.stop());
        microphoneStream = null;

    } catch (error) {
        handleMicrophoneError(error);

    } finally {
        startButton.disabled = false;
        stopButton.disabled = true;
    }
}

startButton.addEventListener("click", requestMicrophoneAccess);

setReadyState();