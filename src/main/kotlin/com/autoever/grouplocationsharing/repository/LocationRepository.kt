package com.autoever.grouplocationsharing.repository

import com.autoever.grouplocationsharing.model.Location
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LocationRepository : CoroutineCrudRepository<Location, Long> {
    @Query("""
        SELECT * FROM locations l1
        WHERE l1.user_id IN (:userIds)
          AND l1.recorded_at = (
              SELECT MAX(l2.recorded_at) FROM locations l2 WHERE l2.user_id = l1.user_id
          )
    """)
    fun findLatestByUserIdIn(userIds: Collection<Long>): Flow<Location>
}
