package com.oatrice.jarwise.utils

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> // Field -> Error Message
)

object TransactionValidator {
    fun validateTransaction(amount: String, jarId: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        // Amount Validation
        val amountValue = amount.toDoubleOrNull()
        if (amount.isBlank()) {
            errors["amount"] = "Amount is required"
        } else if (amountValue == null || amountValue <= 0) {
            errors["amount"] = "Amount must be greater than 0"
        }

        // Jar Validation
        if (jarId.isBlank()) {
            errors["jarId"] = "Please select a jar"
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
