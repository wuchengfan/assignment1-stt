package com.wuchengfan.assignment1stt.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    public String transcribe(MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file must not be empty.");
        }

        return "Transcription service is ready.";
    }
}