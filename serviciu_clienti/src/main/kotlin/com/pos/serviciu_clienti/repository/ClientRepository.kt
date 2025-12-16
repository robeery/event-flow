package com.pos.serviciu_clienti.repository



import com.pos.serviciu_clienti.model.Client
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ClientRepository : MongoRepository<Client, String> {

    // gasesc client by email
    fun findByEmail(email: String): Optional<Client>

    // verific daca exista dupa mail
    fun existsByEmail(email: String): Boolean


}