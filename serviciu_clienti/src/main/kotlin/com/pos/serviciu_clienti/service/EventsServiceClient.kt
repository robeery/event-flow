package com.pos.serviciu_clienti.service



import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.UUID

@Component
class EventsServiceClient(
    @Value("\${events.service.url}")
    private val eventsServiceUrl: String
) {
    private val webClient = WebClient.builder()
        .baseUrl(eventsServiceUrl)
        .build()

    // Genereaza cod unic pentru bilet
    fun generateCodBilet(): String {
        return "BILET-${UUID.randomUUID().toString().substring(0, 8)}"
    }

    // Creeaza bilet in Events Service prin PUT
    fun createBilet(codBilet: String, evenimentId: Long?, pachetId: Long?): BiletResponse {
        val requestBody = BiletRequest(
            evenimentId = evenimentId,
            pachetId = pachetId
        )

        return try {
            val response = webClient.put()
                .uri("/api/event-manager/tickets/{cod}", codBilet)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(BiletResponse::class.java)
                .block()
                ?: throw RuntimeException("Răspuns gol de la Events Service")

            response
        } catch (ex: WebClientResponseException.NotFound) {
            throw NoSuchElementException("Evenimentul sau pachetul specificat nu exista")
        } catch (ex: WebClientResponseException.Conflict) {
            throw IllegalArgumentException("Conflict la crearea biletului: ${ex.message}")
        } catch (ex: WebClientResponseException.UnprocessableEntity) {
            throw IllegalStateException("Nu se poate crea biletul: ${ex.message}")
        } catch (ex: WebClientResponseException) {
            throw RuntimeException("Eroare de la Events Service: ${ex.statusCode} - ${ex.message}")
        } catch (ex: Exception) {
            throw RuntimeException("Eroare la comunicarea cu Events Service: ${ex.message}")
        }
    }

    // sterge bilet din Events Service
    fun deleteBilet(codBilet: String) {
        try {
            webClient.delete()
                .uri("/api/event-manager/tickets/{cod}", codBilet)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (ex: WebClientResponseException.NotFound) {
            // Biletul nu exista, OK pentru delete
        } catch (ex: Exception) {
            throw RuntimeException("Eroare la ștergerea biletului din Events Service: ${ex.message}")
        }
    }

    // Obtine detalii bilet din Events Service
    fun getBiletDetails(codBilet: String): BiletResponse? {
        return try {
            webClient.get()
                .uri("/api/event-manager/tickets/{cod}", codBilet)
                .retrieve()
                .bodyToMono(BiletResponse::class.java)
                .block()
        } catch (ex: WebClientResponseException.NotFound) {
            null
        } catch (ex: Exception) {
            null
        }
    }
}

// DTO pentru request catre Events Service
data class BiletRequest(
    val evenimentId: Long? = null,
    val pachetId: Long? = null
)

// DTO pentru response de la Events Service
data class BiletResponse(
    val cod: String? = null,
    val evenimentId: Long? = null,
    val pachetId: Long? = null
)