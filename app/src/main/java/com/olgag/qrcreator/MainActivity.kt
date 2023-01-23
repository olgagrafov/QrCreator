package com.olgag.qrcreator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.olgag.qrcreator.ui.theme.QRCreatorTheme
import com.olgag.qrcreator.viewmodel.QRViewModel
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

const val MAX_CHAR = 2048
const val MAX_LINES_FOR_INPUT_TEXT = 3
const val MIN_LENGTH_QR_TEXT = 10

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
           // Text(text =" stringResource(R.string.create)")
            QRCreatorTheme {
                GetScaffold(this)
            }
        }
    }
}

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun GetScaffold(context: Context) {
        val config = context.resources.configuration

        Scaffold(
            topBar = { TopAppBar(
                title = {
                    Text(text = stringResource(R.string.create))
                    Image(
                        painter = painterResource(R.drawable.ic_qr_code),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.padding(5.dp)
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colors.background, CircleShape)
                    )
                    Text(text = stringResource(R.string.code)) },
                    navigationIcon = {
                    IconButton(
                            onClick = { (context as? Activity)?.finish() }) {
                            Icon(if(config.layoutDirection == View.LAYOUT_DIRECTION_LTR) Icons.Filled.ArrowBack else Icons.Filled.ArrowForward, null)
                        }
                }) },
            content = { MainContent(context) },
        )

    }

    @Composable
    fun MainContent(context: Context) {
        val configuration  = LocalConfiguration.current
        val isPortrait by remember { mutableStateOf(
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    false
                }
                else -> {
                    true
                }
            }
        )}
        Box(
            Modifier
                .background(brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.background,
                        MaterialTheme.colors.primary
                    )
                ))
                .fillMaxSize()
                .wrapContentSize(Alignment.TopCenter))
                { SaveableContent(isPortrait, context) }
    }

    @Composable
    fun SaveableContent(isPortrait: Boolean, context: Context) {
        var qrText by rememberSaveable { mutableStateOf("") }
        QRView(context, qrText = qrText,
            onQrTextChange = { if (it.length <= MAX_CHAR) qrText = it },
            isPortrait = isPortrait)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun QRView(context: Context, qrText: String, onQrTextChange: (String) -> Unit, isPortrait: Boolean) {
        val btTmp:Bitmap?=null
        var pic by rememberSaveable { mutableStateOf(btTmp) }
        val kc = LocalSoftwareKeyboardController.current
        val innerPadding = PaddingValues(start = 20.dp)

        val trailingIconViewClean = @Composable {
            IconButton(
                onClick = {
                    onQrTextChange("")
                    pic = null
                },
            ) {
                Icon(Icons.Default.Clear, null)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement  = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = if(isPortrait) Modifier.fillMaxWidth().weight(1f)
                               else  Modifier.width(350.dp),
                    shape = MaterialTheme.shapes.medium,
                    value = qrText,
                    leadingIcon = {
                       Icon(painterResource(R.drawable.ic_qr_img), null,
                           Modifier.clip(CircleShape)
                               .border(1.dp, MaterialTheme.colors.surface, CircleShape))
                    },
                    trailingIcon = if (qrText.isNotBlank()) trailingIconViewClean else null,
                    onValueChange = onQrTextChange,
                    label = { Text(text = stringResource(R.string.text_qr)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.enter_text),
                            style = TextStyle(fontSize = 14.sp)
                        )
                    },
                    maxLines = MAX_LINES_FOR_INPUT_TEXT,
                )
             //   if(qrText.trim().length > MIN_LENGTH_QR_TEXT) {
                //    if (isUrl(qrText)) {
                        IconButton(
                            modifier = Modifier.width(50.dp).padding(innerPadding),
                            onClick = {
                                pic = createBitmapFromText(qrText, context)
                                kc?.hide()
                            },
                        ) {
                            Icon(Icons.Default.Done, null)
                        }
                    }
               // }
           // }
            pic?.let {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement  = Arrangement.Center) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { shareQR(context, it) }) {
                            IconAnimated()
                        }
                        CardImage(it)
                    }
                }
            }

        }
    }

    @Composable
    fun CardImage(bt: Bitmap) {
        Card(
                modifier = Modifier.padding(top = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Image(
                    bitmap = bt.asImageBitmap(),
                    contentDescription = stringResource(R.string.description)
                )
            }
    }

    @Composable
    fun IconAnimated() {
        var targetValue by remember { mutableStateOf(0f) }
        val animationProgress by animateFloatAsState(
            targetValue = targetValue,
            animationSpec = repeatable(
                iterations = 3,
                animation = tween(durationMillis = 800)
            )
        )
        SideEffect { targetValue = 2f }
        Icon(
            Icons.Filled.Share, null,
            modifier = Modifier
                .scale(animationProgress)
                .alpha(3 - animationProgress)
                .padding(20.dp)
        )
    }

    fun createBitmapFromText(qRStrin: String ,context: Context): Bitmap {
        val viewModel = QRViewModel(context)
        viewModel.qrText = qRStrin
        return  viewModel.CreateBitmapQR()
    }

    fun shareQR(context:Context,  bit: Bitmap) {
        try {
            val cacheImagePath = File(context.cacheDir, "images")
            cacheImagePath.mkdirs()
            val stream =
                FileOutputStream("$cacheImagePath/image.png")
            bit.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imageFile = File( cacheImagePath, "image.png")
            val imageUri: Uri? =
                FileProvider.getUriForFile(context,  context.getString(R.string.file_provider), imageFile)

            if (imageUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setDataAndType(imageUri, "image/*")
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(shareIntent, "Share image using"))
            } else {
                Toast.makeText(context, context.getString(R.string.wrong), Toast.LENGTH_SHORT)
                    .show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.wrong), Toast.LENGTH_SHORT).show()
            Log.i("e_catch:", e.toString())
        }
    }
    fun isUrl(string: String): Boolean {
        return try {
            URL(string).toURI()
            true
        } catch (e: MalformedURLException) {
            false
        } catch (e: URISyntaxException) {
            false
        }
    }
