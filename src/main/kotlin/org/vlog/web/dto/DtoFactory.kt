package org.vlog.web.dto

interface DtoFactory<I, O> {
    fun of(model: I): O
}