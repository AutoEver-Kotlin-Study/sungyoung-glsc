package com.autoever.grouplocationsharing.repository

import com.autoever.grouplocationsharing.model.UserRoom
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRoomRepository : CoroutineCrudRepository<UserRoom, Long> {
    fun findByUserId(userId: Long): Flow<UserRoom>
    fun findByRoomId(roomId: Long): Flow<UserRoom>

    suspend fun existsByUserIdAndRoomId(userId: Long, roomId: Long): Boolean
    suspend fun existsByUserId(userId: Long): Boolean
    suspend fun countByRoomId(roomId: Long): Long      // 방에 속한 현재 멤버 수

    @Modifying
    @Query("DELETE FROM user_rooms WHERE user_id = :userId AND room_id = :roomId")
    suspend fun deleteByUserIdAndRoomId(userId: Long, roomId: Long): Int
    @Modifying
    suspend fun deleteByRoomId(roomId: Long): Int

}
