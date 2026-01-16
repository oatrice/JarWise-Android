---
name: create-screen
description: Create a new Jetpack Compose screen with Preview, Navigation, and ViewModel stub
---

# Create Screen Skill

This skill scaffolds a new Compose screen following JarWise Android standards.

## Usage

When user says:
- "create screen [name]"
- "สร้างหน้า [name]"
- "new screen for [feature]"

## Workflow

### Step 1: Gather Requirements
1. **Screen Name**: PascalCase (e.g., `ManageJars`, `TransactionDetail`)
2. **Package**: Where to place (default: `com.oatrice.jarwise.ui`)
3. **Has Navigation**: Does it need nav params?

### Step 2: Create Screen File
Create `app/src/main/java/com/oatrice/jarwise/ui/<ScreenName>Screen.kt`:

```kotlin
package com.oatrice.jarwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oatrice.jarwise.ui.theme.JarWiseTheme

@Composable
fun <ScreenName>Screen(
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "<Screen Title>",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        Text(
            text = "TODO: Implement screen content",
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun <ScreenName>ScreenPreview() {
    JarWiseTheme {
        <ScreenName>Screen()
    }
}
```

### Step 3: Add Navigation (if needed)
Update `MainActivity.kt` or nav graph:

```kotlin
composable("screenName") {
    <ScreenName>Screen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### Step 4: Verify Build
```bash
./build_android.sh
```

### Step 5: Report Summary
```
✅ Screen Created Successfully!

Files Created:
- app/src/main/java/com/oatrice/jarwise/ui/<ScreenName>Screen.kt

Features:
- Dark theme background (0xFF0A0A0A)
- Back navigation header
- Preview with JarWiseTheme

Next Steps:
1. Implement screen content
2. Add to navigation graph
3. Test in emulator
```

## Standards (from create_android_ui workflow)
- ✅ Every screen MUST have @Preview
- ✅ Use `showBackground = true, backgroundColor = 0xFF0A0A0A`
- ✅ Use `JarWiseTheme` in Preview
- ✅ Use MockData/GeneratedMockData for preview content
