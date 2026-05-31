package com.autoever.grouplocationsharing

import com.autoever.grouplocationsharing.service.GlscService
import com.autoever.grouplocationsharing.service.dto.CreateRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.DeleteRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.JoinRoomInputDTO
import com.autoever.grouplocationsharing.service.dto.UpdateLocationInputDTO
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertFails

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class GroupLocationSharingApplicationTests @Autowired constructor(
    private val glscService: GlscService,
    var objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    @Order(1)
    fun contextLoads() {
    }

    @Test
    @Order(2)
    fun testGetAllRooms() = runTest {
        val roomList = glscService.getAllRoom()
        log.info("roomList : \n{}", printObjectPretty(roomList))
    }

    @Test
    @Order(3)
    fun testGetRoomsByUserPhoneNumber() = runTest {
        val roomList = glscService.getRoomByPhoneNumber("010-1111-2222")
        log.info("roomList : \n{}", printObjectPretty(roomList))
    }

    @Test
    @Order(4)
    fun testSaveLocation() = runTest {
        glscService.updateLocation(UpdateLocationInputDTO(1L, 37.4802828988503, longitude = 126.947076662905))
    }

    @Test
    @Order(5)
    fun testGetCurrentLocations() = runTest {
        val roomList1 = glscService.getCurrentLocations("ROOM-002")
        log.info("roomList1 : \n{}", printObjectPretty(roomList1))

        glscService.updateLocation(UpdateLocationInputDTO(2L, 37.0, longitude = 126.0))
        glscService.updateLocation(UpdateLocationInputDTO(2L, 37.1, longitude = 126.1))
        glscService.updateLocation(UpdateLocationInputDTO(2L, 37.2, longitude = 126.2))

        val roomList2 = glscService.getCurrentLocations("ROOM-002")
        log.info("roomList2 : \n{}", printObjectPretty(roomList2))

        printJoinStatus()
    }

    @Test
    @Order(6)
    fun testJoinRoomExist() = runTest {
        // 박민준(010-4444-5555)은 이미 ROOM-002에 입장해 있음
        assertFails { glscService.joinRoom(JoinRoomInputDTO(phoneNumber = "010-4444-5555", roomNumber = "ROOM-002")) }
    }

    @Test
    @Order(7)
    fun testLeaveRoom() = runTest {
        // 홍길동(010-1111-2222)이 ROOM-002에 입장 후 퇴장
        glscService.joinRoom(JoinRoomInputDTO(phoneNumber = "010-1111-2222", roomNumber = "ROOM-002"))

        val roomList1 = glscService.getRoomByPhoneNumber("010-1111-2222")
        log.info("roomList1 : \n{}", printObjectPretty(roomList1))

        glscService.leaveRoom(JoinRoomInputDTO(phoneNumber = "010-1111-2222", roomNumber = "ROOM-002"))

        val roomList2 = glscService.getRoomByPhoneNumber("010-1111-2222")
        log.info("roomList2 : \n{}", printObjectPretty(roomList2))
    }

    @Test
    @Order(8)
    fun testJoinRoom() = runTest {
        // 홍길동(010-1111-2222)이 ROOM-002에 입장 → 4/4 정원 꽉 참
        val roomList1 = glscService.getRoomByPhoneNumber("010-1111-2222")
        log.info("roomList1 : \n{}", printObjectPretty(roomList1))

        glscService.joinRoom(JoinRoomInputDTO(phoneNumber = "010-1111-2222", roomNumber = "ROOM-002"))

        val roomList2 = glscService.getRoomByPhoneNumber("010-1111-2222")
        log.info("roomList2 : \n{}", printObjectPretty(roomList2))
    }

    @Test
    @Order(9)
    fun testJoinRoomMax() = runTest {
        // ROOM-002가 4/4로 꽉 찬 상태에서 재입장 시도 → 실패
        assertFails { glscService.joinRoom(JoinRoomInputDTO(phoneNumber = "010-1111-2222", roomNumber = "ROOM-002")) }
    }

    @Test
    @Order(10)
    fun testSaveRoom() = runTest {
        glscService.createRoom(CreateRoomInputDTO(roomName = "TEST방", creatorId = 1L, 5))

        val roomList = glscService.getAllRoom()
        log.info("roomList : \n{}", printObjectPretty(roomList))
    }

    @Test
    @Order(11)
    fun testDeleteRoom() = runTest {
        printJoinStatus()
        glscService.deleteRoom(DeleteRoomInputDTO(roomNumber = "ROOM-003", creatorId = 1L))
        printJoinStatus()
    }

    private suspend fun printJoinStatus() {
        val list = glscService.getAllRoomStatus()
        log.info(
            "방 참여 현황:\n{}",
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(list)
        )
    }

    private fun printObjectPretty(obj: Any): String {
        return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(obj)
    }
}
