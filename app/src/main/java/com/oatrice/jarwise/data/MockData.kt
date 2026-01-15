package com.oatrice.jarwise.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import com.oatrice.jarwise.model.Jar
import com.oatrice.jarwise.model.Transaction
import com.oatrice.jarwise.ui.theme.*

val MockJars = listOf(
    Jar(
        id = "1",
        name = "Necessities",
        current = 1250.0,
        goal = 2000.0,
        level = 5,
        icon = Icons.Rounded.Home,
        color = Blue400, // text-blue-400 used as icon tint mostly, but here passing as base color
        shadowColor = Blue500,
        barColor = Blue500
    ),
    Jar(
        id = "2",
        name = "Play",
        current = 350.0,
        goal = 500.0,
        level = 3,
        icon = Icons.Rounded.Gamepad,
        color = Pink400,
        shadowColor = Pink500,
        barColor = Pink500
    ),
    Jar(
        id = "3",
        name = "Education",
        current = 800.0,
        goal = 1500.0,
        level = 4,
        icon = Icons.Rounded.School, // Closest to GraduationCap
        color = Purple400,
        shadowColor = Purple500,
        barColor = Purple500
    ),
    Jar(
        id = "4",
        name = "Long Term",
        current = 5000.0,
        goal = 10000.0,
        level = 8,
        icon = Icons.Rounded.Flight, // Plane
        color = Green400,
        shadowColor = Green500,
        barColor = Green500
    ),
    Jar(
        id = "5",
        name = "Freedom",
        current = 2500.0,
        goal = 10000.0,
        level = 6,
        icon = Icons.Rounded.AttachMoney, // DollarSign
        color = Yellow400,
        shadowColor = Yellow500,
        barColor = Yellow500
    ),
    Jar(
        id = "6",
        name = "Give",
        current = 100.0,
        goal = 500.0,
        level = 1,
        icon = Icons.Rounded.Favorite, // Heart
        color = Red400,
        shadowColor = Red500,
        barColor = Red500
    )
)

val MockTransactions = listOf(
    Transaction(
        id = "1",
        merchant = "Spotify Premium",
        amount = 12.99,
        category = "Play",
        date = "Today, 2:30 PM",
        icon = Icons.Rounded.Bolt, // Zap
        color = Green500.copy(alpha = 0.1f), // bg-green-500/10
        iconTint = Green400 // text-green-400
        
    ),
    Transaction(
        id = "2",
        merchant = "Whole Foods Market",
        amount = 86.42,
        category = "Necessities",
        date = "Yesterday, 6:15 PM",
        icon = Icons.Rounded.ShoppingBag,
        color = Blue500.copy(alpha = 0.1f), // bg-blue-500/10
        iconTint = Blue400 // text-blue-400
    ),
    Transaction(
        id = "3",
        merchant = "Starbucks Coffee",
        amount = 6.50,
        category = "Play",
        date = "Yesterday, 8:00 AM",
        icon = Icons.Rounded.Coffee,
        color = Pink500.copy(alpha = 0.1f), // bg-pink-500/10
        iconTint = Pink400 // text-pink-400
    ),
    Transaction(
        id = "4",
        merchant = "Apple Store",
        amount = 999.00,
        category = "Necessities",
        date = "3 days ago",
        icon = Icons.Rounded.Smartphone,
        color = Gray500.copy(alpha = 0.1f), // bg-gray-500/10
        iconTint = Gray400 // text-gray-400
    )
)
