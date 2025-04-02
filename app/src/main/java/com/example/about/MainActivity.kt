package com.example.about

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.about.ui.theme.AboutTheme
import androidx.core.net.toUri
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AboutTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProfileEditorScreen(innerPadding)
                }
            }
        }
    }
}


@Composable
fun ProfileEditorScreen(innerPadding: PaddingValues) {
    var name by remember { mutableStateOf("John Smith") }
    var bio by remember { mutableStateOf("Android dev & coffee addict") }
    var website by remember { mutableStateOf("https://example.com") }

    var showQr by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding() + 32.dp, start = 32.dp, end = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("About Me", fontSize = 24.sp)

        InlineEditableField(label = "Name", value = name, onValueChange = { name = it })
        InlineEditableField(
            label = "Bio",
            value = bio,
            onValueChange = { bio = it },
            singleLine = false
        )
        InlineEditableField(label = "Website", value = website, onValueChange = { website = it })

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { showQr = true }) {
            Text("Share")
        }

        if (showQr) {
            QrDialog(data = User(name, bio, website).toJson(), onDismiss = { showQr = false })
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InlineEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isWebsite = label.lowercase() == "website"
    val isValidUrl = remember(value) {
        try {
            val uri = value.toUri()
            uri.scheme != null && uri.host != null
        } catch (e: Exception) {
            false
        }
    }

    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!singleLine) Modifier.heightIn(min = 240.dp) else Modifier),
            singleLine = singleLine,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    modifier = Modifier
                        .clickable { isEditing = false }
                        .padding(8.dp)
                )
            }
        )
    } else {
        val displayText = "$label: $value"

        val modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    if (isWebsite && isValidUrl) {
                        val url = if (value.startsWith("https")) value else "https://$value"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                    }
                },
                onLongClick = {
                    isEditing = true
                }
            )

        val textContent = if (isWebsite && isValidUrl) {
            buildAnnotatedString {
                append("$label: ")
                pushStringAnnotation(tag = "URL", annotation = value)
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(value)
                }
                pop()
            }
        } else {
            buildAnnotatedString { append(displayText) }
        }

        Text(
            text = textContent,
            fontSize = 18.sp,
            modifier = modifier
        )
    }
}

@Composable
fun QrDialog(data: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        val qrBitmap = remember(data) { generateQrCode(data) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code",
            )
        }
    }
}


fun generateQrCode(data: String): Bitmap {
    val size = 512
    val bits = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap[x, y] =
                if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    return bitmap
}


fun User.toJson(): String {
    return Json.encodeToString(this)
}


@Composable
fun PreviewContainer(content: @Composable () -> Unit) {
    AboutTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSingleLineField() {
    PreviewContainer {
        InlineEditableField(
            label = "Name",
            value = "John Smith",
            onValueChange = {},
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMultiLineField() {
    PreviewContainer {
        InlineEditableField(
            label = "Bio",
            value = "I build things with Compose. I drink way too much coffee.",
            onValueChange = {},
            singleLine = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQrDialogContent() {
    val sampleUser = User(
        name = "John Smith",
        bio = "Android dev & Compose fan",
        website = "https://example.com"
    )
    val qrBitmap = generateQrCode(sampleUser.toJson())

    PreviewContainer {
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.fillMaxWidth()
        )

    }
}

