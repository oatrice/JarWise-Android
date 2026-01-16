package com.oatrice.jarwise.utils

object TransactionDisplayUtils {
    fun getDisplayDetails(jarName: String, note: String): Pair<String, String> {
        return Pair(jarName, note.trim())
    }
}
