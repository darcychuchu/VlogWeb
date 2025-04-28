package org.vlog.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VlogWebApplication

fun main(args: Array<String>) {
    runApplication<VlogWebApplication>(*args)
}
