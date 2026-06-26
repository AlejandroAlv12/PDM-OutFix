package com.pdm0126.outfix.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.layer.drawLayer

const val ContinuousProgressiveBlurShader = """
    uniform shader content;
    uniform float2 size;
    uniform float maxBlur;
    uniform float fadeStartFraction;
    uniform float fadeEndFraction;

    float hash12(float2 p) {
        float3 p3  = fract(float3(p.xyx) * .1031);
        p3 += dot(p3, p3.yzx + 33.33);
        return fract((p3.x + p3.y) * p3.z);
    }

    half4 main(float2 fragCoord) {
        float yFrag = fragCoord.y / size.y;
        float progress = clamp((fadeEndFraction - yFrag) / (fadeEndFraction - fadeStartFraction), 0.0, 1.0);
        float radius = maxBlur * progress;
        
        if (radius < 0.5) {
            return content.eval(fragCoord);
        }
        
        half4 sum = half4(0.0);
        float totalWeight = 0.0;
        
        // OPTIMIZACIONES DE RENDIMIENTO:
        // 1. Reducido a 60 muestras (combinado con dithering es visualmente idéntico a 80)
        // 2. Pre-cálculo de división
        // 3. Aproximación cuadrática rápida en lugar de la costosa función exp()
        const int SAMPLES = 60;
        const float GOLDEN_ANGLE = 2.39996323;
        float startAngle = hash12(fragCoord) * 6.2831853;
        const float INV_SAMPLES = 1.0 / float(SAMPLES);
        
        for (int i = 0; i < SAMPLES; i++) {
            float t = float(i) * INV_SAMPLES;
            float r = radius * sqrt(t); 
            float theta = startAngle + float(i) * GOLDEN_ANGLE;
            float2 offset = float2(cos(theta), sin(theta)) * r;
            float2 samplePos = fragCoord + offset;
            
            // Candado estricto en los bordes (0.5 a size-0.5) para que no lea negro transparente (fuera de los límites) en los lados
            samplePos.x = clamp(samplePos.x, 0.5, size.x - 0.5);
            samplePos.y = clamp(samplePos.y, 0.5, size.y - 0.5);
            
            // Falloff ultra rápido matemáticamente equivalente al centro de la campana de Gauss
            float weight = 1.0 - (t * t); 
            
            sum += content.eval(samplePos) * weight;
            totalWeight += weight;
        }
        
        return sum / totalWeight;
    }
"""

@Composable
fun ProgressiveBlurLayer(
    modifier: Modifier = Modifier,
    contentLayer: GraphicsLayer,
    maxBlur: Float = 60f,
    fadeStartFraction: Float = 0.4f,
    fadeEndFraction: Float = 1.0f
) {
    if (android.os.Build.VERSION.SDK_INT >= 33) {
        var shaderSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
        val shaderEffect = remember(shaderSize, maxBlur, fadeStartFraction, fadeEndFraction) {
            if (shaderSize.width > 0f && shaderSize.height > 0f) {
                val shader = android.graphics.RuntimeShader(ContinuousProgressiveBlurShader).apply {
                    setFloatUniform("size", shaderSize.width, shaderSize.height)
                    setFloatUniform("maxBlur", maxBlur)
                    setFloatUniform("fadeStartFraction", fadeStartFraction)
                    setFloatUniform("fadeEndFraction", fadeEndFraction)
                }
                android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
            } else null
        }

        Box(
            modifier = modifier.onSizeChanged {
                shaderSize = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat())
            }
        ) {
            if (shaderEffect != null) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { renderEffect = shaderEffect }
                ) {
                    drawLayer(contentLayer)
                }
            }
        }
    } else {
        // Fallback for API < 33
        val layers = 3
        val blurStep = maxBlur / layers
        
        Box(modifier = modifier) {
            for (i in 0 until layers) {
                val currentBlur = blurStep * (i + 1)
                val fraction = i.toFloat() / (layers - 1).coerceAtLeast(1)
                val startF = fadeStartFraction + (fadeEndFraction - fadeStartFraction) * fraction
                
                var size by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
                val renderEffect = remember(currentBlur) {
                    if (android.os.Build.VERSION.SDK_INT >= 31) {
                        android.graphics.RenderEffect.createBlurEffect(
                            currentBlur, currentBlur, android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    } else null
                }
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
                        .graphicsLayer { 
                            if (renderEffect != null) {
                                this.renderEffect = renderEffect 
                            }
                        }
                ) {
                    if (size.width > 0f && size.height > 0f) {
                        val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            0f to androidx.compose.ui.graphics.Color.Black,
                            startF to androidx.compose.ui.graphics.Color.Black,
                            fadeEndFraction to androidx.compose.ui.graphics.Color.Transparent,
                            1f to androidx.compose.ui.graphics.Color.Transparent
                        )
                        drawContext.canvas.saveLayer(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height), androidx.compose.ui.graphics.Paint())
                        drawLayer(contentLayer)
                        drawRect(brush = brush, blendMode = androidx.compose.ui.graphics.BlendMode.DstIn)
                        drawContext.canvas.restore()
                    }
                }
            }
        }
    }
}
