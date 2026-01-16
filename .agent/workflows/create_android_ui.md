---
description: Create or update Android Jetpack Compose UI components with required standards
---

When creating or modifying Jetpack Compose UI components in Android, follow this workflow:

1.  **Understand Requirements & Mock Data**:
    - Review the data structure available in `GeneratedMockData.kt` or `MockData.kt`.
    - Plan the UI structure based on user requirements.

2.  **Create Composable Function**:
    - Build the UI using Material3 components.
    - Ensure correct Themings (use `JarWiseTheme` or defined colors like `Gray950`, `Blue400`).
    - Use `Modifier` properly for layout and spacing.

3.  **Create Preview (MANDATORY)**:
    - You **MUST** create a `@Preview` composable for every UI component.
    - **Configuration:**
      - Set `showBackground = true`.
      - Set `backgroundColor` to match the theme (e.g., `0xFF0A0A0A` for Dark Mode).
    - **Content:**
      - Provide real-looking mock data (using `GeneratedMockData` is preferred).
    - Example:
      ```kotlin
      @Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
      @Composable
      fun MyComponentPreview() {
          JarWiseTheme {
              MyComponent(data = GeneratedMockData.sampleItem)
          }
      }
      ```

4.  **Verify & Test**:
    - Ensure code compiles.
    - Run UI tests if applicable.

5.  **Documentation**:
    - Add KDoc comments to the main composable explaining its purpose and parameters.
