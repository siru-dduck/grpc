package siru.grpcclient.config;

import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GrpcConfig {

    @Bean
    public GrpcChannelConfigurer grpcChannelConfigurer() {
        return (channelBuilder, name) -> {
            log.info("Channel ChannelConfigurer {} {}", channelBuilder, name);
            if (channelBuilder instanceof NettyChannelBuilder) {
                channelBuilder
                        .usePlaintext()
                        .directExecutor();
                return;
            }

            throw new IllegalStateException("Illegal Channel Config");
        };
    }
}
