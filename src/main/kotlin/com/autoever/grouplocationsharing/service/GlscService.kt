package com.autoever.grouplocationsharing.service

import com.autoever.grouplocationsharing.common.SmsClient
import com.autoever.grouplocationsharing.exception.BusinessException
import com.autoever.grouplocationsharing.exception.ErrorCode
import com.autoever.grouplocationsharing.model.Location
import com.autoever.grouplocationsharing.model.Room
import com.autoever.grouplocationsharing.model.User
import com.autoever.grouplocationsharing.model.UserRoom
import com.autoever.grouplocationsharing.repository.LocationRepository
import com.autoever.grouplocationsharing.repository.RoomRepository
import com.autoever.grouplocationsharing.repository.UserRepository
import com.autoever.grouplocationsharing.repository.UserRoomRepository
import com.autoever.grouplocationsharing.service.dto.CreateRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.DeleteRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.FindRoomOutputDTO
import com.autoever.grouplocationsharing.service.dto.JoinRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.MemberLocationDTO
import com.autoever.grouplocationsharing.service.dto.RoomStatusOutputDTO
import com.autoever.grouplocationsharing.service.dto.UpdateLocationInputDTO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GlscService(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val userRoomRepository: UserRoomRepository,
    private val locationRepository: LocationRepository,
    private val smsClient: SmsClient,
) {
    @Transactional
    suspend fun createRoom(inputDto: CreateRoomInputDTO): Room {
        val roomNumber = roomRepository.nextRoomNumber()
        val room = Room(
            roomNumber = roomNumber,
            roomName = inputDto.roomName,
            creatorId = inputDto.creatorId,
            maxMembers = inputDto.maxMembers,
        )
        val saved = roomRepository.save(room)
        userRoomRepository.save(UserRoom(userId = inputDto.creatorId, roomId = saved.id!!))
        return saved
    }

    @Transactional
    suspend fun deleteRoom(inputDto: DeleteRoomInputDTO): Boolean {
        val room = roomRepository.findByRoomNumber(inputDto.roomNumber)
            ?: throw BusinessException(ErrorCode.ROOM_NOT_FOUND)

        userRoomRepository.deleteByRoomId(room.id!!)
        roomRepository.delete(room)
        return true

    }

    suspend fun getAllRoom(): List<Room> = roomRepository.findAll().toList()

    suspend fun getRoomByPhoneNumber(phoneNumber: String): List<FindRoomOutputDTO> =
        roomRepository.findByPhoneNumber(phoneNumber).toList()

    suspend fun getAllRoomStatus(): List<RoomStatusOutputDTO> {
        val rooms = roomRepository.findAll().toList()

        return rooms.map { room ->
            val userRooms = userRoomRepository.findByRoomId(room.id!!).toList()
            val userIds = userRooms.map { it.userId }

            val users = userRepository.findAllById(userIds).toList()
            val locationByUserId = locationRepository.findLatestByUserIdIn(userIds)
                .toList()
                .associateBy { it.userId }

            val members = users.map { user ->
                val location = locationByUserId[user.id]
                MemberLocationDTO(
                    userId = user.id!!,
                    name = user.name,
                    phoneNumber = user.phoneNumber,
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                )
            }

            RoomStatusOutputDTO(
                roomId = room.id,
                roomNumber = room.roomNumber,
                roomName = room.roomName,
                maxMembers = room.maxMembers,
                currentMembers = userRooms.size,
                members = members,
            )
        }
    }

    // ========= UserRoom ==========
    @Transactional
    suspend fun joinRoom(request: JoinRoomInputDTO): Boolean {
        val room = roomRepository.findByRoomNumber(request.roomNumber)
            ?: throw BusinessException(ErrorCode.ROOM_NOT_FOUND)
        val user = userRepository.findByPhoneNumber(request.phoneNumber)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (userRoomRepository.existsByUserIdAndRoomId(user.id!!, room.id!!)) {
            throw BusinessException(ErrorCode.ALREADY_JOINED)
        }

        val result = roomRepository.joinUserRoom(UserRoom(userId = user.id, roomId = room.id))
        if (!result) {
            throw BusinessException(ErrorCode.ROOM_FULL)
        }

        roomRepository.findUserRoomByRoomId(room.id)
            .filter { it.id != user.id }
            .collect { member -> smsClient.send(
                from = "010-1234-5678",
                to = member.phoneNumber,
                content = "${user.name}님이 그룹 ${room.roomName}에 참여하였습니다."
            ) }

        return result
    }

    @Transactional
    suspend fun leaveRoom(request: JoinRoomInputDTO): Boolean {
        val room = roomRepository.findByRoomNumber(request.roomNumber)
            ?: throw BusinessException(ErrorCode.ROOM_NOT_FOUND)
        val user = userRepository.findByPhoneNumber(request.phoneNumber)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (!userRoomRepository.existsByUserIdAndRoomId(user.id!!, room.id!!)) {
            throw BusinessException(ErrorCode.NOT_JOINED)
        }

        val result = userRoomRepository.deleteByUserIdAndRoomId(user.id, room.id)

        roomRepository.findUserRoomByRoomId(room.id)
            .filter { it.id != user.id }
            .collect { member -> smsClient.send(
                from = "010-1234-5678",
                to = member.phoneNumber,
                content = "${user.name}님이 그룹 ${room.roomName}에서 퇴장하셨습니다."
            ) }

        return result > 0
    }

    // ======== Location =========
    suspend fun updateLocation(request: UpdateLocationInputDTO): Location {
        if (!userRoomRepository.existsByUserId(userId = request.userId)) {
            throw BusinessException(ErrorCode.LOCATION_NOT_ALLOWED)
        }

        return locationRepository.save(Location(
            userId = request.userId,
            latitude = request.latitude,
            longitude = request.longitude,
        ))
    }

    suspend fun getCurrentLocations(roomNumber: String): List<Location> {
        val room = roomRepository.findByRoomNumber(roomNumber)
            ?: throw BusinessException(ErrorCode.ROOM_NOT_FOUND)

        val userIds = userRoomRepository.findByRoomId(room.id!!)
            .map { it.userId }
            .toList()

        if (userIds.isEmpty()) return emptyList()

        return locationRepository.findLatestByUserIdIn(userIds).toList()
    }
}
