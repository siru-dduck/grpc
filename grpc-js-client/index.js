const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const PROTO_PATH = __dirname + '/../grpc-common/src/main/proto/SampleProto.proto';

/**
 * .proto 파일 load
 */
const packageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {
        keepCase: true,
        longs: String,
        enums: String,
        defaults: true,
        oneofs: true
    });
const protoDescriptor = grpc.loadPackageDefinition(packageDefinition);
const sampleProto = protoDescriptor.grpc.sample;

/**
 * 함수처럼 쓸 수 있는 stub생성 
 */
const sampleStub = new sampleProto.SampleService("localhost:4000", grpc.credentials.createInsecure());
const sampleRequsst = {
    userId: "U1001",
    message: "grpc message from js"
};

// rpc call
sampleStub.sampleCall(sampleRequsst, (err, response) => {
    if(err) {
        console.error(err);
        return;
    }

    console.log("respone - ", response);
});