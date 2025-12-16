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
    val email: String,
    val prenume: String? = null,
    val nume: String? = null,
    val links: Map<String, String> = emptyMap()
)

// DTO pentru adaugare bilet
data class AddBiletDTO(
    @field:NotBlank(message = "Codul biletului este obligatoriu")
    val codBilet: String
)