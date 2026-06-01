package com.autoever.grouplocationsharing.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("user_rooms")
data class UserRoom(
    val userId: Long,
    val roomId: Long,
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    val id: Long? = null,
)
