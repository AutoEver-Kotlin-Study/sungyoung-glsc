package com.autoever.grouplocationsharing.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    val name: String,
    val phoneNumber: String,

    @Id
    val id: Long? = null,
) {
    fun update(name: String, phoneNumber: String) = copy(
        name = name,
        phoneNumber = phoneNumber,
    )
}
