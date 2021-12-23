package siru.grpcserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import siru.grpcserver.service.grpc.SampleServiceImpl;

@SpringBootApplication
public class GrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication
                .run(GrpcServerApplication.class, args);
    }

    @Slf4j
    @Component
    public static class GrpcRunner implements ApplicationRunner {

        private static final int PORT = 4000;
        private static final Server GRPC_SERVER = ServerBuilder.forPort(PORT)
                .addService(new SampleServiceImpl())
                .build();

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("gRPC Server started on port {}", PORT);
            GRPC_SERVER.start();
        }

    }
}
