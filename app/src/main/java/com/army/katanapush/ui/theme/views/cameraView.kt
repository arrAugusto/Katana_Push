package com.army.katanapush.ui.theme.views

import android.content.Context
import android.os.Environment
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
        private lateinit var imageCapture: ImageCapture
        private lateinit var outputDirectory: File
        private lateinit var cameraExecutor: ExecutorService

        fun setupCamera(context: Context, lifecycleOwner: LifecycleOwner) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = androidx.camera.core.Preview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageCapture = ImageCapture.Builder().build()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(context))

            outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        fun takePhoto(context: Context, onPhotoSaved: (String?) -> Unit) {
            val file = File(
                outputDirectory,
                SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        onPhotoSaved(null)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        onPhotoSaved(file.absolutePath)
                    }
                }
            )
        }
    }
}
