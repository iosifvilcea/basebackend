package com.blankthings.basebackend.email

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val analyticsTracker: AnalyticsTracker,
    @Value("\${app.url}") private val url: String,
    @Value("\${app.email.from}") private val from: String,
    @Value("\${app.email.subject}") private val subject: String,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendAuthEmail(
        email: String,
        token: String,
    ) {
        val normalizedEmail = email.trim().lowercase()
        val message =
            SimpleMailMessage().apply {
                setTo(normalizedEmail)
                this.from = from
                this.subject = subject
                text = "Here's your login link:\n\n$url/api/auth?token=$token"
            }

        try {
            mailSender.send(message)
            analyticsTracker.track(AnalyticsEvent.EMAIL_SENT, normalizedEmail)
        } catch (ex: MailException) {
            analyticsTracker.track(AnalyticsEvent.EMAIL_FAILED, normalizedEmail)
            logger.error("Failed to send auth email to {}", normalizedEmail, ex)
            throw EmailDeliveryException()
        }
    }
}
