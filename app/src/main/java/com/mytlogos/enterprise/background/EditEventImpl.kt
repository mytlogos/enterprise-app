package com.mytlogos.enterprise.background

import org.joda.time.DateTime

data class EditEventImpl(
    override val id: Int,
    @EditService.EditObject override val objectType: Int,
    @EditService.Event override val eventType: Int,
    override val dateTime: DateTime,
    override val firstValue: String,
    override val secondValue: String,
) : EditEvent {

    constructor(
        id: Int,
        objectType: Int,
        eventType: Int,
        firstValue: Any?,
        secondValue: Any?
    ) : this(id, objectType, eventType, DateTime.now(), firstValue.toString(), secondValue.toString())
}