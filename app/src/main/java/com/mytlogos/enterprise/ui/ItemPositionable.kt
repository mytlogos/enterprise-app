package com.mytlogos.enterprise.ui

interface ItemPositionable<Value : Any> {
    fun getItemAt(position: Int) : Value?
}