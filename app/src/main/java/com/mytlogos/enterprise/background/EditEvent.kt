package com.mytlogos.enterprise.background

import org.joda.time.DateTime

interface EditEvent {
    val id: Int
    val objectType: Int
    val eventType: Int
    val dateTime: DateTime
    val firstValue: String
    val secondValue: String
}