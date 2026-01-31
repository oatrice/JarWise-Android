package com.oatrice.jarwise.ui.managejars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.Allocation
import com.oatrice.jarwise.data.AllocationDao
import com.oatrice.jarwise.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI representation of a jar/category for the Manage Jars screen
 */
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
    val sortOrder: Int
)

class ManageJarsViewModel(
    private val allocationDao: AllocationDao
) : ViewModel() {

    private val _jars = MutableStateFlow<List<EditableJar>>(emptyList())
    // We sort/organize them in correct display order (tree flattened)
    val jars: StateFlow<List<EditableJar>> = _jars.asStateFlow()

    private val _selectedJarId = MutableStateFlow<Long?>(null)
    val selectedJarId: StateFlow<Long?> = _selectedJarId.asStateFlow()

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    private val _jarToDelete = MutableStateFlow<EditableJar?>(null)
    val jarToDelete: StateFlow<EditableJar?> = _jarToDelete.asStateFlow()

    // Only verify percentage sum for Top Level Jars (level = 0)
    val totalPercentage: StateFlow<Int> = _jars.map { list ->
        list.filter { it.level == 0 }.sumOf { it.percentage }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isValid: StateFlow<Boolean> = totalPercentage.map { it == 100 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // TODO: Ideally fetch from User Session
    private val currentUserId = "local_user"

    init {
        loadAllocations()
    }

    private fun loadAllocations() {
        viewModelScope.launch {
            allocationDao.getAllAllocations(currentUserId).collect { allocations ->
                // Sort by sorting tree structure (DFS) to ensure visual hierarchy (Parent -> Children)
                val allItems = allocations.map { it.toEditableJar() }
                
                // Group by parentId
                val groupedByParent = allItems.groupBy { it.parentId }
                
                // Recursive DFS traversal
                val flattened = mutableListOf<EditableJar>()
                
                fun traverse(parentId: Long?) {
                    // Get children for this parent, sort by sortOrder
                    val children = groupedByParent[parentId]?.sortedBy { it.sortOrder } ?: emptyList()
                    
                    children.forEach { child ->
                        flattened.add(child)
                        traverse(child.id) // Recursive call for this child's children
                    }
                }
                
                // Start with roots (parentId = null)
                traverse(null)
                
                _jars.value = flattened
            }
        }
    }

    fun selectJar(id: Long?) {
        _selectedJarId.value = if (_selectedJarId.value == id) null else id
    }

    fun updateJar(id: Long, name: String? = null, percentage: Int? = null, colorName: String? = null, iconName: String? = null) {
        _jars.update { list ->
            list.map { jar ->
                if (jar.id == id) {
                    val newColorName = colorName ?: jar.colorName
                    val newIconName = iconName ?: jar.iconName
                    jar.copy(
                        name = name ?: jar.name,
                        percentage = percentage ?: jar.percentage,
                        colorName = newColorName,
                        iconName = newIconName,
                        color = getColorFromName(newColorName),
                        icon = getIconFromName(newIconName)
                    )
                } else jar
            }
        }
    }

    fun showResetConfirmation() {
        _showResetDialog.value = true
    }

    fun hideResetConfirmation() {
        _showResetDialog.value = false
    }

    fun resetToDefaults() {
        loadAllocations() 
        _showResetDialog.value = false
        _selectedJarId.value = null
    }

    fun save(onSuccess: () -> Unit) {
        // if (!isValid.value) return // Allow saving even if not 100% (Warning only) as per plan

        viewModelScope.launch {
            _jars.value.forEach { editable ->
                // Map back to Allocation and Update
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
                    sortOrder = editable.sortOrder
                )
                allocationDao.update(allocation)
            }
            onSuccess()
        }
    }
    
    // Actions for hierarchy
    fun addJar() {
        // Create new top-level jar
        viewModelScope.launch {
            val newJar = Allocation(
                userId = currentUserId,
                name = "New Jar",
                level = 0,
                parentId = null,
                targetPercent = 0,
                icon = "home",
                color = "gray",
                sortOrder = _jars.value.size + 1
            )
            allocationDao.insert(newJar)
        }
    }
    
    fun addCategory(parentId: Long) {
        viewModelScope.launch {
            // Get parent to inherit color logic if needed, or just default.
            // For now, grey/default. 
            // In Web we inherited parent color. Let's try to find parent.
            val parent = _jars.value.find { it.id == parentId }
            val color = parent?.colorName ?: "gray"
            
            val newCategory = Allocation(
                userId = currentUserId,
                name = "New Category",
                level = 1, // Currently supporting only 1 level deep for categories as per Web
                parentId = parentId,
                targetPercent = null, // Categories don't have direct targetPercent in this model yet
                icon = "dollar", // Default icon
                color = color,
                sortOrder = _jars.value.size + 1 // Simple sort
            )
            allocationDao.insert(newCategory)
        }
    }
    
    fun deleteJar(id: Long) {
         // See confirmDelete() for implementation
    }

    fun showDeleteConfirmation(jar: EditableJar) {
        _jarToDelete.value = jar
    }

    fun cancelDelete() {
        _jarToDelete.value = null
    }

    fun confirmDelete() {
        val jar = _jarToDelete.value ?: return
        viewModelScope.launch {
             // Find and delete. Cascade will match DB.
             val item = _jars.value.find { it.id == jar.id }
             if (item != null) {
                 val allocation = Allocation(
                     id = item.id,
                     userId = item.userId,
                     name = item.name,
                     parentId = item.parentId,
                     level = item.level,
                     targetPercent = item.percentage,
                     icon = item.iconName,
                     color = item.colorName,
                     isSystemDefault = item.isSystemDefault
                 )
                 allocationDao.delete(allocation)
             }
             _jarToDelete.value = null
        }
    }

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
        sortOrder = sortOrder
    )

    companion object {
        val AVAILABLE_COLORS = listOf("blue", "green", "pink", "yellow", "purple", "red", "cyan", "orange")
        val AVAILABLE_ICONS = listOf("home", "dollar", "gamepad", "school", "flight", "heart", "work", "savings")

        fun getColorFromName(name: String): Color = when (name.lowercase()) {
            "blue" -> Blue400
            "green" -> Green400
            "pink" -> Pink400
            "yellow" -> Yellow400
            "purple" -> Purple400
            "red" -> Red400
            "cyan" -> Cyan400
            "orange" -> Orange400
            "gray" -> Color.Gray
            else -> Blue400
        }

        fun getIconFromName(name: String): ImageVector = when (name.lowercase()) {
            "home" -> Icons.Rounded.Home
            "dollar" -> Icons.Rounded.AttachMoney
            "gamepad" -> Icons.Rounded.Gamepad
            "school" -> Icons.Rounded.School
            "flight" -> Icons.Rounded.Flight
            "heart" -> Icons.Rounded.Favorite
            "work" -> Icons.Rounded.Work
            "savings" -> Icons.Rounded.Savings
            "attachmoney" -> Icons.Rounded.AttachMoney
            "piggybank" -> Icons.Rounded.Savings
            "bookopen" -> Icons.Rounded.School
            "smile" -> Icons.Rounded.Face // Mapping 'smile' to Face
            "gift" -> Icons.Rounded.CardGiftcard
            else -> Icons.Rounded.Home
        }
    }

    class Factory(private val allocationDao: AllocationDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageJarsViewModel(allocationDao) as T
        }
    }
}
