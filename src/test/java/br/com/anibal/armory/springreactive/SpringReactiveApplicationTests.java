package br.com.anibal.armory.springreactive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringReactiveApplicationTests {

    static Integer count;

    @LocalServerPort
    private String serverPort;

	@Test
	void virtualTimeTest() {
		//Flux<Integer> flux = Flux.range(0, 10).delayElements(Duration.ofSeconds(1));

        StepVerifier.withVirtualTime(() -> Flux.range(0, 10).delayElements(Duration.ofSeconds(1)))
                .expectSubscription()
                .thenAwait(Duration.ofMinutes(1))
                .expectNextCount(10)
                .verifyComplete();

		//flux.subscribe(s -> System.out.println(s.toString()));

		//Thread.sleep(5000);
	}


	@Test
	void generateTest() throws InterruptedException {
        count = 0;
	    Flux<Integer> flux = Flux.<Integer>generate(sink -> {
	        count++;
	        sink.next(count);
	        System.out.println("Generating "+ count + " at thread " + Thread.currentThread().getName());
	    }).delayElements(Duration.ofMillis(100));

	    Scheduler elastic = Schedulers.elastic();

	    flux.publishOn(elastic);

        ConnectableFlux<Integer> publish = flux.publish();
        Flux<Integer> elasticPublisher = publish.publishOn(elastic);

        elasticPublisher.take(10).subscribe(i -> System.out.println("First: " + i + "  - Thread: " + Thread.currentThread().getName()));
        elasticPublisher.take(10).subscribe(i -> System.out.println("Second: " + i + "  - Thread: " + Thread.currentThread().getName()));

        publish.connect();

        Thread.sleep(5000);
    }

    @Test
    void testGetInfiniteStream () throws InterruptedException {
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:"+ serverPort)
                .build();

        Flux<String> flux = client.get()
                .uri("/hello")
                .retrieve()
                .bodyToFlux(String.class)
                .take(10);

        flux.subscribe(System.out::println);

        Thread.sleep(5000);
    }

}
