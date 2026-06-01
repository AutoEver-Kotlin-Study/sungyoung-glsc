package com.autoever.grouplocationsharing.controller

import com.autoever.grouplocationsharing.model.Location
import com.autoever.grouplocationsharing.model.Room
import com.autoever.grouplocationsharing.service.GlscService
import com.autoever.grouplocationsharing.service.dto.CreateRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.DeleteRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.FindRoomOutputDTO
import com.autoever.grouplocationsharing.service.dto.JoinRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.UpdateLocationInputDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class GlscController(private val glscService: GlscService) {

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createRoom(@RequestBody request: CreateRoomInputDTO): Room =
        glscService.createRoom(request)

    @DeleteMapping("/rooms/{roomNumber}")
    suspend fun deleteRoom(
        @PathVariable roomNumber: String,
        @RequestParam creatorId: Long,
    ): Boolean = glscService.deleteRoom(DeleteRoomInputDTO(roomNumber = roomNumber, creatorId = creatorId))

    @GetMapping("/rooms")
    suspend fun getRooms(@RequestParam phoneNumber: String): List<FindRoomOutputDTO> =
        glscService.getRoomByPhoneNumber(phoneNumber)

    @PostMapping("/rooms/{roomNumber}/join")
    suspend fun joinRoom(
        @PathVariable roomNumber: String,
        @RequestBody phoneNumber: String,
    ): Boolean = glscService.joinRoom(JoinRoomInputDTO(phoneNumber = phoneNumber, roomNumber = roomNumber))

    @DeleteMapping("/rooms/{roomNumber}/leave")
    suspend fun leaveRoom(
        @PathVariable roomNumber: String,
        @RequestParam phoneNumber: String,
    ): Boolean = glscService.leaveRoom(JoinRoomInputDTO(phoneNumber = phoneNumber, roomNumber = roomNumber))

    @GetMapping("/rooms/{roomNumber}/locations")
    suspend fun getLocations(@PathVariable roomNumber: String): List<Location> =
        glscService.getCurrentLocations(roomNumber)

    @PutMapping("/locations")
    suspend fun updateLocation(@RequestBody request: UpdateLocationInputDTO): Location =
        glscService.updateLocation(request)
}

