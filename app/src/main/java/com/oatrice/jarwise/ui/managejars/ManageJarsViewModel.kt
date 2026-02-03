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
    val sortOrder: Int = 0
)

class ManageJarsViewModel(
    private val allocationDao: AllocationDao,
    private val backupManager: com.oatrice.jarwise.data.backup.BackupManager
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
    
    // Manage the loading job to clear previous subscriptions on reload/revert
    private var loadJob: kotlinx.coroutines.Job? = null
    
    // Track pending deletions (Real IDs only)
    private val _pendingDeletions = mutableSetOf<Long>()
    private var nextTempId = -1L

    init {
        loadAllocations()
    }

    fun setBackupPaused(paused: Boolean) {
        backupManager.setAutoBackupPaused(paused)
    }

    private fun loadAllocations() {
        _pendingDeletions.clear()
        nextTempId = -1L
        
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
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



    fun revertUnsavedChanges() {
        _pendingDeletions.clear()
        nextTempId = -1L
        loadAllocations() 
        _showResetDialog.value = false
        _selectedJarId.value = null
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 1. Process Deletions
            _pendingDeletions.forEach { id ->
                val allocation = Allocation(id = id, userId = currentUserId, name = "", level = 0, isSystemDefault = false) // Minimal obj for delete
                allocationDao.delete(allocation)
            }
            _pendingDeletions.clear()

            // 2. Process Inserts and Updates
            // We need to handle parent dependencies for new items.
            // Map keys are TempIDs, values are RealIDs
            val tempIdMap = mutableMapOf<Long, Long>()

            // Separate into New vs Existing
            val (newItems, existingItems) = _jars.value.partition { it.id < 0 }
            
            // Sort new items by level to ensure parents created before children
            val sortedNewItems = newItems.sortedBy { it.level }

            sortedNewItems.forEach { editable ->
                // Resolve Parent ID
                val finalParentId = if (editable.parentId != null && editable.parentId < 0) {
                    tempIdMap[editable.parentId] ?: editable.parentId // Should be in map if sorted correctly
                } else {
                    editable.parentId
                }

                val allocation = Allocation(
                    id = 0, // Auto-generate
                    userId = editable.userId,
                    name = editable.name,
                    parentId = finalParentId,
                    level = editable.level,
                    targetPercent = if (editable.level == 0) editable.percentage else null,
                    icon = editable.iconName,
                    color = editable.colorName,
                    isSystemDefault = editable.isSystemDefault,
                    isActive = true,
                    sortOrder = editable.sortOrder
                )
                val newId = allocationDao.insert(allocation)
                tempIdMap[editable.id] = newId
            }

            // Update Existing items
            existingItems.forEach { editable ->
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

             // Reload to get fresh state from DB (IDs updated etc)
            loadAllocations()
            onSuccess()
        }
    }
    
    // Actions for hierarchy
    fun addJar() {
        val newId = nextTempId--
        val newJar = EditableJar(
            id = newId,
            userId = currentUserId,
            name = "New Jar",
            level = 0,
            parentId = null,
            percentage = 0,
            colorName = "gray",
            iconName = "home",
            color = Color.Gray,
            icon = Icons.Rounded.Home,
            isSystemDefault = false,
            sortOrder = _jars.value.size + 1
        )
         _jars.update { it + newJar }
    }
    
    fun addCategory(parentId: Long) {
        val parent = _jars.value.find { it.id == parentId } ?: return
        val colorName = parent.colorName 
        val newId = nextTempId--
        
        val newCategory = EditableJar(
            id = newId,
            userId = currentUserId,
            name = "New Category",
            level = 1,
            parentId = parentId,
            percentage = 0, // Categories don't have direct targetPercent in this model yet
            colorName = colorName,
            iconName = "dollar",
            color = getColorFromName(colorName),
            icon = Icons.Rounded.AttachMoney,
            isSystemDefault = false,
            sortOrder = _jars.value.size + 1
        )
         _jars.update { it + newCategory }
    }
    


    fun showDeleteConfirmation(jar: EditableJar) {
        _jarToDelete.value = jar
    }

    fun cancelDelete() {
        _jarToDelete.value = null
    }

    fun confirmDelete() {
        val jar = _jarToDelete.value ?: return
        
        // Remove from memory
        // Verify cascade deletion in memory for UI mostly
        val idsToDelete = mutableSetOf<Long>()
        idsToDelete.add(jar.id)
        
        // If parent, find children in memory and delete them too
        if (jar.level == 0) {
            _jars.value.filter { it.parentId == jar.id }.forEach { idsToDelete.add(it.id) }
        }
        
        _jars.update { list ->
            list.filterNot { idsToDelete.contains(it.id) }
        }
        
        // Mark for DB deletion if it's a real ID
        if (jar.id > 0) {
            _pendingDeletions.add(jar.id)
        }
        
        _jarToDelete.value = null
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


}
