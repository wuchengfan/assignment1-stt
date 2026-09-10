package com.wuchengfan.assignment1stt.controller;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private static final String SHUTDOWN_PATH =
            "/api/v1/admin/shutdown";

    private final ConfigurableApplicationContext applicationContext;
    private final AtomicBoolean shutdownInProgress =
            new AtomicBoolean(false);

    public AdminController(
            ConfigurableApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
    }

    @PostMapping(SHUTDOWN_PATH)
    public ResponseEntity<?> shutdownServer() {

        if (!shutdownInProgress.compareAndSet(false, true)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            Instant.now().toString(),
                            409,
                            "Conflict",
                            "Graceful shutdown is already in progress.",
                            SHUTDOWN_PATH));
        }

        /*
         * Shutdown runs on a separate thread so the HTTP 202 response
         * can be returned before the application context closes.
         * Development assistance was provided by ChatGPT and reviewed
         * for this project.
         */
        Thread shutdownThread = new Thread(() -> {

            try {
                Thread.sleep(500);
                applicationContext.close();

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        shutdownThread.setName("graceful-shutdown");
        shutdownThread.start();

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ShutdownResponse(
                        "Graceful shutdown requested."));
    }

    private record ShutdownResponse(String message) {
    }

    private record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            String path) {
    }
}