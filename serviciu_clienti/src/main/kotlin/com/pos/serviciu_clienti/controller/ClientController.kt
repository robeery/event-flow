package com.pos.serviciu_clienti.controller



import com.pos.serviciu_clienti.dto.*
import com.pos.serviciu_clienti.security.AuthorizationHelper
import com.pos.serviciu_clienti.service.ClientService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "API for client management")
class ClientController(
    private val clientService: ClientService,
    private val authHelper: AuthorizationHelper
) {

    // CREATE - POST /api/clients
    @Operation(summary = "Create new client", description = "Creates a new client with unique email. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Client created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid data"),
        ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        ApiResponse(responseCode = "409", description = "Email already exists")
    ])
    @PostMapping
    fun createClient(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Client data",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ClientRequestDTO::class),
                examples = [ExampleObject(
                    name = "Create client example",
                    value = """
                    {
                        "email": "maria.popescu@gmail.com",
                        "prenume": "Maria",
                        "nume": "Popescu",
                        "dateSuntPublice": true,
                        "linkuriSocialMedia": {
                            "facebook": "https://facebook.com/maria.popescu",
                            "linkedin": "https://linkedin.com/in/maria-popescu"
                        }
                    }
                    """
                )]
            )]
        )
        @Valid @RequestBody dto: ClientRequestDTO,
        request: HttpServletRequest
    ): ResponseEntity<ClientResponseDTO> {
        // Authorization: only admin or client can create (clients create their own profile)
        authHelper.requireAdminOrClient(request)
        val idmUserId = authHelper.getUserIdForNewClient(request)

        val client = clientService.createClient(dto, idmUserId)
        val response = toResponseDTO(client)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // READ - GET /api/clients/{id}
    @Operation(summary = "Get client by ID", description = "Returns complete details of a client")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Client found"),
        ApiResponse(responseCode = "404", description = "Client not found")
    ])
    @GetMapping("/{id}")
    fun getClientById(
        @Parameter(description = "Client ID (MongoDB ObjectId)")
        @PathVariable id: String
    ): ResponseEntity<ClientResponseDTO> {
        val client = clientService.getClientById(id)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // READ - GET /api/clients?email=...
    @Operation(summary = "Get client list", description = "Returns paginated list of clients or searches by email")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Client list")
    ])
    @GetMapping
    fun getClients(
        @Parameter(description = "Filter by email")
        @RequestParam(required = false) email: String?,
        @Parameter(description = "Page number (starts from 0)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Number of elements per page")
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
    @Operation(summary = "Update client", description = "Updates an existing client's data. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Client updated"),
        ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        ApiResponse(responseCode = "404", description = "Client not found"),
        ApiResponse(responseCode = "409", description = "New email already exists")
    ])
    @PutMapping("/{id}")
    fun updateClient(
        @Parameter(description = "Client ID")
        @PathVariable id: String,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated client data",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ClientRequestDTO::class),
                examples = [ExampleObject(
                    name = "Update client example",
                    value = """
                    {
                        "email": "andrei.stan.updated@gmail.com",
                        "prenume": "Andrei",
                        "nume": "Stan",
                        "dateSuntPublice": true,
                        "linkuriSocialMedia": {
                            "github": "https://github.com/andrei-stan",
                            "linkedin": "https://linkedin.com/in/andrei-stan"
                        }
                    }
                    """
                )]
            )]
        )
        @Valid @RequestBody dto: ClientRequestDTO,
        request: HttpServletRequest
    ): ResponseEntity<ClientResponseDTO> {
        // Authorization: check ownership
        val existingClient = clientService.getClientById(id)
        authHelper.requireClientOwnership(request, existingClient.idmUserId)

        val client = clientService.updateClient(id, dto)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // DELETE - DELETE /api/clients/{id}
    @Operation(summary = "Delete client", description = "Deletes a client (only if they have no tickets). Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Client deleted"),
        ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        ApiResponse(responseCode = "404", description = "Client not found"),
        ApiResponse(responseCode = "422", description = "Client has purchased tickets")
    ])
    @DeleteMapping("/{id}")
    fun deleteClient(
        @Parameter(description = "Client ID")
        @PathVariable id: String,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        // Authorization: check ownership
        val existingClient = clientService.getClientById(id)
        authHelper.requireClientOwnership(request, existingClient.idmUserId)

        clientService.deleteClient(id)
        return ResponseEntity.noContent().build()
    }

    // BILETE - POST /api/clients/{id}/bilete (cumpara bilet)
    @Operation(summary = "Purchase ticket", description = "Purchases a ticket for an event or package. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Ticket purchased successfully"),
        ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        ApiResponse(responseCode = "404", description = "Client or event/package not found"),
        ApiResponse(responseCode = "409", description = "Invalid data (must provide eventId OR packageId)")
    ])
    @PostMapping("/{id}/bilete")
    fun cumparaBilet(
        @Parameter(description = "Client ID")
        @PathVariable id: String,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Ticket purchase data - provide either evenimentId OR pachetId",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CumparaBiletDTO::class),
                examples = [
                    ExampleObject(
                        name = "Buy ticket for event",
                        value = """{"evenimentId": 5}"""
                    ),
                    ExampleObject(
                        name = "Buy ticket for package",
                        value = """{"pachetId": 3}"""
                    )
                ]
            )]
        )
        @RequestBody dto: CumparaBiletDTO,
        request: HttpServletRequest
    ): ResponseEntity<ClientResponseDTO> {
        // Authorization: check ownership (clients buy tickets for themselves)
        val existingClient = clientService.getClientById(id)
        authHelper.requireClientOwnership(request, existingClient.idmUserId)

        // Extrage token-ul din header pentru a-l pasa la Events Service
        val authToken = request.getHeader("Authorization")

        val client = clientService.cumparaBilet(id, dto, authToken)
        val response = toResponseDTO(client)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // BILETE - DELETE /api/clients/{id}/bilete/{codBilet} (sterge/returneaz bilet)
    @Operation(summary = "Return ticket", description = "Removes a ticket from client and deletes it from Events Service. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Ticket returned"),
        ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        ApiResponse(responseCode = "404", description = "Client or ticket not found")
    ])
    @DeleteMapping("/{id}/bilete/{codBilet}")
    fun returneazaBilet(
        @Parameter(description = "Client ID")
        @PathVariable id: String,
        @Parameter(description = "Ticket code")
        @PathVariable codBilet: String,
        request: HttpServletRequest
    ): ResponseEntity<ClientResponseDTO> {
        // Authorization: check ownership
        val existingClient = clientService.getClientById(id)
        authHelper.requireClientOwnership(request, existingClient.idmUserId)

        // Extrage token-ul din header pentru a-l pasa la Events Service
        val authToken = request.getHeader("Authorization")

        val client = clientService.returneazaBilet(id, codBilet, authToken)
        val response = toResponseDTO(client)
        return ResponseEntity.ok(response)
    }

    // HELPER - converteste Client -> ClientResponseDTO cu HATEOAS
    private fun toResponseDTO(client: com.pos.serviciu_clienti.model.Client): ClientResponseDTO {
        val selfLink = linkTo(methodOn(ClientController::class.java).getClientById(client.id!!)).withSelfRel()
        val allClientsLink = linkTo(methodOn(ClientController::class.java).getClients(null, 0, 10)).withRel("all-clients")

        return ClientResponseDTO(
            id = client.id!!,
            idmUserId = client.idmUserId,
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
            idmUserId = client.idmUserId,
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


    // BILETE - GET /api/clients/{id}/bilete (format lung)
    @Operation(summary = "Get detailed tickets", description = "Returns list of tickets in long format with event/package details")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Ticket list"),
        ApiResponse(responseCode = "404", description = "Client not found")
    ])
    @GetMapping("/{id}/bilete")
    fun getBileteClient(
        @Parameter(description = "Client ID")
        @PathVariable id: String
    ): ResponseEntity<Map<String, Any>> {
        val client = clientService.getClientById(id)
        val bileteDetaliate = clientService.getBileteDetaliate(id)

        val selfLink = linkTo(methodOn(ClientController::class.java).getBileteClient(id)).withSelfRel()
        val clientLink = linkTo(methodOn(ClientController::class.java).getClientById(id)).withRel("client")

        val response: Map<String, Any> = mapOf(
            "clientId" to (client.id ?: ""),
            "clientEmail" to client.email,
            "totalBilete" to bileteDetaliate.size,
            "bilete" to bileteDetaliate,
            "links" to mapOf(
                "self" to selfLink.href,
                "client" to clientLink.href
            )
        )

        return ResponseEntity.ok(response)
    }
}