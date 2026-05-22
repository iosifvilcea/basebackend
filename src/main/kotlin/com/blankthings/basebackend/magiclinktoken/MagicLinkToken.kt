package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.time.Instant

const val EXPIRATION_TIME_15_MINS_IN_SECONDS = 900L

@Entity
@Table(name = "magic_link_tokens")
class MagicLinkToken(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mlt_seq")
    @SequenceGenerator(name = "mlt_seq", sequenceName = "mlt_id_seq", allocationSize = 1)
    val id: Long = 0,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,
    @Column(nullable = false, unique = true)
    val tokenHash: String,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant = Instant.now().plusSeconds(EXPIRATION_TIME_15_MINS_IN_SECONDS),
    private var used: Boolean = false,
) {
    fun copy(
        id: Long = this.id,
        user: User = this.user,
        tokenHash: String = this.tokenHash,
        expiresAt: Instant = this.expiresAt,
        createdAt: Instant = this.createdAt,
        used: Boolean = this.used,
    ): MagicLinkToken =
        MagicLinkToken(
            id = id,
            user = user,
            tokenHash = tokenHash,
            createdAt = createdAt,
            expiresAt = expiresAt,
            used = used,
        )

    fun markAsUsed() {
        this.used = true
    }

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun isValid(): Boolean = !used && !isExpired()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MagicLinkToken
        if (id != other.id) return false
        if (user.id != other.user.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + user.id.hashCode()
        return result
    }
}
