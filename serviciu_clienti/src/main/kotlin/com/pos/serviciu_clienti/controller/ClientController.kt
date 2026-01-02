package com.pos.serviciu_clienti.controller



import com.pos.serviciu_clienti.dto.*
import com.pos.serviciu_clienti.service.ClientService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clients")
class ClientController(
    private val clientService: ClientService
) {

    // CREATE - POST /api/clients
    @PostMapping
    fun createClient(@Valid @RequestBody dto: ClientRequestDTO): ResponseEntity<ClientResponseDTO> {
        val client = clientService.createClient(dto)
        val response = toResponseDTO(client)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // READ - GET /api/clients/{id}
    @GetMapping("/{id}")
    fun getClientById(@PathVariable id: String): ResponseEntity<ClientResponseDTO> {
        val client = clientService.getClientById(id)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // READ - GET /api/clients?email=...
    @GetMapping
    fun getClients(
        @RequestParam(required = false) email: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {

        //daca se cauta dupa mail
        if (email != null) {
            val client = clientService.getClientByEmail(email)
            return ResponseEntity.ok(
                mapOf(
                    "client" to toResponseDTO(client)
                )
            )
        }

        // Altfel returneaza paginat
        val pageable = PageRequest.of(page, size)
        val clientsPage = clientService.getAllClients(pageable)

        val response = mapOf(
            "clients" to clientsPage.content.map { toSummaryDTO(it) },
            "currentPage" to clientsPage.number,
            "totalPages" to clientsPage.totalPages,
            "totalItems" to clientsPage.totalElements,
            "links" to buildPaginationLinks(page, clientsPage.totalPages)
        )

        return ResponseEntity.ok(response)
    }

    // UPDATE - PUT /api/clients/{id}
    @PutMapping("/{id}")
    fun updateClient(
        @PathVariable id: String,
        @Valid @RequestBody dto: ClientRequestDTO
    ): ResponseEntity<ClientResponseDTO> {
        val client = clientService.updateClient(id, dto)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // DELETE - DELETE /api/clients/{id}
    @DeleteMapping("/{id}")
    fun deleteClient(@PathVariable id: String): ResponseEntity<Void> {
        clientService.deleteClient(id)
        return ResponseEntity.noContent().build()
    }

    // BILETE - POST /api/clients/{id}/bilete
    @PostMapping("/{id}/bilete")
    fun addBilet(
        @PathVariable id: String,
        @Valid @RequestBody dto: AddBiletDTO
    ): ResponseEntity<ClientResponseDTO> {
        val client = clientService.addBiletToClient(id, dto.codBilet)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // BILETE - DELETE /api/clients/{id}/bilete/{codBilet}
    @DeleteMapping("/{id}/bilete/{codBilet}")
    fun removeBilet(
        @PathVariable id: String,
        @PathVariable codBilet: String
    ): ResponseEntity<ClientResponseDTO> {
        val client = clientService.removeBiletFromClient(id, codBilet)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // HELPER - converteste Client -> ClientResponseDTO cu HATEOAS
    private fun toResponseDTO(client: com.pos.serviciu_clienti.model.Client): ClientResponseDTO {
        val selfLink = linkTo(methodOn(ClientController::class.java).getClientById(client.id!!)).withSelfRel()
        val allClientsLink = linkTo(methodOn(ClientController::class.java).getClients(null, 0, 10)).withRel("all-clients")

        return ClientResponseDTO(
            id = client.id!!,
            email = client.email,
            prenume = client.prenume,
            nume = client.nume,
            dateSuntPublice = client.dateSuntPublice,
            linkuriSocialMedia = client.linkuriSocialMedia,
            bileteAchizitionate = client.bileteAchizitionate,
            links = mapOf(
                "self" to selfLink.href,
                "all-clients" to allClientsLink.href
            )
        )
    }

    // HELPER - Converteste Client -> ClientSummaryDTO cu HATEOAS
    private fun toSummaryDTO(client: com.pos.serviciu_clienti.model.Client): ClientSummaryDTO {
        val selfLink = linkTo(methodOn(ClientController::class.java).getClientById(client.id!!)).withSelfRel()

        return ClientSummaryDTO(
            id = client.id!!,
            email = client.email,
            prenume = client.prenume,
            nume = client.nume,
            links = mapOf("self" to selfLink.href)
        )
    }

    // HELPER - contruieste  linkuri cu paginare
    private fun buildPaginationLinks(currentPage: Int, totalPages: Int): Map<String, String?> {
        val links = mutableMapOf<String, String?>()

        if (currentPage > 0) {
            links["prev"] = linkTo(methodOn(ClientController::class.java)
                .getClients(null, currentPage - 1, 10)).withRel("prev").href
        }

        if (currentPage < totalPages - 1) {
            links["next"] = linkTo(methodOn(ClientController::class.java)
                .getClients(null, currentPage + 1, 10)).withRel("next").href
        }

        return links
    }
}