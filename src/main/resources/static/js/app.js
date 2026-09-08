const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("statusText");
const transcriptionText = document.getElementById("transcriptionText");
const errorMessage = document.getElementById("errorMessage");

let microphoneStream = null;
let mediaRecorder = null;
let audioChunks = [];
let recordedAudioBlob = null;

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

async function uploadRecordedAudio(audioBlob) {
    const formData = new FormData();

    formData.append(
        "audio",
        audioBlob,
        "recording.webm"
    );

    const response = await fetch("/api/audio", {
        method: "POST",
        body: formData
    });

    const responseText = await response.text();

    if (!response.ok) {
        throw new Error(responseText || "Audio upload failed.");
    }

    return responseText;
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
		
		recordedAudioBlob = null;

        mediaRecorder.addEventListener("dataavailable", event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

		mediaRecorder.addEventListener("stop", async () => {
		    const audioType = mediaRecorder.mimeType || "audio/webm";

		    recordedAudioBlob = new Blob(audioChunks, {
		        type: audioType
		    });

		    // Reject empty recordings before making a backend request because
		    // sending unusable audio would only waste server and cloud resources.
		    if (recordedAudioBlob.size === 0) {
		        showError("No audio data was captured. Please try recording again.");

		        recordedAudioBlob = null;
		        releaseMicrophone();
		        audioChunks = [];
		        mediaRecorder = null;
		        setReadyState();

		        return;
		    }

		    releaseMicrophone();
		    audioChunks = [];
		    mediaRecorder = null;

		    setStatus("Uploading audio...");

		    try {
		        const result = await uploadRecordedAudio(recordedAudioBlob);

		        transcriptionText.textContent = result;

		    } catch (error) {
		        showError(
		            error.message || "Unable to upload the recording. Please try again."
		        );

		    } finally {
		        setReadyState();
		    }
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