package com.quarantine.thirtyseconds.utils

import java.lang.Exception

class NicknameTakenException : Exception() {
    override val message: String?
        get() = "This nickname has already been taken. Please don't tell Liam Neelson"
}