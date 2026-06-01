package com.autoever.grouplocationsharing.repository

import com.autoever.grouplocationsharing.model.Room
import com.autoever.grouplocationsharing.model.User
import com.autoever.grouplocationsharing.model.UserRoom
import com.autoever.grouplocationsharing.service.dto.FindRoomOutputDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import java.time.LocalDateTime

class RoomRepositoryCustomImpl(
    private val databaseClient: DatabaseClient,
) : RoomRepositoryCustom {

    // ROOM-001 형식의 방번호 채번
    override suspend fun nextRoomNumber(): String {
        val seq = databaseClient
            .sql("SELECT NEXT VALUE FOR room_number_seq")
            .map { row, _ -> row.get(0, java.lang.Long::class.java)!!.toLong() }
            .one()
            .awaitSingle()
        return "ROOM-%03d".format(seq)
    }

    override fun findByPhoneNumber(phoneNumber: String): Flow<FindRoomOutputDTO> = databaseClient.sql(
        """
            SELECT r.*
                 , uc.name AS creator_name
                 , (SELECT COUNT(*) FROM user_rooms WHERE room_id = r.id) AS current_members
              FROM user_rooms ur
             INNER JOIN users u ON ur.user_id = u.id
             INNER JOIN rooms r ON ur.room_id = r.id
             INNER JOIN users uc ON r.creator_id = uc.id
             WHERE u.phone_number = :phoneNumber
             ORDER BY ur.joined_at
            """.trimIndent()
    )
        .bind("phoneNumber", phoneNumber)
        .map { row, _ ->
            FindRoomOutputDTO(
                roomNumber = row.get("room_number", String::class.java)!!,
                roomName = row.get("room_name", String::class.java)!!,
                creatorId = row.get("creator_id", java.lang.Long::class.java)!!.toLong(),
                creatorName = row.get("creator_name", String::class.java)!!,
                maxMembers = row.get("max_members", Number::class.java)!!.toInt(),
                currentMembers = row.get("current_members", Number::class.java)!!.toInt(),
                createdAt = row.get("created_at", LocalDateTime::class.java)!!,
            )
        }
        .all()
        .asFlow()

    // 그룹방 참여 시, SQL을 통한 인원 확인 후 저장 (동시성 문제)
    override suspend fun joinUserRoom(userRoom: UserRoom): Boolean {
        val updated = databaseClient.sql(
            """
                INSERT INTO user_rooms(user_id, room_id, joined_at)
                SELECT :userId, :roomId, CURRENT_TIMESTAMP
                WHERE (
                    SELECT COUNT(*)
                    FROM user_rooms
                    WHERE room_id = :roomId
                ) < (
                    SELECT max_members
                    FROM rooms
                    WHERE id = :roomId
                )
            """
        )
            .bind("userId", userRoom.userId)
            .bind("roomId", userRoom.roomId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return updated == 1L
    }

    override fun findUserRoomByRoomId(roomId: Long): Flow<User> = databaseClient.sql(
        """
            SELECT u.id
                 , u.name
                 , u.phone_number
              FROM user_rooms ur
             INNER JOIN users u ON ur.user_id = u.id
             WHERE ur.room_id = :roomId
            """
    )
        .bind("roomId", roomId)
        .map { row -> User(
            id = row.get("id", java.lang.Long::class.java)!!.toLong(),
            name = row.get("name", String::class.java)!!,
            phoneNumber = row.get("phone_number", String::class.java)!!,
        ) }
        .all()
        .asFlow()
}
