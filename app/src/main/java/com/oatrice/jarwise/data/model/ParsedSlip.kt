package com.oatrice.jarwise.data.model

import java.util.Date

data class ParsedSlip(
    val date: Date? = null,
    val amount: Double? = null,
    val bankName: String? = null,
    val senderAccount: String? = null,
    val receiverAccount: String? = null,
    val rawText: String = ""
)
