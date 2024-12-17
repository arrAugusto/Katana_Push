package com.army.katanapush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.* // Diseño
import androidx.compose.material3.* // Material3
import androidx.compose.runtime.* // Estados y composición
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.army.katanapush.interfaces.ApiService // Importación de la interfaz
import com.army.katanapush.services.UploadImage // Importación del servicio de subida
import com.army.katanapush.ui.theme.KatanaPushTheme
import com.army.katanapush.ui.theme.views.CameraView
import kotlinx.coroutines.* // Coroutines
import okhttp3.* // OkHttp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configura la cámara
        CameraView.setupCamera(this, this)

        setContent {
            KatanaPushTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Clase para respuesta del servidor
data class UploadResponse(
    val message: String,
    val file_path: String,
    val command: String,
    val distance: String
)

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isSending by remember { mutableStateOf(false) }
    var uploadResponse by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Instancia de la API
    val apiService: ApiService = getApiService()
    val uploadImageService = UploadImage()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StartButton(isSending = isSending, onStart = {
            isSending = true
            coroutineScope.launch {
                while (isSending) {
                    CameraView.takePhoto(context) { path ->
                        path?.let {
                            coroutineScope.launch {
                                uploadResponse = withContext(Dispatchers.IO) {
                                    uploadImageService.uploadImage(it, apiService)
                                }
                            }
                        }
                    }
                    delay(5000) // Espera 5 segundos antes de la siguiente captura
                }
            }
        })

        Spacer(modifier = Modifier.height(16.dp))

        StopButton(isSending = isSending, onStop = { isSending = false })

        Spacer(modifier = Modifier.height(16.dp))

        uploadResponse?.let {
            Text(text = "Respuesta del servidor: $it")
        }
    }
}

@Composable
fun StartButton(isSending: Boolean, onStart: () -> Unit) {
    Button(onClick = onStart, enabled = !isSending) {
        Text(text = "Iniciar")
    }
}

@Composable
fun StopButton(isSending: Boolean, onStop: () -> Unit) {
    Button(onClick = onStop, enabled = isSending) {
        Text(text = "Detener")
    }
}

fun getApiService(): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2/ups/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(ApiService::class.java)
}
