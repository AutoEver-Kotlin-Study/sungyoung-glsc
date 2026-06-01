package com.autoever.grouplocationsharing.service.dto

import java.time.LocalDateTime

data class CreateRoomInputDTO(
    val roomName: String,
    val creatorId: Long,
    val maxMembers: Int,
)

data class DeleteRoomInputDTO(
    val roomNumber: String,
    val creatorId: Long,
)

data class FindRoomOutputDTO(
    val roomNumber: String,
    val roomName: String,
    val creatorId: Long,
    val creatorName: String,
    val maxMembers: Int,
    var currentMembers: Int,

    val createdAt: LocalDateTime,
)

data class JoinRoomInputDTO(
    val phoneNumber: String,
    val roomNumber: String,
)

data class FindUserRoomOutputDTO(
    val roomNumber: String,
    val roomName: String,
    val creatorId: Long,
    val creatorName: String,
    val maxMembers: Int,
    var currentMembers: Int,

    val createdAt: LocalDateTime,
)

data class RoomStatusOutputDTO(
    val roomId: Long,
    val roomNumber: String,
    val roomName: String,
    val maxMembers: Int,
    val currentMembers: Int,
    val members: List<MemberLocationDTO>,
)

data class MemberLocationDTO(
    val userId: Long,
    val name: String,
    val phoneNumber: String,
    val latitude: Double?,
    val longitude: Double?,
)