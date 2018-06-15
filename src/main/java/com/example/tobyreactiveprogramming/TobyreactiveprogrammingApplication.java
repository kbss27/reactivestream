package com.example.tobyreactiveprogramming;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TobyreactiveprogrammingApplication {

    /**
     * Spring에서 적용할 때는 publisher만 만들면된다. publisher만 던져주면 spring이 subscriber역할을 한다.
     * 원하는 방식으로 원하는 시점에 subscriber만들어서 던진 publisher에 데이터를 요청해서 받아온다.
     */

    @RestController
    public static class Controller {
        @RequestMapping("/hello")
        public Publisher<String> hello(String name) {
            return new Publisher<String>() {
                @Override
                public void subscribe(Subscriber<? super String> s) {
                    s.onSubscribe(new Subscription() {
                        @Override
                        public void request(long n) {
                            s.onNext("Hello " + name);
                            s.onComplete();
                        }

                        @Override
                        public void cancel() {

                        }
                    });
                }
            };
        }
    }


	public static void main(String[] args) {
		SpringApplication.run(TobyreactiveprogrammingApplication.class, args);
	}
}
