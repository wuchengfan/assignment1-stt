package com.wuchengfan.assignment1stt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private final String apiKey;

    public TranscriptionService(
            @Value("${OPENAI_API_KEY:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String transcribe(MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException(
                    "Audio file must not be empty.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not configured.");
        }

        return "Transcription service is ready.";
    }
}