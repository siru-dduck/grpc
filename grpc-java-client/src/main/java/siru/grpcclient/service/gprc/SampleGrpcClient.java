package siru.grpcclient.service.gprc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import siru.proto.SampleRequest;
import siru.proto.SampleResponse;
import siru.proto.SampleServiceGrpc;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class SampleGrpcClient {

    private static final int PORT = 4000;
    private static final String HOST = "localhost";

    @GrpcClient("sampleService")
    private SampleServiceGrpc.SampleServiceStub asyncStub;

    // grpc-spring-boot-starter 를 쓰면 아래같이 설정과 호출이 집약된 코드를 짤 필요가 없다. (auto-configuration을 통한 설정분리)
//    private final SampleServiceGrpc.SampleServiceStub asyncStub = SampleServiceGrpc.newStub(
//            ManagedChannelBuilder.forAddress(HOST, PORT)
//                    .usePlaintext()
//                    .build()
//    );

    public CompletableFuture<SampleResponse> sampleCall(SampleRequest sampleRequest) {
        CompletableFuture<SampleResponse> completableFuture = new CompletableFuture<>();

        asyncStub.sampleCall(sampleRequest, new StreamObserver<SampleResponse>() {
            @Override
            public void onNext(SampleResponse response) {
                log.debug("Response {}", response.getMessage());
                completableFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("GrpcClient#sampleCall - onError {}", t);
            }

            @Override
            public void onCompleted() {
                log.debug("GrpcClient#sampleCall - onCompleted");
            }
        });

        return completableFuture;
    }

    public CompletableFuture<SampleResponse> clientStreamingCall(Iterable<SampleRequest> sampleRequestIterable) {
        CompletableFuture<SampleResponse> completableFuture = new CompletableFuture<>();
        StreamObserver<SampleRequest> requestStreamObserver= asyncStub.clientStreamingCall(new StreamObserver<SampleResponse>() {
            @Override
            public void onNext(SampleResponse response) {
                log.debug("Response: {}", response.getMessage());
                completableFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("", t);
            }

            @Override
            public void onCompleted() {
                log.debug("GrpcClient#clientStreamingCall - onCompleted");
            }
        });

        sampleRequestIterable.forEach(requestStreamObserver::onNext);

        return completableFuture;
    }

    public Mono<SampleResponse> clientStreamingCallWithReactiveStream(Iterable<SampleRequest> sampleRequestFlux) {
        class StreamObserverPublisher implements Publisher<SampleResponse>, StreamObserver<SampleResponse> {

            private Subscriber<? super SampleResponse> subscriber;

            @Override
            public void onNext(SampleResponse value) {
                subscriber.onNext(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("", t);
            }

            @Override
            public void onCompleted() {
                log.debug("onCompleted");
                subscriber.onComplete();
            }

            @Override
            public void subscribe(Subscriber<? super SampleResponse> s) {
                log.info("subscribe");
                this.subscriber = s;
                s.onSubscribe(new BaseSubscriber<>() {});
                StreamObserver<SampleRequest> requestStreamObserver = asyncStub.clientStreamingCall(this);
                sampleRequestFlux.forEach(requestStreamObserver::onNext);
                requestStreamObserver.onCompleted();
            }
        }
        StreamObserverPublisher streamObserverPublisher = new StreamObserverPublisher();
        Mono<SampleResponse> responseMono = Mono.from(streamObserverPublisher);

        return responseMono;
    }

}
