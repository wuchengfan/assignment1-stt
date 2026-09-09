package com.wuchengfan.assignment1stt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wuchengfan.assignment1stt.service.TranscriptionService;

@RestController
public class AudioController {

    private final TranscriptionService transcriptionService;

    public AudioController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/api/audio")
    public ResponseEntity<String> receiveAudio(
            @RequestParam("audio") MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("No audio data received.");
        }

        try {
            String transcription = transcriptionService.transcribe(audio);
            return ResponseEntity.ok(transcription);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());

        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Transcription service is currently unavailable.");
        }
    }
}