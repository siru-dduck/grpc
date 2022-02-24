package siru.grpcclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import siru.grpcclient.service.gprc.SampleGrpcClient;
import siru.proto.SampleRequest;

@SpringBootApplication
public class GrpcClientApplication {

    public static void main(String[] args) {
        SpringApplication
                .run(GrpcClientApplication.class, args);
    }

    @Component
    @Slf4j
    @RequiredArgsConstructor
    public static class GrpcRunner implements ApplicationRunner {

        private final SampleGrpcClient grpcClient;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("grpc call started..");
            final SampleRequest sampleRequest = SampleRequest.newBuilder()
                    .setUserId("U1001")
                    .setMessage("grpc request from java")
                    .build();
            grpcClient.sampleCall(sampleRequest);
        }

    }
}
