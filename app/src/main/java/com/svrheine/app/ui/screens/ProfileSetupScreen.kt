package com.svrheine.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.svrheine.app.data.UserProfile
import com.svrheine.app.viewmodel.MainViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: MainViewModel,
    navController: NavController,
    isEditing: Boolean = false
) {
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    var fullName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var signatureBase64 by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            fullName = it.fullName
            street = it.street
            houseNumber = it.houseNumber
            zip = it.zip
            city = it.city
            iban = it.iban
            bankName = it.bankName
            signatureBase64 = it.signatureBase64
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Profil bearbeiten" else "Profil einrichten") },
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Vollständiger Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Straße") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = houseNumber, onValueChange = { houseNumber = it }, label = { Text("Hausnummer") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("PLZ") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Stadt") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = iban, onValueChange = { iban = it }, label = { Text("IBAN") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bankname") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Unterschrift:", style = MaterialTheme.typography.titleMedium)
            
            SignaturePad(
                initialBase64 = signatureBase64,
                onSignatureCaptured = { signatureBase64 = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.saveProfile(
                        UserProfile(
                            fullName = fullName,
                            street = street,
                            houseNumber = houseNumber,
                            zip = zip,
                            city = city,
                            iban = iban,
                            bankName = bankName,
                            signatureBase64 = signatureBase64
                        )
                    )
                    if (!isEditing) {
                        navController.navigate("home") { popUpTo("profile_setup") { inclusive = true } }
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profil speichern")
            }
        }
    }
}

@Composable
fun SignaturePad(
    initialBase64: String?,
    onSignatureCaptured: (String?) -> Unit
) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .background(Color.White)
                .onGloballyPositioned { canvasSize = it.size }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply { moveTo(offset.x, offset.y) }
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            val p = currentPath
                            currentPath = null
                            currentPath = p
                        },
                        onDragEnd = {
                            currentPath?.let { paths.add(it) }
                            currentPath = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                currentPath?.let { path ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { 
                paths.clear()
                onSignatureCaptured(null)
            }) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Löschen")
            }
            
            Button(onClick = {
                if (paths.isNotEmpty() && canvasSize.width > 0 && canvasSize.height > 0) {
                    // High-Res capture: 4x scale
                    val scaleFactor = 4f
                    val bitmap = Bitmap.createBitmap(
                        (canvasSize.width * scaleFactor).toInt(),
                        (canvasSize.height * scaleFactor).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    canvas.scale(scaleFactor, scaleFactor)
                    
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 6f
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        strokeJoin = android.graphics.Paint.Join.ROUND
                        isAntiAlias = true
                        isFilterBitmap = true
                    }
                    
                    paths.forEach { composePath ->
                        canvas.drawPath(composePath.asAndroidPath(), paint)
                    }
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                    onSignatureCaptured(base64)
                }
            }) {
                Text("Unterschrift übernehmen")
            }
        }
        
        if (initialBase64 != null) {
            Text("Unterschrift ist gespeichert ✅", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Keine Unterschrift gespeichert", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}
