package com.pos.serviciu_clienti.security

import com.pos.grpc.idm.IdentityServiceGrpc
import com.pos.grpc.idm.TokenRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class IdmClientService {

    @Value("\${idm.service.host:localhost}")
    private lateinit var idmHost: String

    @Value("\${idm.service.port:50051}")
    private var idmPort: Int = 50051

    private lateinit var channel: ManagedChannel
    private lateinit var blockingStub: IdentityServiceGrpc.IdentityServiceBlockingStub

    @PostConstruct
    fun init() {
        channel = ManagedChannelBuilder.forAddress(idmHost, idmPort)
            .usePlaintext()
            .build()
        blockingStub = IdentityServiceGrpc.newBlockingStub(channel)
    }

    @PreDestroy
    fun shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            channel.shutdownNow()
        }
    }

    fun validateToken(token: String): TokenValidationResult {
        return try {
            val request = TokenRequest.newBuilder()
                .setToken(token)
                .build()

            val response = blockingStub.validateToken(request)

            TokenValidationResult(
                valid = response.valid,
                userId = response.userId,
                role = response.role,
                message = response.message
            )
        } catch (e: StatusRuntimeException) {
            TokenValidationResult(
                valid = false,
                userId = 0,
                role = null,
                message = "IDM service unavailable: ${e.status}"
            )
        }
    }

    data class TokenValidationResult(
        val valid: Boolean,
        val userId: Int,
        val role: String?,
        val message: String
    )
}
