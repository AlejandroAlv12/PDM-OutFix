package com.pdm0126.outfix.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.screens.closet.parseColorHex

@Composable
fun CharacterWithClothes(
    top: GarmentResponse?,
    bottom: GarmentResponse?,
    shoes: GarmentResponse?,
    head: GarmentResponse?,
    accessories: List<GarmentResponse> = emptyList(),
    modifier: Modifier = Modifier
) {
    val defaultSkin = Color(0xFFEAC3AB)
    
    val topColor = remember(top?.colorHex) { top?.colorHex?.let { parseColorHex(it) } ?: defaultSkin }
    val hasBottom = bottom != null
    val bottomColor = remember(bottom?.colorHex) { bottom?.colorHex?.let { parseColorHex(it) } ?: defaultSkin }
    val shoesColor = remember(shoes?.colorHex) { shoes?.colorHex?.let { parseColorHex(it) } }
    val hatColor = remember(head?.colorHex) { head?.colorHex?.let { parseColorHex(it) } ?: defaultSkin }

    Canvas(modifier = modifier.fillMaxSize().clipToBounds()) {
        val u = size.height / 340f
        val cx = size.width / 2
        val cy = size.height / 2
        
        val headRadius = 26f * u
        val headCenter = Offset(cx, cy - 90f * u)
        
        val torsoTop = headCenter.y + headRadius + 8f * u
        val torsoWidth = 56f * u
        val torsoHeight = 75f * u
        val shoulderRadius = 18f * u
        
        val armWidth = 22f * u
        val armHeight = 85f * u
        val armRadius = armWidth / 2
        
        val upperWidth = torsoWidth + 2 * armWidth
        
        val legWidth = torsoWidth / 2
        val legHeight = 100f * u
        val legRadius = legWidth / 2
        
        drawCircle(
            color = defaultSkin,
            radius = headRadius,
            center = headCenter
        )
        
        val gafas = accessories.find { it.category.equals("Gafas", ignoreCase = true) }
        if (gafas != null) {
            val accColor = gafas.colorHex?.let { parseColorHex(it) } ?: Color.Black
            val glassWidth = 14f * u
            val glassHeight = 10f * u
            val glassY = headCenter.y - 4f * u
            drawRoundRect(
                color = accColor,
                topLeft = Offset(headCenter.x - glassWidth - 2f * u, glassY),
                size = Size(glassWidth, glassHeight),
                cornerRadius = CornerRadius(3f * u, 3f * u)
            )
            drawRoundRect(
                color = accColor,
                topLeft = Offset(headCenter.x + 2f * u, glassY),
                size = Size(glassWidth, glassHeight),
                cornerRadius = CornerRadius(3f * u, 3f * u)
            )
            drawRect(
                color = accColor,
                topLeft = Offset(headCenter.x - 2f * u, glassY + glassHeight / 2 - 1f * u),
                size = Size(4f * u, 2f * u)
            )
        }
        
        drawArc(
            color = hatColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius),
            size = Size(headRadius * 2, headRadius * 2)
        )
        
        val isGorra = head?.category?.equals("Gorra", ignoreCase = true) == true
        val isSombrero = head?.category?.equals("Sombrero", ignoreCase = true) == true
        
        if (isGorra || isSombrero) {
            drawRoundRect(
                color = hatColor,
                topLeft = Offset(headCenter.x, headCenter.y - 8f * u),
                size = Size(headRadius + 15f * u, 8f * u),
                cornerRadius = CornerRadius(4f * u, 4f * u)
            )
        }
        if (isSombrero) {
            drawRoundRect(
                color = hatColor,
                topLeft = Offset(headCenter.x - headRadius - 15f * u, headCenter.y - 8f * u),
                size = Size(headRadius + 15f * u, 8f * u),
                cornerRadius = CornerRadius(4f * u, 4f * u)
            )
        }
        
        val upperPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - upperWidth / 2 + shoulderRadius, torsoTop)
            lineTo(cx + upperWidth / 2 - shoulderRadius, torsoTop)
            
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx + upperWidth / 2 - 2 * shoulderRadius,
                    top = torsoTop,
                    right = cx + upperWidth / 2,
                    bottom = torsoTop + 2 * shoulderRadius
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            
            lineTo(cx + upperWidth / 2, torsoTop + armHeight - armRadius)
            
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx + torsoWidth / 2,
                    top = torsoTop + armHeight - 2 * armRadius,
                    right = cx + upperWidth / 2,
                    bottom = torsoTop + armHeight
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            
            lineTo(cx + torsoWidth / 2, torsoTop + torsoHeight)
            
            lineTo(cx - torsoWidth / 2, torsoTop + torsoHeight)
            
            lineTo(cx - torsoWidth / 2, torsoTop + armHeight - armRadius)
            
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx - upperWidth / 2,
                    top = torsoTop + armHeight - 2 * armRadius,
                    right = cx - torsoWidth / 2,
                    bottom = torsoTop + armHeight
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            
            lineTo(cx - upperWidth / 2, torsoTop + shoulderRadius)
            
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx - upperWidth / 2,
                    top = torsoTop,
                    right = cx - upperWidth / 2 + 2 * shoulderRadius,
                    bottom = torsoTop + 2 * shoulderRadius
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            
            close()
        }
        drawPath(path = upperPath, color = topColor)
        
        val isFormalTop = top?.style?.equals("Formal", ignoreCase = true) == true
        val isLongSleeveCategory = top?.category in listOf("Chaqueta", "Abrigo", "Suéter", "Camisa")
        val isLongSleeve = isFormalTop || isLongSleeveCategory
        val isDressTop = top?.category?.equals("Vestido", ignoreCase = true) == true
        
        val handHeight = if (isDressTop) armHeight else if (isLongSleeve) 16f * u else armHeight - 30f * u
        val handTop = torsoTop + armHeight - handHeight
        val topCorner = if (isDressTop) CornerRadius(shoulderRadius, shoulderRadius) else CornerRadius.Zero
        
        val leftHandPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - upperWidth / 2,
                    top = handTop,
                    right = cx - torsoWidth / 2,
                    bottom = handTop + handHeight,
                    topLeftCornerRadius = topCorner,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius(armRadius, armRadius),
                    bottomRightCornerRadius = CornerRadius(armRadius, armRadius)
                )
            )
        }
        drawPath(path = leftHandPath, color = defaultSkin)
        
        val rightHandPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx + torsoWidth / 2,
                    top = handTop,
                    right = cx + upperWidth / 2,
                    bottom = handTop + handHeight,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = topCorner,
                    bottomLeftCornerRadius = CornerRadius(armRadius, armRadius),
                    bottomRightCornerRadius = CornerRadius(armRadius, armRadius)
                )
            )
        }
        drawPath(path = rightHandPath, color = defaultSkin)
        
        val legTop = torsoTop + torsoHeight
        
        val isDress = top?.category?.equals("Vestido", ignoreCase = true) == true
        val isSkirt = bottom?.category?.equals("Falda", ignoreCase = true) == true
        val drawSkirt = isDress || (isSkirt && hasBottom)
        
        val legColor = if (drawSkirt) defaultSkin else bottomColor
        
        val leftLegPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - legWidth,
                    top = legTop,
                    right = cx + 1f,
                    bottom = legTop + legHeight,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                    bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                )
            )
        }
        drawPath(path = leftLegPath, color = legColor)
        
        val rightLegPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - 1f,
                    top = legTop,
                    right = cx + legWidth,
                    bottom = legTop + legHeight,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                    bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                )
            )
        }
        drawPath(path = rightLegPath, color = legColor)
        
        val isShort = bottom?.category?.equals("Short", ignoreCase = true) == true
        if (isShort && !drawSkirt) {
            val legSkinHeight = legHeight - 35f * u
            val legSkinTop = legTop + legHeight - legSkinHeight
            
            val leftLegSkinPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx - legWidth,
                        top = legSkinTop,
                        right = cx + 1f,
                        bottom = legSkinTop + legSkinHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                        bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                    )
                )
            }
            drawPath(path = leftLegSkinPath, color = defaultSkin)
            
            val rightLegSkinPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx - 1f,
                        top = legSkinTop,
                        right = cx + legWidth,
                        bottom = legSkinTop + legSkinHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                        bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                    )
                )
            }
            drawPath(path = rightLegSkinPath, color = defaultSkin)
        }
        
        if (!hasBottom && !isDress) {
            val briefPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - legWidth, legTop - 10f * u)
                lineTo(cx + legWidth, legTop - 10f * u)
                lineTo(cx + legWidth, legTop + 3f * u)
                lineTo(cx + 8f * u, legTop + 10f * u)
                quadraticTo(
                    cx, legTop + 18f * u,
                    cx - 8f * u, legTop + 10f * u
                )
                lineTo(cx - legWidth, legTop + 3f * u)
                close()
            }
            drawPath(path = briefPath, color = Color.White)
        }
        
        if (drawSkirt) {
            val skirtPath = androidx.compose.ui.graphics.Path().apply {
                val topWidth = torsoWidth / 2
                val bottomWidth = topWidth + 15f * u
                val skirtLength = 55f * u
                val cr = 8f * u
                val skirtTop = legTop - 1f
                
                moveTo(cx - topWidth, skirtTop)
                lineTo(cx + topWidth, skirtTop)
                
                lineTo(cx + bottomWidth - 2.5f * u, legTop + skirtLength - cr)
                
                quadraticTo(
                    cx + bottomWidth + 1f * u, legTop + skirtLength,
                    cx + bottomWidth - cr, legTop + skirtLength
                )
                
                lineTo(cx - bottomWidth + cr, legTop + skirtLength)
                
                quadraticTo(
                    cx - bottomWidth - 1f * u, legTop + skirtLength,
                    cx - bottomWidth + 2.5f * u, legTop + skirtLength - cr
                )
                
                close()
            }
            val skirtColor = if (isDress) topColor else bottomColor
            drawPath(path = skirtPath, color = skirtColor)
        }
        
        if (shoesColor != null) {
            val isBoots = shoes?.category?.equals("Botas", ignoreCase = true) == true
            val shoeHeight = if (isBoots) 40f * u else 25f * u
            val shoeTop = legTop + legHeight - shoeHeight
            
            val leftShoePath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx - legWidth,
                        top = shoeTop,
                        right = cx + 1f,
                        bottom = shoeTop + shoeHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                        bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                    )
                )
            }
            drawPath(path = leftShoePath, color = shoesColor)
            
            val rightShoePath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx - 1f,
                        top = shoeTop,
                        right = cx + legWidth,
                        bottom = shoeTop + shoeHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                        bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                    )
                )
            }
            drawPath(path = rightShoePath, color = shoesColor)
        }
        
        val zOrder = mapOf(
            "joyería" to 1,
            "corbata" to 2,
            "bufanda" to 3
        )
        val sortedAccessories = accessories.sortedBy { zOrder[it.category?.lowercase()] ?: 10 }
        
        sortedAccessories.forEach { accessory ->
            val accCategory = accessory.category
            val accColor = accessory.colorHex?.let { parseColorHex(it) } ?: Color.Black
            
            if (accCategory?.equals("Cinturón", ignoreCase = true) == true) {
                drawRect(
                    color = accColor,
                    topLeft = Offset(cx - torsoWidth / 2, legTop - 6f * u),
                    size = Size(torsoWidth, 6f * u)
                )
            } else if (accCategory?.equals("Corbata", ignoreCase = true) == true) {
                val tiePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - 4f * u, torsoTop)
                    lineTo(cx + 4f * u, torsoTop)
                    lineTo(cx + 3f * u, torsoTop + 8f * u)
                    lineTo(cx - 3f * u, torsoTop + 8f * u)
                    close()
                    moveTo(cx - 3f * u, torsoTop + 8f * u)
                    lineTo(cx + 3f * u, torsoTop + 8f * u)
                    lineTo(cx + 6f * u, torsoTop + 43f * u)
                    lineTo(cx, torsoTop + 53f * u)
                    lineTo(cx - 6f * u, torsoTop + 43f * u)
                    close()
                }
                drawPath(path = tiePath, color = accColor)
            } else if (accCategory?.equals("Bufanda", ignoreCase = true) == true) {
                drawRoundRect(
                    color = accColor,
                    topLeft = Offset(cx - 16f * u, torsoTop - 6f * u),
                    size = Size(32f * u, 16f * u),
                    cornerRadius = CornerRadius(8f * u, 8f * u)
                )
                drawRoundRect(
                    color = accColor,
                    topLeft = Offset(cx + 2f * u, torsoTop + 5f * u),
                    size = Size(10f * u, 35f * u),
                    cornerRadius = CornerRadius(4f * u, 4f * u)
                )
            } else if (accCategory?.equals("Mochila", ignoreCase = true) == true || accCategory?.equals("Bolso", ignoreCase = true) == true) {
                val bagWidth = 30f * u
                val bagHeight = 50f * u
                val bagTop = legTop + legHeight - bagHeight
                val bagLeft = cx + legWidth + 15f * u
                
                drawRoundRect(
                    color = accColor,
                    topLeft = Offset(bagLeft, bagTop),
                    size = Size(bagWidth, bagHeight),
                    cornerRadius = CornerRadius(armRadius, armRadius)
                )
            } else if (accCategory?.equals("Reloj", ignoreCase = true) == true) {
                val wristY = torsoTop + armHeight - 20f * u
                drawRect(
                    color = accColor,
                    topLeft = Offset(cx + torsoWidth / 2, wristY),
                    size = Size(armWidth, 6f * u)
                )
            } else if (accCategory?.equals("Joyería", ignoreCase = true) == true) {
                drawArc(
                    color = accColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 10f * u, torsoTop - 8f * u),
                    size = Size(20f * u, 16f * u),
                    style = Stroke(width = 2f * u)
                )
                drawCircle(
                    color = accColor,
                    radius = 3.5f * u,
                    center = Offset(cx, torsoTop + 8f * u)
                )
            }
        }
    }
}
