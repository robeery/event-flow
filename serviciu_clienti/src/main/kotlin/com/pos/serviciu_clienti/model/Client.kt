package com.pos.serviciu_clienti.model



import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed

@Document(collection = "clients")
data class Client(

    @Id
    val id: String? = null,  // MongoDB imi genereaza automat ObjectId

    @Indexed(unique = true)
    val idmUserId: Int,  // Link to IDM user (from authentication)

    @Indexed(unique = true)
    val email: String,

    val prenume: String? = null,
    val nume: String? = null,

    val dateSuntPublice: Boolean = false,  // Flag pentru vizibilitate

    val linkuriSocialMedia: Map<String, String>? = null,  // {"facebook": "url", "instagram": "url"}

    val bileteAchizitionate: MutableList<String> = mutableListOf()  // Lista cu coduri bilete
)