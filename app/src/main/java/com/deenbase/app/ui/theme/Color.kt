package com.deenbase.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Light Mode Palette (60/30/10) ────────────────────────────────────────────
val LightPrimary   = Color(0xFFCCD4E0)  // 60% — main surfaces
val LightSecondary = Color(0xFF14AAF5)  // 30% — interactive / accent
val LightAccent    = Color(0xFF141E29)  // 10% — text / dark elements

// ── Dark Mode Palette (60/30/10) ─────────────────────────────────────────────
val DarkPrimary    = Color(0xFF03141C)  // 60% — main surfaces
val DarkSecondary  = Color(0xFF1B9FE0)  // 30% — interactive / accent
val DarkAccent     = Color(0xFFCED7DB)  // 10% — text / light elements

// ── Derived surface shades ────────────────────────────────────────────────────
// Light
val LightBackground             = Color(0xFFEEF2F6)  // slightly lighter than primary
val LightSurface                = LightPrimary
val LightSurfaceContainer       = Color(0xFFC2CCDA)
val LightSurfaceContainerLow    = Color(0xFFD8DFE9)
val LightSurfaceContainerHigh   = Color(0xFFB8C2D2)
val LightSurfaceContainerHighest= Color(0xFFABB7C8)
val LightOutline                = Color(0xFF8A97A8)
val LightOutlineVariant         = Color(0xFFCDD5E0)

// Dark
val DarkBackground              = Color(0xFF010C12)  // deeper than primary
val DarkSurface                 = DarkPrimary
val DarkSurfaceContainer        = Color(0xFF071E2B)
val DarkSurfaceContainerLow     = Color(0xFF051622)
val DarkSurfaceContainerHigh    = Color(0xFF0A2535)
val DarkSurfaceContainerHighest = Color(0xFF0F2E40)
val DarkOutline                 = Color(0xFF1B4A66)
val DarkOutlineVariant          = Color(0xFF0A2535)
