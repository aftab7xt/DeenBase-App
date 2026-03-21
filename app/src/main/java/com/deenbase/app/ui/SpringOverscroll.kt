package com.deenbase.app.ui

import androidx.compose.ui.Modifier

// Spring overscroll removed — returns the modifier unchanged.
// All call sites remain valid without any other changes.
fun Modifier.springOverscroll(): Modifier = this
