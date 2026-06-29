package com.pdm0126.outfix.screens.scan

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.pdm0126.outfix.ui.bouncyClickable
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

fun getLatestGalleryImageUri(context: Context): Uri? {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    try {
        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val id = cursor.getLong(idColumn)
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
    } catch (e: Exception) {
        Log.e("ScanGarmentScreen", "Failed to fetch gallery image", e)
    }
    return null
}

data class TrackingData(
    val boundingBox: android.graphics.Rect,
    val sourceWidth: Int,
    val sourceHeight: Int
)

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class ObjectTrackingAnalyzer(
    private val onObjectTracked: (TrackingData?) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    private val objectDetector = ObjectDetection.getClient(options)
    
    private var lastTrackingData: TrackingData? = null
    private var lastDetectedTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            try {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                objectDetector.process(image)
                    .addOnSuccessListener { detectedObjects ->
                        val fashionObjects = detectedObjects.filter { obj ->
                            obj.labels.any { it.text == PredefinedCategory.FASHION_GOOD }
                        }
                        
                        val targetObjects = if (fashionObjects.isNotEmpty()) fashionObjects else detectedObjects

                        val largestObject = targetObjects.maxByOrNull {
                            it.boundingBox.width() * it.boundingBox.height()
                        }
                        
                        val currentTime = System.currentTimeMillis()
                        if (largestObject != null) {
                            val isPortrait = imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270
                            val imageWidth = if (isPortrait) imageProxy.height else imageProxy.width
                            val imageHeight = if (isPortrait) imageProxy.width else imageProxy.height
                            
                            lastTrackingData = TrackingData(largestObject.boundingBox, imageWidth, imageHeight)
                            lastDetectedTime = currentTime
                            onObjectTracked(lastTrackingData)
                        } else {
                            if (currentTime - lastDetectedTime > 500) {
                                lastTrackingData = null
                                onObjectTracked(null)
                            } else {
                                onObjectTracked(lastTrackingData)
                            }
                        }
                    }
                    .addOnFailureListener {
                        onObjectTracked(null)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } catch (e: Exception) {
                Log.e("ObjectTrackingAnalyzer", "Error processing image", e)
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}

@Composable
fun ScanGarmentScreen(onClose: () -> Unit, onImageCaptured: (String, String, List<androidx.compose.ui.graphics.Color>) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val permissionsToRequest = remember {
        mutableListOf(Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasMediaPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    var latestGalleryUri by remember { mutableStateOf<Uri?>(null) }
    var zoomRatio by remember { mutableStateOf(1.0f) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    
    var trackingData by remember { mutableStateOf<TrackingData?>(null) }
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    LaunchedEffect(trackingData != null) {
        if (trackingData != null) {
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }
    
    var isProcessing by remember { mutableStateOf(false) }
    val handleImageCaptured = { path: String, category: String, colors: List<androidx.compose.ui.graphics.Color> ->
        isProcessing = false
        onImageCaptured(path, category, colors)
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
            val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            hasMediaPermission = permissions[mediaPermission] ?: hasMediaPermission
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                isProcessing = true
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                                decoder.isMutableRequired = true
                            }
                        } else {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }

                        var finalBitmap = bitmap
                        val maxSize = 1500
                        if (finalBitmap.width > maxSize || finalBitmap.height > maxSize) {
                            val scale = maxSize.toFloat() / Math.max(finalBitmap.width, finalBitmap.height)
                            finalBitmap = Bitmap.createScaledBitmap(finalBitmap, (finalBitmap.width * scale).toInt(), (finalBitmap.height * scale).toInt(), true)
                        }

                        val copyBitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true)
                        val segmenterOptions = SubjectSegmenterOptions.Builder().enableForegroundBitmap().build()
                        val segmenter = SubjectSegmentation.getClient(segmenterOptions)

                        segmenter.process(InputImage.fromBitmap(copyBitmap, 0))
                            .addOnSuccessListener { result ->
                                val fgBitmap = result.foregroundBitmap
                                val file = File(context.cacheDir, "gallery_garment_${System.currentTimeMillis()}.png")
                                val out = FileOutputStream(file)
                                if (fgBitmap != null) {
                                    val straightBitmap = straightenBitmap(fgBitmap)
                                    straightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    out.close()
                                    processCapturedImageData(straightBitmap, file.absolutePath, handleImageCaptured)
                                } else {
                                    val straightBitmap = straightenBitmap(copyBitmap)
                                    straightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    out.close()
                                    processCapturedImageData(straightBitmap, file.absolutePath, handleImageCaptured)
                                }
                            }
                            .addOnFailureListener {
                                val file = File(context.cacheDir, "gallery_garment_${System.currentTimeMillis()}.png")
                                val out = FileOutputStream(file)
                                copyBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                out.close()
                                processCapturedImageData(copyBitmap, file.absolutePath, handleImageCaptured)
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isProcessing = false
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasMediaPermission) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    LaunchedEffect(hasMediaPermission) {
        if (hasMediaPermission) {
            latestGalleryUri = getLatestGalleryImageUri(context)
        }
    }


    val captureImage = {
        isProcessing = true
        val file = File(context.cacheDir, "cropped_garment_${System.currentTimeMillis()}.png")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val exif = ExifInterface(file.absolutePath)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }

                    val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val uprightBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

                    val currentData = trackingData
                    if (currentData != null) {
                        val scaleX = uprightBitmap.width.toFloat() / currentData.sourceWidth
                        val scaleY = uprightBitmap.height.toFloat() / currentData.sourceHeight

                        val padX = (currentData.boundingBox.width() * scaleX * 0.15f).toInt()
                        val padY = (currentData.boundingBox.height() * scaleY * 0.15f).toInt()

                        val cropX = ((currentData.boundingBox.left * scaleX).toInt() - padX).coerceAtLeast(0)
                        val cropY = ((currentData.boundingBox.top * scaleY).toInt() - padY).coerceAtLeast(0)
                        
                        val rawW = (currentData.boundingBox.width() * scaleX).toInt() + (padX * 2)
                        val rawH = (currentData.boundingBox.height() * scaleY).toInt() + (padY * 2)
                        
                        val cropW = rawW.coerceIn(1, uprightBitmap.width - cropX)
                        val cropH = rawH.coerceIn(1, uprightBitmap.height - cropY)

                        val croppedBitmap = Bitmap.createBitmap(uprightBitmap, cropX, cropY, cropW, cropH)
                        
                        val segmenterOptions = SubjectSegmenterOptions.Builder().enableForegroundBitmap().build()
                        val segmenter = SubjectSegmentation.getClient(segmenterOptions)
                        
                        segmenter.process(InputImage.fromBitmap(croppedBitmap, 0))
                            .addOnSuccessListener { result ->
                                val fgBitmap = result.foregroundBitmap
                                val out = FileOutputStream(file)
                                if (fgBitmap != null) {
                                    val straightBitmap = straightenBitmap(fgBitmap)
                                    straightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    out.close()
                                    processCapturedImageData(straightBitmap, file.absolutePath, handleImageCaptured)
                                } else {
                                    val straightBitmap = straightenBitmap(croppedBitmap)
                                    straightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    out.close()
                                    processCapturedImageData(straightBitmap, file.absolutePath, handleImageCaptured)
                                }
                            }
                            .addOnFailureListener {
                                val out = FileOutputStream(file)
                                croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                out.close()
                                processCapturedImageData(croppedBitmap, file.absolutePath, handleImageCaptured)
                            }
                    } else {
                        val segmenterOptions = SubjectSegmenterOptions.Builder().enableForegroundBitmap().build()
                        val segmenter = SubjectSegmentation.getClient(segmenterOptions)
                        
                        segmenter.process(InputImage.fromBitmap(uprightBitmap, 0))
                            .addOnSuccessListener { result ->
                                val fgBitmap = result.foregroundBitmap
                                val out = FileOutputStream(file)
                                if (fgBitmap != null) {
                                    val straightBitmap = straightenBitmap(fgBitmap)
                                    straightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    out.close()
                                    processCapturedImageData(straightBitmap, file.absolutePath, handleImageCaptured)
                                } else {
                                    val straightBitmap = straightenBitmap(uprightBitmap)
                                    straightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    out.close()
                                    processCapturedImageData(straightBitmap, file.absolutePath, handleImageCaptured)
                                }
                            }
                            .addOnFailureListener {
                                val out = FileOutputStream(file)
                                uprightBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                out.close()
                                processCapturedImageData(uprightBitmap, file.absolutePath, handleImageCaptured)
                            }
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("ScanGarmentScreen", "Error al capturar la foto", exc)
                    isProcessing = false
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val blurRadius by animateDpAsState(
                targetValue = if (isProcessing) 40.dp else 0.dp,
                animationSpec = tween(800),
                label = "blurRadius"
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
                    .padding(top = 24.dp, bottom = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 12.dp)
                        .zIndex(10f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterStart)
                            .bouncyClickable { onClose() }
                            .clip(CircleShape)
                            .background(Color(0xFFBDBDBD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Atrás",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Escanear",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    var showInfo by remember { mutableStateOf(false) }
                    val cornerRadius by animateDpAsState(
                        targetValue = if (showInfo) 24.dp else 20.dp,
                        animationSpec = tween(300),
                        label = "cornerRadius"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (showInfo) Color.White else Color(0xFFBDBDBD),
                        animationSpec = tween(300),
                        label = "bgColor"
                    )
                    val elevation by animateDpAsState(
                        targetValue = if (showInfo) 16.dp else 0.dp,
                        animationSpec = tween(300),
                        label = "elevation"
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .widthIn(min = 40.dp, max = 280.dp)
                            .bouncyClickable { showInfo = !showInfo }
                            .shadow(
                                elevation = elevation, 
                                shape = RoundedCornerShape(cornerRadius),
                                spotColor = Color.Black.copy(alpha = 0.2f),
                                ambientColor = Color.Black.copy(alpha = 0.1f)
                            )
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(bgColor)
                            .animateContentSize(
                                animationSpec = tween(300)
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !showInfo,
                                enter = fadeIn(animationSpec = tween(200)),
                                exit = fadeOut(animationSpec = tween(150))
                            ) {
                                Text(
                                    text = "i",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = showInfo,
                                enter = fadeIn(animationSpec = tween(300, delayMillis = 150)),
                                exit = fadeOut(animationSpec = tween(150))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Consejo para escanear",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Text(
                                        text = "Recuerda limpiar el sensor de la cámara y contar con buena iluminación con un fondo sin texturas de preferencia para un resultado satisfactorio.",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color(0xFFE5E5E5))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        var previewSize by remember { mutableStateOf(IntSize.Zero) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .onGloballyPositioned { coordinates ->
                                    previewSize = coordinates.size
                                }
                        ) {
                            if (hasCameraPermission) {
                                CameraPreviewView(
                                    modifier = Modifier.fillMaxSize(),
                                    isFlashEnabled = isFlashEnabled,
                                    imageCapture = imageCapture,
                                    onZoomRatioChanged = { zoomRatio = it },
                                    onObjectTracked = { trackingData = it }
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1fx", zoomRatio),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hay acceso a la cámara", color = Color.Gray, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            if (previewSize.width > 0 && previewSize.height > 0) {
                                val density = LocalDensity.current
                                
                                val defaultPadding = 100f
                                val defaultRect = androidx.compose.ui.geometry.Rect(
                                    left = defaultPadding,
                                    top = defaultPadding,
                                    right = previewSize.width.toFloat() - defaultPadding,
                                    bottom = previewSize.height.toFloat() - defaultPadding
                                )

                                val targetRect = trackingData?.let { data ->
                                    val scale = max(previewSize.width.toFloat() / data.sourceWidth, previewSize.height.toFloat() / data.sourceHeight)
                                    val scaledWidth = data.sourceWidth * scale
                                    val scaledHeight = data.sourceHeight * scale
                                    val offsetX = (previewSize.width - scaledWidth) / 2f
                                    val offsetY = (previewSize.height - scaledHeight) / 2f
                                    
                                    val trackPad = 10f
                                    androidx.compose.ui.geometry.Rect(
                                        left = (data.boundingBox.left * scale) + offsetX - trackPad,
                                        top = (data.boundingBox.top * scale) + offsetY - trackPad,
                                        right = (data.boundingBox.right * scale) + offsetX + trackPad,
                                        bottom = (data.boundingBox.bottom * scale) + offsetY + trackPad
                                    )
                                } ?: defaultRect

                                val animationSpec = if (trackingData != null) {
                                    androidx.compose.animation.core.spring<Float>(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)
                                } else {
                                    tween(300)
                                }

                                val animLeft by animateFloatAsState(targetRect.left, animationSpec = animationSpec)
                                val animTop by animateFloatAsState(targetRect.top, animationSpec = animationSpec)
                                val animRight by animateFloatAsState(targetRect.right, animationSpec = animationSpec)
                                val animBottom by animateFloatAsState(targetRect.bottom, animationSpec = animationSpec)

                                val widthDp = with(density) { max(0f, animRight - animLeft).toDp() }
                                val heightDp = with(density) { max(0f, animBottom - animTop).toDp() }
                                val offsetX = with(density) { animLeft.toDp() }
                                val offsetY = with(density) { animTop.toDp() }

                                var showCorners by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(400)
                                    showCorners = true
                                }
                                val cornersAlpha by animateFloatAsState(
                                    targetValue = if (showCorners) 1f else 0f,
                                    animationSpec = tween(400),
                                    label = "cornersAlpha"
                                )

                                ViewfinderCorners(
                                    modifier = Modifier
                                        .absoluteOffset(x = offsetX, y = offsetY)
                                        .size(width = widthDp, height = heightDp)
                                        .graphicsLayer { alpha = cornersAlpha }
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = trackingData == null,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Colocar prenda dentro del recuadro",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .drawBehind {
                                    val strokeWidth = 8f
                                    val stroke = Stroke(
                                        width = strokeWidth,
                                        cap = StrokeCap.Round,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f), 0f)
                                    )
                                    drawRoundRect(color = Color.Gray, style = stroke, cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()))
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .bouncyClickable { galleryLauncher.launch("image/*") }
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFBDBDBD))
                        ) {
                            if (latestGalleryUri != null) {
                                AsyncImage(
                                    model = latestGalleryUri,
                                    contentDescription = "Última foto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .bouncyClickable { captureImage() }
                                .border(4.dp, Color.Gray, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A4A4A))
                        )

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .bouncyClickable { isFlashEnabled = !isFlashEnabled }
                                .clip(CircleShape)
                                .background(if (isFlashEnabled) Color(0xFFFFC107) else Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isFlashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff, 
                                contentDescription = "Flash", 
                                tint = if (isFlashEnabled) Color.White else Color(0xFF757575)
                            )
                        }
                    }
                }
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = isProcessing,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.4f))
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFFC7E054),
                            strokeWidth = 6.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Analizando prendas...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extrayendo silueta y colores",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewfinderCorners(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val cornerLength = 40.dp.toPx()
        val cornerStroke = 6.dp.toPx()
        val inset = cornerStroke / 2f
        val w = size.width
        val h = size.height

        val stroke = Stroke(width = cornerStroke, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(inset, inset + cornerLength)
            lineTo(inset, inset)
            lineTo(inset + cornerLength, inset)

            moveTo(w - inset - cornerLength, inset)
            lineTo(w - inset, inset)
            lineTo(w - inset, inset + cornerLength)

            moveTo(inset, h - inset - cornerLength)
            lineTo(inset, h - inset)
            lineTo(inset + cornerLength, h - inset)

            moveTo(w - inset - cornerLength, h - inset)
            lineTo(w - inset, h - inset)
            lineTo(w - inset, h - inset - cornerLength)
        }
        
        drawPath(path = path, color = Color.White, style = stroke)
    }
}

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier, 
    isFlashEnabled: Boolean, 
    imageCapture: ImageCapture,
    onZoomRatioChanged: (Float) -> Unit,
    onObjectTracked: (TrackingData?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    var currentZoomRatio by remember { mutableStateOf(1f) }
    var maxZoomRatio by remember { mutableStateOf(1f) }
    var minZoomRatio by remember { mutableStateOf(1f) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    LaunchedEffect(isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    DisposableEffect(camera) {
        val observer = androidx.lifecycle.Observer<ZoomState> { state ->
            currentZoomRatio = state.zoomRatio
            maxZoomRatio = state.maxZoomRatio
            minZoomRatio = state.minZoomRatio
            onZoomRatioChanged(state.zoomRatio)
        }
        camera?.cameraInfo?.zoomState?.observe(lifecycleOwner, observer)
        onDispose {
            camera?.cameraInfo?.zoomState?.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier.pointerInput(camera) {
            detectTransformGestures { _, _, zoom, _ ->
                camera?.let { cam ->
                    val newZoom = max(minZoomRatio, min(maxZoomRatio, currentZoomRatio * zoom))
                    cam.cameraControl.setZoomRatio(newZoom)
                }
            }
        }
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, ObjectTrackingAnalyzer(onObjectTracked))
                        }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                        camera?.cameraControl?.enableTorch(isFlashEnabled)
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { view ->
                val ctx = view.context
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Error unbinding camera", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        )
    }
}

fun straightenBitmap(bitmap: Bitmap): Bitmap {
    val scale = 150f / Math.max(bitmap.width, bitmap.height).toFloat()
    val smallW = Math.max(1, (bitmap.width * scale).toInt())
    val smallH = Math.max(1, (bitmap.height * scale).toInt())
    val smallBitmap = Bitmap.createScaledBitmap(bitmap, smallW, smallH, true)

    var m00 = 0.0
    var m10 = 0.0
    var m01 = 0.0

    val pixels = IntArray(smallW * smallH)
    smallBitmap.getPixels(pixels, 0, smallW, 0, 0, smallW, smallH)

    for (y in 0 until smallH) {
        for (x in 0 until smallW) {
            val alpha = android.graphics.Color.alpha(pixels[y * smallW + x])
            if (alpha > 50) {
                m00 += alpha
                m10 += alpha * x
                m01 += alpha * y
            }
        }
    }

    if (m00 == 0.0) return bitmap

    val cx = m10 / m00
    val cy = m01 / m00

    var mu20 = 0.0
    var mu02 = 0.0
    var mu11 = 0.0

    for (y in 0 until smallH) {
        for (x in 0 until smallW) {
            val alpha = android.graphics.Color.alpha(pixels[y * smallW + x])
            if (alpha > 50) {
                val dx = x - cx
                val dy = y - cy
                mu20 += alpha * dx * dx
                mu02 += alpha * dy * dy
                mu11 += alpha * dx * dy
            }
        }
    }

    val theta = 0.5 * Math.atan2(2 * mu11, mu20 - mu02)
    val angleDeg = Math.toDegrees(theta)

    val rotationAngle = if (Math.abs(angleDeg) < 45.0) {
        -angleDeg.toFloat()
    } else {
        if (angleDeg > 0) (90.0 - angleDeg).toFloat() else (-90.0 - angleDeg).toFloat()
    }

    val matrix = android.graphics.Matrix()
    matrix.postRotate(rotationAngle)
    
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    return cropTransparentBounds(rotatedBitmap)
}

fun cropTransparentBounds(bitmap: Bitmap): Bitmap {
    val scale = 200f / Math.max(bitmap.width, bitmap.height).toFloat()
    val smallW = Math.max(1, (bitmap.width * scale).toInt())
    val smallH = Math.max(1, (bitmap.height * scale).toInt())
    val smallBitmap = Bitmap.createScaledBitmap(bitmap, smallW, smallH, true)

    var minX = smallW
    var minY = smallH
    var maxX = 0
    var maxY = 0

    val pixels = IntArray(smallW * smallH)
    smallBitmap.getPixels(pixels, 0, smallW, 0, 0, smallW, smallH)

    for (y in 0 until smallH) {
        for (x in 0 until smallW) {
            if (android.graphics.Color.alpha(pixels[y * smallW + x]) > 10) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }

    if (minX > maxX || minY > maxY) return bitmap

    val padSmall = 5
    minX = Math.max(0, minX - padSmall)
    minY = Math.max(0, minY - padSmall)
    maxX = Math.min(smallW - 1, maxX + padSmall)
    maxY = Math.min(smallH - 1, maxY + padSmall)

    val actualMinX = (minX / scale).toInt().coerceIn(0, bitmap.width - 1)
    val actualMinY = (minY / scale).toInt().coerceIn(0, bitmap.height - 1)
    val actualMaxX = (maxX / scale).toInt().coerceIn(0, bitmap.width - 1)
    val actualMaxY = (maxY / scale).toInt().coerceIn(0, bitmap.height - 1)
    
    val width = actualMaxX - actualMinX + 1
    val height = actualMaxY - actualMinY + 1

    return Bitmap.createBitmap(bitmap, actualMinX, actualMinY, width, height)
}

fun processCapturedImageData(
    bitmap: Bitmap,
    filePath: String,
    onComplete: (String, String, List<androidx.compose.ui.graphics.Color>) -> Unit
) {
    val extractedColors = mutableListOf<androidx.compose.ui.graphics.Color>()
    val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
    
    val swatches = listOfNotNull(
        palette.vibrantSwatch,
        palette.dominantSwatch,
        palette.lightVibrantSwatch,
        palette.darkVibrantSwatch,
        palette.mutedSwatch
    )
    
    for (swatch in swatches) {
        val color = androidx.compose.ui.graphics.Color(swatch.rgb)
        if (!extractedColors.contains(color) && extractedColors.size < 3) {
            extractedColors.add(color)
        }
    }
    
    if (extractedColors.isEmpty()) {
        extractedColors.add(androidx.compose.ui.graphics.Color.Gray)
    }

    val labeler = com.google.mlkit.vision.label.ImageLabeling.getClient(
        com.google.mlkit.vision.label.defaults.ImageLabelerOptions.DEFAULT_OPTIONS
    )
    val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
    
    labeler.process(image)
        .addOnSuccessListener { labels ->
            var category = "Otro"
            val foundLabels = labels.map { it.text.lowercase() }
            
            if (foundLabels.any { it in listOf("glasses", "sunglasses") }) {
                category = "Gafas"
            } else if (foundLabels.any { it in listOf("watch", "wristwatch") }) {
                category = "Reloj"
            } else if (foundLabels.any { it in listOf("belt") }) {
                category = "Cinturón"
            } else if (foundLabels.any { it in listOf("tie") }) {
                category = "Corbata"
            } else if (foundLabels.any { it in listOf("scarf") }) {
                category = "Bufanda"
            } else if (foundLabels.any { it in listOf("necklace", "ring", "earring", "jewelry") }) {
                category = "Joyería"
            } else if (foundLabels.any { it in listOf("backpack") }) {
                category = "Mochila"
            } else if (foundLabels.any { it in listOf("bag", "handbag") }) {
                category = "Bolso"
            } else if (foundLabels.any { it in listOf("cap") }) {
                category = "Gorra"
            } else if (foundLabels.any { it in listOf("hat", "fedora", "sombrero") }) {
                category = "Sombrero"
            } else if (foundLabels.any { it in listOf("beanie") }) {
                category = "Gorro"
            } else if (foundLabels.any { it in listOf("sneaker") }) {
                category = "Zapatillas"
            } else if (foundLabels.any { it in listOf("boot") }) {
                category = "Botas"
            } else if (foundLabels.any { it in listOf("shoe", "footwear") }) {
                category = "Zapatos"
            } else if (foundLabels.any { it in listOf("sweater") }) {
                category = "Suéter"
            } else if (foundLabels.any { it in listOf("jacket") }) {
                category = "Chaqueta"
            } else if (foundLabels.any { it in listOf("coat") }) {
                category = "Abrigo"
            } else if (foundLabels.any { it in listOf("blouse") }) {
                category = "Blusa"
            } else if (foundLabels.any { it in listOf("t-shirt") }) {
                category = "Camiseta"
            } else if (foundLabels.any { it in listOf("shirt", "top") }) {
                category = "Camisa"
            } else if (foundLabels.any { it in listOf("jeans") }) {
                category = "Jeans"
            } else if (foundLabels.any { it in listOf("shorts") }) {
                category = "Short"
            } else if (foundLabels.any { it in listOf("skirt") }) {
                category = "Falda"
            } else if (foundLabels.any { it in listOf("pants", "trousers") }) {
                category = "Pantalón"
            } else if (foundLabels.any { it in listOf("dress") }) {
                category = "Vestido"
            }
            
            onComplete(filePath, category, extractedColors)
        }
        .addOnFailureListener {
            onComplete(filePath, "Otro", extractedColors)
        }
}
