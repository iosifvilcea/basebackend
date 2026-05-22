package com.blankthings.basebackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BaseBackendApplication

fun main(args: Array<String>) {
    runApplication<BaseBackendApplication>(*args)
}
