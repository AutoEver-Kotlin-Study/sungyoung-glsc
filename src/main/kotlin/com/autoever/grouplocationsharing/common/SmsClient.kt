package com.autoever.grouplocationsharing.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class SmsClient(
    @Value("\${sms.url}") url: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.builder().baseUrl(url).build()

    suspend fun send(from: String, to: String, content: String) = withContext(Dispatchers.IO) {
        runCatching {
            restClient.post()
                .uri("/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(SmsRequest(from = from, to = to, content = content))
                .retrieve()
                .toBodilessEntity()
            log.info("SMS 발송 성공: to={}, content={}", to, content)
        }.onFailure {
            log.warn("SMS 발송 실패: to={}, content={}", to, content, it)
            throw IllegalArgumentException("SMS 발송 실패")
        }
    }
}

private data class SmsRequest(val from: String, val to: String, val content: String)
