package com.pos.serviciu_clienti.service



import com.fasterxml.jackson.annotation.JsonProperty
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
    fun createBilet(codBilet: String, evenimentId: Long?, pachetId: Long?, authToken: String?): BiletResponse {
        val requestBody = BiletRequest(
            evenimentId = evenimentId,
            pachetId = pachetId
        )

        return try {
            var request = webClient.put()
                .uri("/api/event-manager/tickets/{cod}", codBilet)
                .contentType(MediaType.APPLICATION_JSON)
            
            // Adauga token-ul de autorizare daca exista
            if (authToken != null) {
                request = request.header("Authorization", authToken)
            }
            
            val response = request
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
            val responseBody = ex.responseBodyAsString
            throw RuntimeException("Eroare de la Events Service: ${ex.statusCode} - ${ex.message} - Body: $responseBody")
        } catch (ex: Exception) {
            throw RuntimeException("Eroare la comunicarea cu Events Service: ${ex.message}")
        }
    }

    // sterge bilet din Events Service
    fun deleteBilet(codBilet: String, authToken: String?) {
        try {
            var request = webClient.delete()
                .uri("/api/event-manager/tickets/{cod}", codBilet)
            
            // Adauga token-ul de autorizare daca exista
            if (authToken != null) {
                request = request.header("Authorization", authToken)
            }
            
            request
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



    // Obtine detalii eveniment
    fun getEveniment(evenimentId: Long): EvenimentInfo? {
        return try {
            webClient.get()
                .uri("/api/event-manager/events/{id}", evenimentId)
                .retrieve()
                .bodyToMono(EvenimentInfo::class.java)
                .block()
        } catch (ex: Exception) {
            null
        }
    }

    // Obtine detalii pachet
    fun getPachet(pachetId: Long): PachetInfo? {
        return try {
            webClient.get()
                .uri("/api/event-manager/event-packets/{id}", pachetId)
                .retrieve()
                .bodyToMono(PachetInfo::class.java)
                .block()
        } catch (ex: Exception) {
            null
        }
    }

    // Obtine detalii complete bilet (cod, evenimentId, pachetId)
    fun getBiletComplet(codBilet: String): BiletCompletInfo? {
        return try {
            webClient.get()
                .uri("/api/event-manager/tickets/{cod}", codBilet)
                .retrieve()
                .bodyToMono(BiletCompletInfo::class.java)
                .block()
        } catch (ex: Exception) {
            null
        }
    }

    // Obtine evenimentele unui pachet
    fun getEvenimentePachet(pachetId: Long): List<EvenimentInfo> {
        return try {
            val response = webClient.get()
                .uri("/api/event-manager/event-packets/{id}/events", pachetId)
                .retrieve()
                .bodyToMono(EvenimenteResponse::class.java)
                .block()

            response?.embedded?.evenimente ?: emptyList()
        } catch (ex: Exception) {
            emptyList()
        }
    }

}

// DTO pentru request catre serviciu evenimente
data class BiletRequest(
    val evenimentId: Long? = null,
    val pachetId: Long? = null
)

// DTO pentru response de la serviciu evenimente
data class BiletResponse(
    val cod: String? = null,
    val evenimentId: Long? = null,
    val pachetId: Long? = null
)

// DTO pentru informatii eveniment
data class EvenimentInfo(
    val id: Long? = null,
    val nume: String? = null,
    val locatie: String? = null,
    val descriere: String? = null,
    val numarLocuri: Int? = null,
    val bileteDisponibile: Int? = null
)

// DTO pentru informatii pachet
data class PachetInfo(
    val id: Long? = null,
    val nume: String? = null,
    val locatie: String? = null,
    val descriere: String? = null,
    val numarLocuri: Int? = null,
    val bileteDisponibile: Int? = null
)

// DTO pentru bilet complet de la serviciu evenimente
data class BiletCompletInfo(
    val cod: String? = null,
    val evenimentId: Long? = null,
    val pachetId: Long? = null
)

// DTO pentru raspunsul cu evenimente (HATEOAS embedded)
data class EvenimenteResponse(
    @JsonProperty("_embedded")
    val embedded: EmbeddedEvents? = null
)

data class EmbeddedEvents(
    @JsonProperty("evenimentDTOes")
    val evenimente: List<EvenimentInfo>? = null
)