package com.example.app;

public class TaskOutput {
    // Indicates result of parsing server response
    ParseResult parseResult;
    // Indicates result of background task
    TaskResult taskResult;
    // Error caused unsuccessful result
    Throwable taskError;
}
