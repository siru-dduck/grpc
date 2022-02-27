package siru.grpcclient.service.grpc;


import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import net.devh.boot.grpc.server.service.GrpcService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Mono;
import siru.grpcclient.service.gprc.SampleGrpcClient;
import siru.proto.SampleRequest;
import siru.proto.SampleResponse;
import siru.proto.SampleServiceGrpc;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest(classes = {SampleGrpcClient.class, GrpcTest.GrpcTestConfig.class})
public class GrpcTest {

    @Autowired
    private SampleGrpcClient sampleGrpcClient;

    @Test
    @Description("gprc unary 요청 정상 테스트")
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
    @Description("gprc unary 요청 여러건 보낸후 요청-응답 처리시간 테스트")
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    void many_sampleCallTest() throws InterruptedException {
        // given
        final int NUM_REQUEST = 50000;
        final CountDownLatch latch = new CountDownLatch(NUM_REQUEST);
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U-1234")
                .setMessage("hello")
                .build();

        // when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("performance test");
        IntStream.range(0, NUM_REQUEST)
                .forEach(i -> sampleGrpcClient
                        .sampleCall(sampleRequest)
                        .thenAccept(response -> {
                            log.debug("{}", response);
                            latch.countDown();
                            if (latch.getCount() <= 0) {
                                stopWatch.stop();
                                log.info("{}", stopWatch.prettyPrint());
                            }
                        })
                );

        // then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @Description("reactive unary call 여러건 테스트")
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    void many_reactive_sampleCallTest_test() throws InterruptedException {
        // given
        final int NUM_REQUEST = 50000;
        final CountDownLatch latch = new CountDownLatch(NUM_REQUEST);
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U-1234")
                .setMessage("hello")
                .build();

        // when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("performance test");
        IntStream.range(0, NUM_REQUEST)
                .forEach(i ->
                        sampleGrpcClient.sampleCallWithReactiveStream(sampleRequest)
                                .doOnSuccess(r -> {
                                    latch.countDown();
                                    if(latch.getCount() <= 0) {
                                        stopWatch.stop();
                                        log.info("{}", stopWatch.prettyPrint());
                                    }
                                })
                                .subscribe()
                );

        // then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @Description("many gprc client streaming call test")
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    void many_clientStreamingCall_test() throws InterruptedException {
        // given
        final int NUM_REQUEST = 100000;
        final int BUFFER_SIZE = 500;
        final CountDownLatch latch = new CountDownLatch(NUM_REQUEST / BUFFER_SIZE);
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U-1234")
                .setMessage("hello")
                .build();

        // when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("performance test");
        IntStream.range(0, NUM_REQUEST / BUFFER_SIZE).forEach(i -> {
            SampleRequest[] sampleRequestArr = new SampleRequest[BUFFER_SIZE];
            Arrays.fill(sampleRequestArr, sampleRequest);
            sampleGrpcClient.clientStreamingCall(Arrays.asList(sampleRequestArr))
                    .thenAccept(response -> {
                        log.debug("{}", response);
                        latch.countDown();
                        if (latch.getCount() <= 0) {
                            stopWatch.stop();
                            log.info("{}", stopWatch.prettyPrint());
                        }
                    });
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @Description("many gprc client streaming with reactive stream call test")
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    void many_reactive_clientStreamingCall_test() throws InterruptedException {
        // given
        final int NUM_REQUEST = 100000;
        final int BUFFER_SIZE = 500;
        final CountDownLatch latch = new CountDownLatch(NUM_REQUEST / BUFFER_SIZE);
        SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U-1234")
                .setMessage("hello")
                .build();

        // when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("performance test");
        IntStream.range(0, NUM_REQUEST / BUFFER_SIZE).forEach(i -> {
            SampleRequest[] sampleRequestArr = new SampleRequest[BUFFER_SIZE];
            Arrays.fill(sampleRequestArr, sampleRequest);
            sampleGrpcClient.clientStreamingCallWithReactiveStream(Arrays.asList(sampleRequestArr))
                    .doOnSuccess(r -> {
                        latch.countDown();
                        if(latch.getCount() <= 0) {
                            stopWatch.stop();
                            log.info("{}", stopWatch.prettyPrint());
                        }
                    })
                    .subscribe();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
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

        @Bean
        public GrpcChannelConfigurer grpcChannelConfigurer() {
            return (channelBuilder, name) -> {
                log.info("Channel ChannelConfigurer {} {}", channelBuilder, name);
                if (channelBuilder instanceof InProcessChannelBuilder) {
                    channelBuilder
                            .usePlaintext()
                            .directExecutor();
                    return;
                }

                throw new IllegalStateException("Illegal Channel Config");
            };
        }
    }

    /**
     * Mock Server
     */
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
