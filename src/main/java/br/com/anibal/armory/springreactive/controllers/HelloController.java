package br.com.anibal.armory.springreactive.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class HelloController {

    final FluxProcessor processor;
    final FluxSink sink;
    final AtomicLong counter;

    public HelloController () {
        this.processor = DirectProcessor.create().serialize();
        this.sink = processor.sink();
        this.counter = new AtomicLong();
    }

    @RequestMapping(path = "/hello", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> hello () {
        return Flux.<String>generate(sink -> {
            sink.next("Hello");
        }).delayElements(Duration.ofMillis(100));

    }

    @GetMapping(path = "/sayHello")
    public ResponseEntity<?> sayHello() {
        sink.next("Hello "+ counter.getAndIncrement());
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/wait", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> waitHello() {
        return processor.map(e -> ServerSentEvent.builder(e).build());
    }
}
