package com.pdm0126.outfix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.data.GARMENT_STYLES

@Composable
fun StyleSlider(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stylesLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val currentWidthDp = maxWidth
        val currentWidthPx = constraints.maxWidth.toFloat()
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    stylesLayer.record {
                        this@drawWithContent.drawContent()
                    }
                }
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(GARMENT_STYLES) { style ->
                val isSelected = style == selectedStyle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Color.Black else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color(0xFFBDBDBD),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onStyleSelected(style) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = style,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        HorizontalEdgesProgressiveBlurLayer(
            modifier = Modifier.matchParentSize(),
            contentLayer = stylesLayer,
            maxBlur = 20f,
            edgeWidthFraction = if (currentWidthPx > 0) with(androidx.compose.ui.platform.LocalDensity.current) { 24.dp.toPx() } / currentWidthPx else 0.1f
        )
    }
}
