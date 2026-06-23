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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
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
                        val largestObject = detectedObjects.maxByOrNull {
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
fun ScanGarmentScreen(onClose: () -> Unit) {
    val context = LocalContext.current

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
    
    // Tracking State
    var trackingData by remember { mutableStateOf<TrackingData?>(null) }
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
            // TODO: Procesar la imagen seleccionada de la galería
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
        val file = File(context.cacheDir, "cropped_garment.jpg")
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

                        val cropX = (currentData.boundingBox.left * scaleX).toInt().coerceIn(0, uprightBitmap.width)
                        val cropY = (currentData.boundingBox.top * scaleY).toInt().coerceIn(0, uprightBitmap.height)
                        val cropW = (currentData.boundingBox.width() * scaleX).toInt().coerceIn(1, uprightBitmap.width - cropX)
                        val cropH = (currentData.boundingBox.height() * scaleY).toInt().coerceIn(1, uprightBitmap.height - cropY)

                        val croppedBitmap = Bitmap.createBitmap(uprightBitmap, cropX, cropY, cropW, cropH)
                        val out = FileOutputStream(file)
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.close()
                    } else {
                        val out = FileOutputStream(file)
                        uprightBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.close()
                    }
                    
                    // TODO: Navigate to Specification Screen passing file.absolutePath
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("ScanGarmentScreen", "Error al capturar la foto", exc)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, bottom = 40.dp)
            ) {
                // App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBDBDBD))
                            .clickable { onClose() },
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
                        color = Color.Black
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBDBDBD)),
                        contentAlignment = Alignment.Center
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
                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color(0xFFE5E5E5))

                // Main Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Camera Viewfinder Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        var previewSize by remember { mutableStateOf(IntSize.Zero) }

                        // Inner Area
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
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Se requiere permiso de cámara para escanear", color = Color.Gray, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            // Corners overlay calculated dynamically
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

                                ViewfinderCorners(
                                    modifier = Modifier
                                        .absoluteOffset(x = offsetX, y = offsetY)
                                        .size(width = widthDp, height = heightDp)
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = trackingData == null,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
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

                    // Bottom Controls
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Thumbnail
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFBDBDBD))
                                .clickable { galleryLauncher.launch("image/*") }
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

                        // Capture Button
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .border(4.dp, Color.Gray, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A4A4A))
                                .clickable { captureImage() }
                        )

                        // Flash Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isFlashEnabled) Color(0xFFFFC107) else Color(0xFFE0E0E0))
                                .clickable { isFlashEnabled = !isFlashEnabled },
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
            modifier = Modifier.fillMaxSize()
        )
    }
}
