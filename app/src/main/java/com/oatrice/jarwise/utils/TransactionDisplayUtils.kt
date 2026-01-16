package com.oatrice.jarwise.utils

object TransactionDisplayUtils {
    fun getDisplayDetails(jarName: String, note: String): Pair<String, String> {
        val trimmedNote = note.trim()
        return if (trimmedNote.isNotEmpty()) {
            Pair(trimmedNote, jarName)
        } else {
            Pair(jarName, "")
        }
    }
}
