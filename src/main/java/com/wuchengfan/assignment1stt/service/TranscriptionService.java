package com.wuchengfan.assignment1stt.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    private final StatisticsService statisticsService;

    public TranscriptionService(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${spring.http.clients.connect-timeout:3s}")
            Duration connectTimeout,
            @Value("${spring.http.clients.read-timeout:8s}")
            Duration readTimeout,
            StatisticsService statisticsService) {

        this.apiKey = apiKey;
        this.statisticsService = statisticsService;

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
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

            if (response.usage() != null) {
                statisticsService.addUsage(
                        response.usage().input_tokens(),
                        response.usage().output_tokens());
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

    /*
     * OpenAI transcription response fields follow the official
     * Audio Transcriptions API documentation.
     * Development assistance was provided by ChatGPT and reviewed
     * for this project.
     */
    private record TranscriptionResponse(
            String text,
            Usage usage) {
    }

    private record Usage(
            long input_tokens,
            long output_tokens) {
    }
}