package com.oatrice.jarwise.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

/**
 * State holder for scroll visibility
 * Controls visibility of UI elements (TopAppBar, BottomNav) based on scroll direction
 */
class ScrollVisibilityState(
    private val lazyListState: LazyListState
) {
    var isVisible by mutableStateOf(true)
        private set
    
    private var lastScrollOffset = 0
    private var lastIndex = 0
    
    /**
     * Updates visibility based on scroll direction
     * - Scrolling down (positive delta) -> hide (isVisible = false)
     * - Scrolling up (negative delta) -> show (isVisible = true)
     * - Threshold of 10dp to avoid flicker
     */
    fun updateScrollVisibility() {
        val currentIndex = lazyListState.firstVisibleItemIndex
        val currentOffset = lazyListState.firstVisibleItemScrollOffset
        
        // Calculate scroll delta
        val delta = if (currentIndex == lastIndex) {
            currentOffset - lastScrollOffset
        } else if (currentIndex > lastIndex) {
            // Scrolled down to next item
            100 // Positive indicates scroll down
        } else {
            // Scrolled up to previous item
            -100 // Negative indicates scroll up
        }
        
        // Apply visibility change with threshold
        if (delta > 10) {
            isVisible = false // Scrolling down -> hide
        } else if (delta < -10) {
            isVisible = true // Scrolling up -> show
        }
        
        // At top of list, always show
        if (currentIndex == 0 && currentOffset == 0) {
            isVisible = true
        }
        
        lastIndex = currentIndex
        lastScrollOffset = currentOffset
    }
}

/**
 * Remember scroll visibility state for a LazyListState
 */
@Composable
fun rememberScrollVisibilityState(lazyListState: LazyListState): ScrollVisibilityState {
    return remember(lazyListState) { 
        ScrollVisibilityState(lazyListState) 
    }
}
