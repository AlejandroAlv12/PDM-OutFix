#!/bin/bash
sed -i '' 's/import androidx.compose.foundation.clickable/import androidx.compose.foundation.clickable\
import androidx.compose.foundation.combinedClickable\
import androidx.compose.foundation.ExperimentalFoundationApi\
import androidx.compose.ui.hapticfeedback.HapticFeedbackType\
import androidx.compose.ui.platform.LocalHapticFeedback\
import androidx.compose.ui.layout.onGloballyPositioned\
import androidx.compose.ui.layout.boundsInRoot\
import androidx.compose.ui.graphics.graphicsLayer\
import com.pdm0126.outfix.screens.closet.ClosetOverlayState\
import com.pdm0126.outfix.data.model.DayInfo/g' app/src/main/java/com/pdm0126/outfix/screens/home/HomeScreen.kt
