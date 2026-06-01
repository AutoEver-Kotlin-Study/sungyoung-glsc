package com.autoever.grouplocationsharing.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("locations")
data class Location(
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
    val recordedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    val id: Long? = null,
)

