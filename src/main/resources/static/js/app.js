const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("statusText");
const transcriptionText = document.getElementById("transcriptionText");
const errorMessage = document.getElementById("errorMessage");

let microphoneStream = null;
let mediaRecorder = null;
let audioChunks = [];

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

function handleMicrophoneError(error) {
    switch (error.name) {
        case "NotAllowedError":
            showError(
                "Microphone access was denied. Please allow microphone access and try again."
            );
            break;

        case "NotFoundError":
            showError("No microphone was found on this device.");
            break;

        case "NotReadableError":
            showError(
                "The microphone is currently unavailable or being used by another application."
            );
            break;

        default:
            showError("Unable to access the microphone. Please try again.");
            break;
    }
}

function releaseMicrophone() {
    if (microphoneStream) {
        microphoneStream.getTracks().forEach(track => track.stop());
        microphoneStream = null;
    }
}

async function startRecording() {
    clearError();

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        showError("Microphone recording is not supported by this browser.");
        return;
    }

    if (!window.MediaRecorder) {
        showError("Audio recording is not supported by this browser.");
        return;
    }

    try {
        startButton.disabled = true;
        stopButton.disabled = true;
        setStatus("Requesting microphone access...");

        microphoneStream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        // Start each recording with a fresh collection so audio from an
        // earlier recording cannot accidentally be included in the next one.
        audioChunks = [];

        mediaRecorder = new MediaRecorder(microphoneStream);

        mediaRecorder.addEventListener("dataavailable", event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

        mediaRecorder.addEventListener("stop", () => {
            // Release the device after recording so the microphone is not
            // left active while the user is waiting or starting another recording.
            releaseMicrophone();

            transcriptionText.textContent =
                "Recording captured. Audio verification will be implemented next.";

            mediaRecorder = null;
            setReadyState();
        });

        mediaRecorder.start();
        setRecordingState();

    } catch (error) {
        releaseMicrophone();
        mediaRecorder = null;
        handleMicrophoneError(error);
        setReadyState();
    }
}

function stopRecording() {
    if (!mediaRecorder || mediaRecorder.state !== "recording") {
        return;
    }

    setStatus("Processing...");
    stopButton.disabled = true;

    mediaRecorder.stop();
}

startButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);

setReadyState();