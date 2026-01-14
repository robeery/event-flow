package com.pos.security;

import com.pos.grpc.idm.IdentityServiceGrpc;
import com.pos.grpc.idm.TokenRequest;
import com.pos.grpc.idm.ValidationResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdmClientService {

    @Value("${idm.service.host:localhost}")
    private String idmHost;

    @Value("${idm.service.port:50051}")
    private int idmPort;

    private ManagedChannel channel;
    private IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(idmHost, idmPort)
                .usePlaintext()
                .build();
        blockingStub = IdentityServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
            }
        }
    }

    public TokenValidationResult validateToken(String token) {
        try {
            TokenRequest request = TokenRequest.newBuilder()
                    .setToken(token)
                    .build();

            ValidationResponse response = blockingStub.validateToken(request);

            return new TokenValidationResult(
                    response.getValid(),
                    response.getUserId(),
                    response.getRole(),
                    response.getMessage()
            );
        } catch (StatusRuntimeException e) {
            return new TokenValidationResult(false, 0, null, "IDM service unavailable: " + e.getStatus());
        }
    }

    public record TokenValidationResult(boolean valid, int userId, String role, String message) {}
}
