package com.example.sudokuscan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.lifecycle.LifecycleOwner
import com.example.sudokuscan.databinding.ActivityScanBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class Scan : AppCompatActivity(), ImageAnalysis.Analyzer {

    private lateinit var binding: ActivityScanBinding

    private val executor = Executors.newSingleThreadExecutor()
    private val permissions = listOf(Manifest.permission.CAMERA)
    private lateinit var bufferImage: Bitmap
    //private val processor = ImageProcessor(bufferImage)

    /**
     * Function on createe
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                startCamera(cameraProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, getExecutor())

    } // End of on create function. //

    override fun onDestroy() {
        /* Terminate all outstanding analyzing jobs (if there is any). */
        executor.apply {
            shutdown()
            awaitTermination(1000, TimeUnit.MILLISECONDS)
        }

        super.onDestroy()
    }

    /**
     *
     */
    private fun getExecutor(): Executor = ContextCompat.getMainExecutor(this)

    /**
     * Method to initialise the camera capture and the analysis task
     */
    @SuppressLint("RestrictedApi")
    private fun startCamera(cameraProvider: ProcessCameraProvider){
        cameraProvider.unbindAll()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val preview: Preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(binding.imageView.surfaceProvider)

        /* --------------- IMAGE CAPTURE USE CASE DECLARATION --------------- */
        var imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        /* --------------- IMAGE ANALYSIS USE CASE DECLARATION --------------- */
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(getExecutor(), this)

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((this as LifecycleOwner)!!, cameraSelector, preview, imageCapture, imageAnalysis )
    } // End of the function to initialize the camera. //

    /**
     *
     */
    override fun analyze(image: ImageProxy) {
        val bitmap: Bitmap? =
            binding.imageView.bitmap?.let { Bitmap.createScaledBitmap(it, 250, 450, false) }
        image.close()
        /* check for get a bitmap .*/
        if (bitmap == null) return
        /* Get for buffer image initialization. */
        if (!::bufferImage.isInitialized){
            bufferImage = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        }
        val height = bitmap.height; val width = bitmap.width
        val processor = ImageProcessor(bitmap)
        val binaryImage = processor.getFeatureMap()

        val puzzleCoords = PuzzleExtractor().getPuzzle(binaryImage, height, width)

        try {

            val puzzle = Bitmap.createBitmap(
                PerspectiveFixer.getFixedImage(puzzleCoords, binaryImage, width),
                //binaryImage,
                900,
                900,
                Bitmap.Config.RGB_565
            )


            runOnUiThread { binding.ivGrayView.setImageBitmap(puzzle) }
        }catch (e: Exception){
            Log.e("Error:", e.toString())
        }
    } // End of the process to analyze image.

}