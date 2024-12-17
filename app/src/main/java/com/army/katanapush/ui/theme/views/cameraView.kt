package com.army.katanapush.ui.theme.views

import android.content.Context
import android.os.Environment
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraView {

    companion object {
        // Variables necesarias para la captura de imágenes
        private lateinit var imageCapture: ImageCapture
        private lateinit var outputDirectory: File
        private lateinit var cameraExecutor: ExecutorService

        /**
         * Configura la cámara y el entorno para capturar imágenes.
         * @param context El contexto de la aplicación o actividad.
         * @param lifecycleOwner El ciclo de vida del propietario (Activity o Fragment).
         */
        fun setupCamera(context: Context, lifecycleOwner: LifecycleOwner) {
            // Inicializa el CameraProvider de CameraX
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Configura la vista previa de la cámara
                val preview = androidx.camera.core.Preview.Builder().build()

                // Selecciona la cámara trasera por defecto
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Inicializa ImageCapture para tomar fotos
                imageCapture = ImageCapture.Builder().build()

                // Enlaza la cámara con el ciclo de vida del propietario
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            }, ContextCompat.getMainExecutor(context))

            // Configura el directorio de salida para las fotos
            outputDirectory = getOutputDirectory(context)

            // Configura un executor para tareas en segundo plano
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        /**
         * Toma una foto y la guarda en el directorio configurado.
         * @param context El contexto de la aplicación o actividad.
         * @param onPhotoSaved Callback que devuelve la ruta del archivo guardado o null si falla.
         */
        fun takePhoto(context: Context, onPhotoSaved: (String?) -> Unit) {
            // Genera un archivo único para la foto
            val photoFile = createFile()

            // Configura las opciones de salida de la imagen
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Realiza la captura de la foto
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        // Llama al callback con null en caso de error
                        onPhotoSaved(null)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        // Llama al callback con la ruta de la imagen guardada
                        onPhotoSaved(photoFile.absolutePath)
                    }
                }
            )
        }

        /**
         * Crea un archivo con un nombre único basado en la fecha y hora actual.
         * @return File El archivo creado donde se guardará la foto.
         */
        private fun createFile(): File {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
                .format(System.currentTimeMillis())

            return File(outputDirectory, "$timestamp.jpg")
        }

        /**
         * Obtiene el directorio de salida donde se guardarán las fotos.
         * @param context El contexto de la aplicación o actividad.
         * @return File El directorio de imágenes.
         */
        private fun getOutputDirectory(context: Context): File {
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        }
    }
}
