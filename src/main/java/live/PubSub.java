package live;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Publisher -> [Data1] -> Op1 -> [Data2] -> Op2 -> [Data3] -> Subscriber
 * 1. map (d1 -> f -> d2)
 *
 * downStream
 * pub -> [Data1] -> mapPub -> [Data2] -> logSub
 * upstream이 일어날 때 mapPub을 거쳐가기 때문에 mapPub도 subscribe기능을 가져야 한다. 때문에 Publisher로 만들었다.
 */

public class PubSub {

    public static void main(String[] args) {

        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<String> mapPub = mapPub(pub, s -> "["+s+"]");//(Function<String, String>)
//        Publisher<Integer> map2Pub = mapPub(mapPub, s-> -s);
        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(),
                (a, b) -> a.append(b+","));//(BiFunction<Integer,Integer,Integer>)

//        mapPub.subscribe(logSub());
        reducePub.subscribe(logSub());
//        sumPub.subscribe(logSub());
//        map2Pub.subscribe(logSub());
    }

    private static <T,R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T, R>(sub) {
                    R result = init;

                    @Override
                    public void onNext(T i) {
                        result = bf.apply(result, i);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

   /* private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {
                    int sum = 0;
                    @Override
                    public void onNext(Integer i) {
                        sum += i;
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(sum);
                        sub.onComplete();
                    }
                });
            }
        };
    }*/

   // T -> R
    private static <T,R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T,R>(sub) {
                    @Override
                    public void onNext(T i) {
                        sub.onNext(f.apply(i));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe : ");
                //거의 무제한으로 값을 받을 수 있음
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T i) {
                System.out.println("onNext : " + i);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError : " + t);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete : ");
            }
        };
    }

    private static Publisher<Integer> iterPub(List<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(s->sub.onNext(s));
                            sub.onComplete();
                        } catch(Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
