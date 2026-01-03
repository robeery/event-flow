package com.pos.serviciu_clienti.service



import com.pos.serviciu_clienti.dto.*
import com.pos.serviciu_clienti.model.Client
import com.pos.serviciu_clienti.repository.ClientRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class ClientService(
    private val clientRepository: ClientRepository,
    private val eventsServiceClient: EventsServiceClient
) {

    // CREATE - Creeaza client nou
    fun createClient(dto: ClientRequestDTO): Client {
        // verific mail
        if (clientRepository.existsByEmail(dto.email)) {
            throw IllegalArgumentException("Email-ul '${dto.email}' este deja folosit")
        }

        val client = Client(
            email = dto.email,
            prenume = dto.prenume,
            nume = dto.nume,
            dateSuntPublice = dto.dateSuntPublice,
            linkuriSocialMedia = dto.linkuriSocialMedia
        )

        return clientRepository.save(client)
    }

    // READ - gaseste client dupa id
    fun getClientById(id: String): Client {
        return clientRepository.findById(id)
            .orElseThrow { NoSuchElementException("Clientul cu ID '$id' nu a fost gasit") }
    }

    // READ - -||- dupa mail
    fun getClientByEmail(email: String): Client {
        return clientRepository.findByEmail(email)
            .orElseThrow { NoSuchElementException("Clientul cu email '$email' nu a fost gasit") }
    }

    // READ - toti clientii cu paginare
    fun getAllClients(pageable: Pageable): Page<Client> {
        return clientRepository.findAll(pageable)
    }

    // UPDATE - actualizeaza client
    fun updateClient(id: String, dto: ClientRequestDTO): Client {
        val clientExistent = getClientById(id)

        // verific daca email nou e disponibil
        if (dto.email != clientExistent.email && clientRepository.existsByEmail(dto.email)) {
            throw IllegalArgumentException("Email-ul '${dto.email}' este deja folosit")
        }


        val clientActualizat = Client(
            id = id,
            email = dto.email,
            prenume = dto.prenume,
            nume = dto.nume,
            dateSuntPublice = dto.dateSuntPublice,
            linkuriSocialMedia = dto.linkuriSocialMedia,
            bileteAchizitionate = clientExistent.bileteAchizitionate
        )

        return clientRepository.save(clientActualizat)
    }

    // DELETE - sterge client
    fun deleteClient(id: String) {
        val client = getClientById(id)

        // daca are bilete nu pot sterge
        if (client.bileteAchizitionate.isNotEmpty()) {
            throw IllegalStateException(
                "Nu se poate sterge clientul cu ID '$id' deoarece are ${client.bileteAchizitionate.size} bilet/e achiziționate"
            )
        }

        clientRepository.deleteById(id)
    }

    // BILETE - Cumpara bilet (ACTUALIZAT)
    fun cumparaBilet(clientId: String, dto: CumparaBiletDTO): Client {
        // Validare DTO
        if (!dto.isValid()) {
            throw IllegalArgumentException("Trebuie sa specifici fie evenimentId, fie pachetId (nu ambele, nu niciunul)")
        }

        val client = getClientById(clientId)

        // Genereaza cod bilet
        val codBilet = eventsServiceClient.generateCodBilet()

        // Creeaza biletul in Events Service
        eventsServiceClient.createBilet(codBilet, dto.evenimentId, dto.pachetId)

        // Adauga codul biletului la client
        client.bileteAchizitionate.add(codBilet)

        return clientRepository.save(client)
    }

    // BILETE - Sterge bilet de la client
    fun returneazaBilet(clientId: String, codBilet: String): Client {
        val client = getClientById(clientId)

        if (!client.bileteAchizitionate.contains(codBilet)) {
            throw NoSuchElementException("Biletul '$codBilet' nu exista la acest client")
        }

        // Sterge biletul din Events Service
        eventsServiceClient.deleteBilet(codBilet)

        // Sterge din lista clientului
        client.bileteAchizitionate.remove(codBilet)

        return clientRepository.save(client)
    }





    // BILETE - Obtine lista de bilete in format lung
    fun getBileteDetaliate(clientId: String): List<BiletDetaliatDTO> {
        val client = getClientById(clientId)

        return client.bileteAchizitionate.mapNotNull { codBilet ->
            val biletInfo = eventsServiceClient.getBiletComplet(codBilet)

            if (biletInfo != null) {
                when {
                    // Bilet pentru eveniment
                    biletInfo.evenimentId != null -> {
                        val eveniment = eventsServiceClient.getEveniment(biletInfo.evenimentId)
                        BiletDetaliatDTO(
                            cod = codBilet,
                            tip = "eveniment",
                            evenimentId = biletInfo.evenimentId,
                            evenimentNume = eveniment?.nume,
                            evenimentLocatie = eveniment?.locatie,
                            evenimentDescriere = eveniment?.descriere,
                            evenimentNumarLocuri = eveniment?.numarLocuri,
                            evenimentBileteDisponibile = eveniment?.bileteDisponibile
                        )
                    }
                    // Bilet pentru pachet
                    biletInfo.pachetId != null -> {
                        val pachet = eventsServiceClient.getPachet(biletInfo.pachetId)
                        val evenimentePachet = eventsServiceClient.getEvenimentePachet(biletInfo.pachetId)
                        BiletDetaliatDTO(
                            cod = codBilet,
                            tip = "pachet",
                            pachetId = biletInfo.pachetId,
                            pachetNume = pachet?.nume,
                            pachetLocatie = pachet?.locatie,
                            pachetDescriere = pachet?.descriere,
                            pachetNumarLocuri = pachet?.numarLocuri,
                            pachetBileteDisponibile = pachet?.bileteDisponibile,
                            evenimenteIncluse = evenimentePachet.mapNotNull { it.nume }
                        )
                    }
                    else -> {
                        BiletDetaliatDTO(cod = codBilet, tip = "necunoscut")
                    }
                }
            } else {
                BiletDetaliatDTO(cod = codBilet, tip = "invalid")
            }
        }
    }


}