package com.autoever.grouplocationsharing.repository

import com.autoever.grouplocationsharing.model.Room
import com.autoever.grouplocationsharing.model.User
import com.autoever.grouplocationsharing.model.UserRoom
import com.autoever.grouplocationsharing.service.dto.FindRoomOutputDTO
import kotlinx.coroutines.flow.Flow

interface RoomRepositoryCustom {
    suspend fun nextRoomNumber(): String
    fun findByPhoneNumber(phoneNumber: String): Flow<FindRoomOutputDTO>
    suspend fun joinUserRoom(userRoom: UserRoom): Boolean
    fun findUserRoomByRoomId(roomId: Long): Flow<User>
}
