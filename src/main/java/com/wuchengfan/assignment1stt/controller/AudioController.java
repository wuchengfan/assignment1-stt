package com.wuchengfan.assignment1stt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AudioController {

    @PostMapping("/api/audio")
    public ResponseEntity<String> receiveAudio(
            @RequestParam("audio") MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("No audio data received.");
        }

        return ResponseEntity.ok(
                "Audio received successfully. Size: " + audio.getSize() + " bytes"
        );
    }
}