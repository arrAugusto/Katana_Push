package com.army.katanapush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.* // Para diseño
import androidx.compose.material3.* // Para Material3
import androidx.compose.runtime.* // Para estados y composición
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Para obtener el contexto
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.army.katanapush.ui.theme.KatanaPushTheme
import com.army.katanapush.ui.theme.views.CameraView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

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

// Retrofit API Interface
interface ApiService {
    @Multipart
    @POST("index.php") // Cambia la ruta si es necesario
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadResponse
}

// Data class para la respuesta del servidor
data class UploadResponse(
    val message: String,
    val file_path: String,
    val command: String,
    val distance: String
)

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isSending by remember { mutableStateOf(false) } // Estado para controlar envío automático
    var uploadResponse by remember { mutableStateOf<String?>(null) } // Estado para la respuesta del servidor
    val coroutineScope = rememberCoroutineScope()

    // Configura Retrofit
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2/ups/") // Cambia esto a tu URL base
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para iniciar envío automático
        Button(onClick = {
            isSending = true
            coroutineScope.launch {
                while (isSending) {
                    CameraView.takePhoto(context) { path ->
                        path?.let {
                            coroutineScope.launch {
                                uploadResponse = withContext(Dispatchers.IO) {
                                    uploadImage(it, apiService)
                                }
                            }
                        }
                    }
                    delay(5000) // Espera 5 segundos antes de capturar y subir la siguiente imagen
                }
            }
        }, enabled = !isSending) {
            Text(text = "Iniciar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para detener envío automático
        Button(onClick = {
            isSending = false // Detiene el envío
        }, enabled = isSending) {
            Text(text = "Detener")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la respuesta del servidor
        uploadResponse?.let {
            Text(text = "Respuesta del servidor: $it")
        }
    }
}

// Método para subir la imagen al servidor
suspend fun uploadImage(photoPath: String, apiService: ApiService): String {
    return try {
        val file = File(photoPath)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("ruta", file.name, requestBody)

        val response = apiService.uploadImage(multipartBody)
        "Mensaje: ${response.message}, Ruta del archivo: ${response.file_path}, Comando: ${response.command}, Distancia: ${response.distance}"
    } catch (e: Exception) {
        "Error al subir la imagen: ${e.message}"
    }
}
