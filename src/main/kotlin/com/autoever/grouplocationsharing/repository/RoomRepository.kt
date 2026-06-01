package com.autoever.grouplocationsharing.repository

import com.autoever.grouplocationsharing.model.Room
import com.autoever.grouplocationsharing.service.dto.FindRoomOutputDTO
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RoomRepository : CoroutineCrudRepository<Room, Long>, RoomRepositoryCustom {
    suspend fun existsByRoomNumber(roomNumber: String): Boolean
    suspend fun findByRoomNumber(roomNumber: String): Room?
}
