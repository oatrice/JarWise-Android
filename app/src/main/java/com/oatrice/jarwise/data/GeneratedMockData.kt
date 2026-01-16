package com.oatrice.jarwise.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.oatrice.jarwise.model.Jar
import com.oatrice.jarwise.model.Transaction
import com.oatrice.jarwise.ui.theme.*

// WARNING: This file is auto-generated. Do not edit directly.
// Generated from: shared-spec/data/mockData.json

object GeneratedMockData {
    val jars = listOf(
        Jar(
            id = "1",
            name = "Necessities",
            current = 2450.5,
            goal = 4000.0,
            level = 4,
            icon = Icons.Rounded.Home,
            color = Blue400,
            shadowColor = Blue500,
            barColor = Blue500
        ),
        Jar(
            id = "2",
            name = "Financial Freedom",
            current = 12000.0,
            goal = 100000.0,
            level = 12,
            icon = Icons.Rounded.AttachMoney,
            color = Green400,
            shadowColor = Green500,
            barColor = Green500
        ),
        Jar(
            id = "3",
            name = "Play",
            current = 850.0,
            goal = 1000.0,
            level = 2,
            icon = Icons.Rounded.Gamepad,
            color = Pink400,
            shadowColor = Pink500,
            barColor = Pink500
        ),
        Jar(
            id = "4",
            name = "Education",
            current = 150.0,
            goal = 500.0,
            level = 1,
            icon = Icons.Rounded.School,
            color = Yellow400,
            shadowColor = Yellow500,
            barColor = Yellow500
        ),
        Jar(
            id = "5",
            name = "Long-term Savings",
            current = 3200.0,
            goal = 5000.0,
            level = 5,
            icon = Icons.Rounded.Flight,
            color = Purple400,
            shadowColor = Purple500,
            barColor = Purple500
        ),
        Jar(
            id = "6",
            name = "Give",
            current = 120.0,
            goal = 200.0,
            level = 1,
            icon = Icons.Rounded.Favorite,
            color = Red400,
            shadowColor = Red500,
            barColor = Red500
        )
    )

    val transactions = listOf(
        Transaction(
            id = "1",
            merchant = "Spotify Premium",
            amount = -12.99,
            category = "Play",
            date = "Today, 10:43 AM",
            icon = Icons.Rounded.Headphones,
            color = Green500.copy(alpha = 0.1f),
            iconTint = Green400
        ),
        Transaction(
            id = "2",
            merchant = "Whole Foods Market",
            amount = -142.5,
            category = "Necessities",
            date = "Yesterday, 6:30 PM",
            icon = Icons.Rounded.ShoppingBag,
            color = Blue500.copy(alpha = 0.1f),
            iconTint = Blue400
        ),
        Transaction(
            id = "3",
            merchant = "Udemy Course",
            amount = -24.99,
            category = "Education",
            date = "Dec 28, 2025",
            icon = Icons.Rounded.School,
            color = Yellow500.copy(alpha = 0.1f),
            iconTint = Yellow400
        )
    )
}