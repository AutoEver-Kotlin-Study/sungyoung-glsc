package com.autoever.grouplocationsharing.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("rooms")
data class Room(
    val roomNumber: String,
    val roomName: String,
    val creatorId: Long,
    val maxMembers: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    val id: Long? = null,
)
