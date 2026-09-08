const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("statusText");
const transcriptionText = document.getElementById("transcriptionText");
const errorMessage = document.getElementById("errorMessage");

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

function setRecordingState() {
    setStatus("Recording...");
    startButton.disabled = true;
    stopButton.disabled = false;
}

startButton.addEventListener("click", () => {
    clearError();
    setRecordingState();
});

stopButton.addEventListener("click", () => {
    setStatus("Processing...");
    stopButton.disabled = true;

    setTimeout(() => {
        transcriptionText.textContent =
            "Recording stopped. Audio processing will be implemented next.";
        setReadyState();
    }, 500);
});

setReadyState();