package br.com.anibal.armory.springreactive.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class HelloController {

    @RequestMapping(path = "/hello", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> hello () {
        return Flux.<String>generate(sink -> {
            sink.next("Hello");
        }).delayElements(Duration.ofMillis(100));
        }
        }
