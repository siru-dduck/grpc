package siru.grpcclient.service.gprc;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import siru.proto.SampleRequest;
import siru.proto.SampleResponse;
import siru.proto.SampleServiceGrpc;

@Slf4j
@Service
public class GrpcClient {

    private static final int PORT = 4000;
    private static final String HOST = "localhost";
    private final SampleServiceGrpc.SampleServiceStub asyncStub = SampleServiceGrpc.newStub(
            ManagedChannelBuilder.forAddress(HOST, PORT)
                    .usePlaintext()
                    .build()
    );

    public void sampleCall() {
        final SampleRequest sampleRequest = SampleRequest.newBuilder()
                .setUserId("U1001")
                .setMessage("grpc request from java")
                .build();

        asyncStub.sampleCall(sampleRequest, new StreamObserver<SampleResponse>() {
            @Override
            public void onNext(SampleResponse response) {
                log.info("Response {}", response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                log.error("GrpcClient#sampleCall - onError {}", t);
            }

            @Override
            public void onCompleted() {
                log.info("GrpcClient#sampleCall - onCompleted");
            }
        });

        log.info("doing something...");
    }

}
