package com.pos.serviciu_clienti.security

data class AuthenticatedUser(
    val userId: Int,
    val role: String
) {
    fun isAdmin(): Boolean = role == "admin"

    fun isOwnerEvent(): Boolean = role == "owner-event"

    fun isClient(): Boolean = role == "client"

    fun canManageClient(clientIdmUserId: Int?): Boolean {
        if (isAdmin()) return true
        if (isClient() && clientIdmUserId != null) {
            return clientIdmUserId == userId
        }
        return false
    }
}
