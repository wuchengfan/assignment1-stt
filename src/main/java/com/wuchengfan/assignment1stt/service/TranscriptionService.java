package com.wuchengfan.assignment1stt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private static final String TRANSCRIPTION_URL =
            "https://api.openai.com/v1/audio/transcriptions";

    private static final String MODEL =
            "gpt-4o-mini-transcribe";

    private final String apiKey;
    private final RestClient restClient;

    public TranscriptionService(
            @Value("${OPENAI_API_KEY:}") String apiKey) {

        this.apiKey = apiKey;
        this.restClient = RestClient.create();
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

        MultiValueMap<String, Object> formData =
                new LinkedMultiValueMap<>();

        formData.add("file", audio.getResource());
        formData.add("model", MODEL);
        formData.add("response_format", "json");

        try {
            TranscriptionResponse response = restClient.post()
                    .uri(TRANSCRIPTION_URL)
                    .headers(headers ->
                            headers.setBearerAuth(apiKey))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(formData)
                    .retrieve()
                    .body(TranscriptionResponse.class);

            if (response == null
                    || response.text() == null
                    || response.text().isBlank()) {

                throw new IllegalStateException(
                        "The transcription service returned no text.");
            }

            return response.text();

        } catch (RestClientResponseException exception) {

            throw new IllegalStateException(
                    "OpenAI transcription request failed with HTTP "
                            + exception.getStatusCode().value() + ".",
                    exception);

        } catch (RestClientException exception) {

            throw new IllegalStateException(
                    "Unable to contact the OpenAI transcription service.",
                    exception);
        }
    }

    private record TranscriptionResponse(String text) {
    }
}