package com.autoever.grouplocationsharing.service.dto

import java.time.LocalDateTime

data class UpdateLocationInputDTO(
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
)
