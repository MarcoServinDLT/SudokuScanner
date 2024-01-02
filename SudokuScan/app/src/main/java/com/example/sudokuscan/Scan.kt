package com.example.sudokuscan

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.sudokuscan.databinding.ActivityScanBinding
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class Scan : AppCompatActivity(), ImageAnalysis.Analyzer {

    private lateinit var binding: ActivityScanBinding

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var bufferImage: Bitmap
    //private val processor = ImageProcessor(bufferImage)

    /**
     * Function on create
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
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
        }

    } // End of on create function. //

    override fun onDestroy() {
        /* Terminate all outstanding analyzing jobs (if there is any). */
        executor.apply {
            shutdown()
            awaitTermination(1000, TimeUnit.MILLISECONDS)
        }

        super.onDestroy()
    }

    private fun requestPermission(onPermission: () -> Unit) {
        requestCameraPermission {
            if(it) {
                onPermission()
            }
            else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCameraPermission(onResult: ((Boolean) -> Unit)) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            onResult(true)
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                onResult(it)
            }.launch(android.Manifest.permission.CAMERA)
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
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setMaxResolution(Size(250, 450))
            .build()

        /* --------------- IMAGE ANALYSIS USE CASE DECLARATION --------------- */
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(getExecutor(), this)

        //bind to lifecycle:
        cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
    } // End of the function to initialize the camera. //

    /**
     *
     */
    override fun analyze(image: ImageProxy) {
        val bitmap: Bitmap? =
            binding.imageView.bitmap?.let {
                Bitmap.createScaledBitmap(it, 150, 275, false)
            }
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

        val puzzleCoordinates = PuzzleExtractor().getPuzzle(binaryImage, height, width)

        val puzzle = Bitmap.createBitmap(
            PerspectiveFixer.getFixedImage(puzzleCoordinates, binaryImage, width),
            900,
            900,
            Bitmap.Config.RGB_565
        )
        runOnUiThread { binding.ivGrayView.setImageBitmap(puzzle) }

    } // End of the process to analyze image.

}