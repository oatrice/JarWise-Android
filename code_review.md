# Luma Code Review Report

**Date:** 2026-01-31 21:33:23
**Files Reviewed:** ['app/src/main/java/com/oatrice/jarwise/data/AllocationDao.kt', 'app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsViewModel.kt', 'app/src/main/java/com/oatrice/jarwise/MainActivity.kt', 'draft_pr_prompt.md', 'app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt', 'app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsScreen.kt', 'app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt', 'app/src/main/java/com/oatrice/jarwise/data/Allocation.kt', 'app/src/test/java/com/oatrice/jarwise/data/AllocationDaoTest.kt', 'app/src/test/java/com/oatrice/jarwise/data/MigrationTest.kt', '.luma_state.json', 'app/schemas/com.oatrice.jarwise.data.AppDatabase/5.json']

## 📝 Reviewer Feedback

There is a critical data loss bug in the `ManageJarsViewModel` that will reset the `sortOrder` of all allocations every time the user saves their changes.

### **Issue: `sortOrder` is not preserved on save**

In `ManageJarsViewModel.kt`, the `save()` function reconstructs `Allocation` objects from `EditableJar` objects before updating them in the database. However, the `EditableJar` data class does not contain the `sortOrder` field.

When the `Allocation` object is created inside the `save` function, the `sortOrder` field is omitted. Because the `Allocation` data class has a default value (`sortOrder: Int = 0`), every item updated will have its `sortOrder` reset to `0`. This will break the user-defined ordering of jars and categories.

**File:** `app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsViewModel.kt`

**Problematic Code in `save()`:**
```kotlin
// ...
val allocation = Allocation(
    id = editable.id,
    userId = editable.userId,
    name = editable.name,
    parentId = editable.parentId,
    level = editable.level,
    targetPercent = if (editable.level == 0) editable.percentage else null,
    icon = editable.iconName,
    color = editable.colorName,
    isSystemDefault = editable.isSystemDefault,
    isActive = true
    // sortOrder is missing here!
)
allocationDao.update(allocation)
// ...
```

### **Fix**

To fix this, you need to add `sortOrder` to the `EditableJar` data class and ensure it's mapped correctly throughout the ViewModel.

1.  **Update `EditableJar` data class:** Add the `sortOrder` property.

    **File:** `app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsViewModel.kt`
    ```kotlin
    data class EditableJar(
        val id: Long,
        val userId: String,
        val name: String,
        val percentage: Int, // Represents targetPercent
        val colorName: String,
        val iconName: String,
        val color: Color,
        val icon: ImageVector,
        val parentId: Long?,
        val level: Int,
        val isSystemDefault: Boolean,
        val sortOrder: Int // <-- ADD THIS
    )
    ```

2.  **Update `toEditableJar()` extension function:** Map the `sortOrder` from the `Allocation` entity.

    **File:** `app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsViewModel.kt`
    ```kotlin
    private fun Allocation.toEditableJar() = EditableJar(
        id = id,
        userId = userId,
        name = name,
        percentage = targetPercent ?: 0,
        colorName = color,
        iconName = icon,
        color = getColorFromName(color),
        icon = getIconFromName(icon),
        parentId = parentId,
        level = level,
        isSystemDefault = isSystemDefault,
        sortOrder = sortOrder // <-- ADD THIS
    )
    ```

3.  **Update the `save()` function:** Include `sortOrder` when reconstructing the `Allocation` object.

    **File:** `app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsViewModel.kt`
    ```kotlin
    fun save(onSuccess: () -> Unit) {
        // ...
        viewModelScope.launch {
            _jars.value.forEach { editable ->
                val allocation = Allocation(
                    id = editable.id,
                    userId = editable.userId,
                    name = editable.name,
                    parentId = editable.parentId,
                    level = editable.level,
                    targetPercent = if (editable.level == 0) editable.percentage else null,
                    icon = editable.iconName,
                    color = editable.colorName,
                    isSystemDefault = editable.isSystemDefault,
                    isActive = true,
                    sortOrder = editable.sortOrder // <-- ADD THIS
                )
                allocationDao.update(allocation)
            }
            onSuccess()
        }
    }
    ```

## 🧪 Test Suggestions

Here are 3 critical, edge-case test cases that should be added or verified based on the code changes:

*   **Orphaned Category:** Create a category (an `Allocation` with a `parentId`) in the database, but ensure its `parentId` points to a non-existent jar ID. The test should verify that the application does not crash and that this orphaned category is correctly excluded from the UI list, as the new `organizeJarsAndCategories` logic would not be able to find its parent in the `topLevelJars` list.

*   **Demoting the Last Jar:** With only one top-level jar remaining in the database, attempt to demote it to a category. The test should verify that the UI either prevents this action (as a category must have a parent) or, if the action is allowed, that the resulting UI correctly displays an empty list, since there are no longer any top-level jars to render.

*   **Circular Dependency (Self-Parenting):** Attempt to demote a jar and select itself as the new parent. The UI logic should prevent this action. A test should verify that the `demoteToCategory` function is not called with an `id` and `newParentId` that are identical, as this would cause the jar to become an orphan and disappear from the view.

