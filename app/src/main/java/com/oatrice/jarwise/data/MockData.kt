package com.oatrice.jarwise.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import com.oatrice.jarwise.model.Jar
import com.oatrice.jarwise.model.Transaction

val Blue500 = Color(0xFF3B82F6)
val Cyan400 = Color(0xFF22D3EE)
val Purple500 = Color(0xFFA855F7)
val Pink500 = Color(0xFFEC4899)
val Orange500 = Color(0xFFF97316)
val Emerald500 = Color(0xFF10B981)

val MockJars = listOf(
    Jar(
        id = "1",
        name = "New Macbook Pro",
        current = 2400.0,
        goal = 3000.0,
        level = 42,
        icon = Icons.Rounded.Laptop,
        color = Color(0xFF1E3A8A), // Dark Blue
        shadowColor = Blue500,
        barColor = Blue500
    ),
    Jar(
        id = "2",
        name = "Japan Trip",
        current = 1250.0,
        goal = 5000.0,
        level = 15,
        icon = Icons.Rounded.Flight,
        color = Color(0xFF4C1D95), // Dark Purple
        shadowColor = Purple500,
        barColor = Purple500
    ),
    Jar(
        id = "3",
        name = "Emergency Fund",
        current = 15000.0,
        goal = 20000.0,
        level = 88,
        icon = Icons.Rounded.Shield,
        color = Color(0xFF064E3B), // Dark Emerald
        shadowColor = Emerald500,
        barColor = Emerald500
    )
)

val MockTransactions = listOf(
    Transaction(
        id = "1",
        merchant = "Starbucks",
        category = "Coffee",
        date = "Today, 10:23 AM",
        amount = 5.40,
        icon = Icons.Rounded.Coffee,
        color = Color(0xF3F4F6) // Light Gray equivalent
    ),
    Transaction(
        id = "2",
        merchant = "Apple Services",
        category = "Subscription",
        date = "Yesterday, 2:00 PM",
        amount = 14.99,
        icon = Icons.Rounded.Add,
        color = Color(0xF3F4F6)
    ),
    Transaction(
        id = "3",
        merchant = "Uber Eats",
        category = "Food",
        date = "Yesterday, 8:45 PM",
        amount = 24.50,
        icon = Icons.Rounded.Fastfood,
        color = Color(0xF3F4F6)
    ),
    Transaction(
        id = "4",
        merchant = "Netflix",
        category = "Entertainment",
        date = "Oct 24, 2023",
        amount = 19.99,
        icon = Icons.Rounded.Movie,
        color = Color(0xF3F4F6)
    )
)
