package com.kenjdavidson.golf.handicap.util

import org.slf4j.LoggerFactory

inline fun <R> Any.operation(description: String, block: () -> R): R {
    val logger = LoggerFactory.getLogger(this::class.java)

    logger.info("Starting operation: {}", description)
    val startTime = System.currentTimeMillis()

    try {
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        logger.info("Successfully completed operation: {} (took {}ms)", description, duration)
        return result
    } catch (ex: Throwable) {
        val duration = System.currentTimeMillis() - startTime
        logger.error("Failed operation: {} (failed after {}ms). Error: {}", description, duration, ex.message, ex)
        throw ex
    }
}

inline fun <R> Any.debug(description: String, block: () -> R): R {
    val logger = LoggerFactory.getLogger(this::class.java)

    logger.debug("Starting operation: {}", description)
    val startTime = System.currentTimeMillis()

    try {
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        logger.debug("Successfully completed operation: {} (took {}ms)", description, duration)
        return result
    } catch (ex: Throwable) {
        val duration = System.currentTimeMillis() - startTime
        logger.error("Failed operation: {} (failed after {}ms). Error: {}", description, duration, ex.message, ex)
        throw ex
    }
}