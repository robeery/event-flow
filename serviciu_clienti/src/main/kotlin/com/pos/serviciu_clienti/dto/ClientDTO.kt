package com.pos.serviciu_clienti.dto



import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

// DTO pentru request (CREATE/UPDATE)
data class ClientRequestDTO(
    @field:NotBlank(message = "Email-ul este obligatoriu")
    @field:Email(message = "Email-ul trebuie sa fie valid")
    val email: String,

    val prenume: String? = null,
    val nume: String? = null,
    val dateSuntPublice: Boolean = false,
    val linkuriSocialMedia: Map<String, String>? = null
)

// DTO pentru response (GET) - include ID si bilete
data class ClientResponseDTO(
    val id: String,
    val idmUserId: Int,  // Link to IDM user
    val email: String,
    val prenume: String? = null,
    val nume: String? = null,
    val dateSuntPublice: Boolean,
    val linkuriSocialMedia: Map<String, String>? = null,
    val bileteAchizitionate: List<String>,
    val links: Map<String, String> = emptyMap()  // Pentru HATEOAS
)

// DTO simplificat pentru liste (fara bilete)
data class ClientSummaryDTO(
    val id: String,
    val idmUserId: Int,  // Link to IDM user
    val email: String,
    val prenume: String? = null,
    val nume: String? = null,
    val links: Map<String, String> = emptyMap()
)

// DTO pentru adaugare bilet
data class CumparaBiletDTO(
    val evenimentId: Long? = null,
    val pachetId: Long? = null
) {

    fun isValid(): Boolean {
        return (evenimentId != null && pachetId == null) ||
                (evenimentId == null && pachetId != null)
    }
}

// DTO pentru bilet in format detaliat (detalii de la serviciu evenimente)
data class BiletDetaliatDTO(
    val codBilet: String,
    val tip: String,  // "eveniment" sau "pachet"
    val eveniment: EvenimentDetaliiDTO? = null,
    val pachet: PachetDetaliiDTO? = null
)

// DTO pentru detalii eveniment in bilet
data class EvenimentDetaliiDTO(
    val id: Long,
    val nume: String?,
    val locatie: String?,
    val descriere: String? = null,
    val numarLocuri: Int? = null,
    val bileteDisponibile: Int? = null
)

// DTO pentru detalii pachet in bilet
data class PachetDetaliiDTO(
    val id: Long,
    val nume: String?,
    val locatie: String?,
    val descriere: String? = null,
    val numarLocuri: Int? = null,
    val bileteDisponibile: Int? = null,
    val evenimenteIncluse: List<String>? = null
)