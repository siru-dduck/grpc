package siru.grpcclient.service.grpc;


import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import net.devh.boot.grpc.server.service.GrpcService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import siru.grpcclient.service.gprc.SampleGrpcClient;
import siru.proto.SampleRequest;
import siru.proto.SampleResponse;
import siru.proto.SampleServiceGrpc;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest(classes = {SampleGrpcClient.class, GrpcTest.GrpcTestConfig.class})
public class GrpcTest {

    @Autowired
    private SampleGrpcClient sampleGrpcClient;

    @Test
    void sampleCallTest() {
        // given
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                        .setUserId("U-1234")
                        .setMessage("hello")
                        .build();

        // when
        CompletableFuture<SampleResponse> sampleResponseCompletableFuture = sampleGrpcClient.sampleCall(sampleRequest);

        assertThatCode(() -> sampleResponseCompletableFuture.get(3, TimeUnit.SECONDS))
                .doesNotThrowAnyException();
    }

    @Test
    void many_sampleCallTest() throws InterruptedException {
        // given
        final int NUM_REQUEST = 100000;
        final CountDownLatch latch = new CountDownLatch(NUM_REQUEST);
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U-1234")
                .setMessage("hello")
                .build();

        // when
        List<CompletableFuture<SampleResponse>> completableFutureList = IntStream.range(0, NUM_REQUEST)
                .boxed()
                .map(i -> sampleGrpcClient.sampleCall(sampleRequest))
                .collect(Collectors.toList());
log.info("###");
        completableFutureList.forEach(completableFuture -> {
            completableFuture.thenAccept(response -> {
                log.debug("{}", response);
                latch.countDown();
            });
        });

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void reactive_clientStreamingCall_test() throws InterruptedException {
        // given
        final int NUM_REQUEST = 100000;
        final int BUFFER_SIZE = 500;
        final CountDownLatch latch = new CountDownLatch(NUM_REQUEST / BUFFER_SIZE);
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U-1234")
                .setMessage("hello")
                .build();

        // when
        IntStream.range(0, NUM_REQUEST / BUFFER_SIZE).forEach(i -> {
            SampleRequest[] sampleRequestArr = new SampleRequest[BUFFER_SIZE];
            Arrays.fill(sampleRequestArr, sampleRequest);
            sampleGrpcClient.clientStreamingCallWithReactiveStream(Arrays.asList(sampleRequestArr))
                    .doOnSuccess(r -> latch.countDown())
                    .subscribe();
        });

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    @Configuration
    @ImportAutoConfiguration({
            GrpcClientAutoConfiguration.class,
            GrpcServerAutoConfiguration.class,
            GrpcServerFactoryAutoConfiguration.class
    })
    public static class GrpcTestConfig {

        @Bean
        public SampleService sampleService() {
            return new SampleService();
        }
    }

    @GrpcService
    public static class SampleService extends SampleServiceGrpc.SampleServiceImplBase {

        @Override
        public void sampleCall(SampleRequest request, StreamObserver<SampleResponse> responseObserver) {
            Mono.delay(Duration.ofSeconds(1))
                    .subscribe((v) -> {
                        responseObserver.onNext(
                                SampleResponse.newBuilder()
                                        .setMessage("hello")
                                        .build()
                        );
                        responseObserver.onCompleted();
                    });
        }

        @Override
        public StreamObserver<SampleRequest> clientStreamingCall(StreamObserver<SampleResponse> responseObserver) {
            return new StreamObserver<>() {
                @Override
                public void onNext(SampleRequest value) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {
                    Mono.delay(Duration.ofSeconds(1))
                            .subscribe((v) -> {
                                responseObserver.onNext(
                                        SampleResponse.newBuilder()
                                                .setMessage("hello")
                                                .build()
                                );
                                responseObserver.onCompleted();
                            });
                }
            };
        }
    }

}
