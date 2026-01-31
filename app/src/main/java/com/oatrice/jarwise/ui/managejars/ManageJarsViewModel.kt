package com.oatrice.jarwise.ui.managejars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.JarConfig
import com.oatrice.jarwise.data.repository.JarConfigRepository
import com.oatrice.jarwise.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI representation of a jar for the Manage Jars screen
 */
data class EditableJar(
    val id: String,
    val name: String,
    val percentage: Int,
    val colorName: String,
    val iconName: String,
    val color: Color,
    val icon: ImageVector
)

class ManageJarsViewModel(
    private val repository: JarConfigRepository
) : ViewModel() {

    private val _jars = MutableStateFlow<List<EditableJar>>(emptyList())
    val jars: StateFlow<List<EditableJar>> = _jars.asStateFlow()

    private val _selectedJarId = MutableStateFlow<String?>(null)
    val selectedJarId: StateFlow<String?> = _selectedJarId.asStateFlow()

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    val totalPercentage: StateFlow<Int> = _jars.map { jars ->
        jars.sumOf { it.percentage }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isValid: StateFlow<Boolean> = totalPercentage.map { it == 100 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadJars()
    }

    private fun loadJars() {
        viewModelScope.launch {
            repository.initializeDefaultsIfEmpty()
            repository.getAllJarConfigsFlow().collect { configs ->
                _jars.value = configs.map { it.toEditableJar() }
            }
        }
    }

    fun selectJar(id: String?) {
        _selectedJarId.value = if (_selectedJarId.value == id) null else id
    }

    fun updateJar(id: String, name: String? = null, percentage: Int? = null, colorName: String? = null, iconName: String? = null) {
        _jars.update { jars ->
            jars.map { jar ->
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
        viewModelScope.launch {
            repository.resetToDefaults()
            _showResetDialog.value = false
            _selectedJarId.value = null
        }
    }

    fun save(onSuccess: () -> Unit) {
        if (!isValid.value) return
        viewModelScope.launch {
            val configs = _jars.value.map { jar ->
                JarConfig(
                    id = jar.id,
                    name = jar.name,
                    percentage = jar.percentage,
                    colorName = jar.colorName,
                    iconName = jar.iconName
                )
            }
            repository.saveAllJarConfigs(configs)
            onSuccess()
        }
    }

    private fun JarConfig.toEditableJar() = EditableJar(
        id = id,
        name = name,
        percentage = percentage,
        colorName = colorName,
        iconName = iconName,
        color = getColorFromName(colorName),
        icon = getIconFromName(iconName)
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
            else -> Icons.Rounded.Home
        }
    }

    class Factory(private val repository: JarConfigRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageJarsViewModel(repository) as T
        }
    }
}
