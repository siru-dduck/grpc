const PROTO_PATH = __dirname + '/../grpc-common/src/main/proto/SampleProto.proto';
const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');

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

console.log(sampleProto);
const gRPC_Server = new grpc.Server();

/**
 * grpc sampleCall handler
 */
const sampleCall = (call, callback) => {
    console.log("SampleServiceImpl#sampleCall - ", call.request);
    callback(null, { message: "grpc service response from js" });
}

const Server = () => {
    const server = new grpc.Server();
    server.addService(sampleProto.SampleService.service, {
        sampleCall
    });
    return server;
}

const routeServer = Server();
routeServer.bindAsync('0.0.0.0:4000', grpc.ServerCredentials.createInsecure(), () => {
    console.log("start grpc server");
    routeServer.start();
});