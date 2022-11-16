package com.example.sudokuscan

import android.graphics.*
import android.util.Log
import androidx.core.graphics.get
import java.lang.Math.*
import kotlin.math.pow


class ImageProcessor (private var image: Bitmap, private val noiseSuppression: Boolean = false){

    private val width = image.width
    private val height = image.height
    private var filterBuffer = IntArray(width * height)
    private lateinit var featureMap: Bitmap
    /* A default Gaussian kernel with 5 of radius. */
    private val kernel  = floatArrayOf(
        0.0030F, 0.0133F, 0.0219F, 0.0133F, 0.0030F,
        0.0133F, 0.0596F, 0.0983F, 0.0596F, 0.0133F,
        0.0219F, 0.0983F, 0.1621F, 0.0983F, 0.0219F,
        0.0133F, 0.0596F, 0.0983F, 0.0596F, 0.0133F,
        0.0030F, 0.0133F, 0.0219F, 0.0133F, 0.0030F
    )
    private val kernelRadius = 5

    /**
     * Class initialization.
     */
    init { toGrayScale() }

    /**
     * Function to convert the image to gray
     */
    private fun toGrayScale(){
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(image, 0f, 0f, paint)
        image = bmpGrayscale
    }

    /**
     *  Convert a 32 bits Integer representation of RGBA pixel of image in one value of the
     *  limits of 8 bits, to represent the binary value of the pixel.
     *  @param pixel An integer of 32 bits that represents any RGBA pixel.
     *  @param threshold An integer that represents the limit that define the value
     *  of the pixel, bigger than threshold inactive (remove noise), otherwise active.
     *  @return One value 255 or 0 as a binary representation of color.
     */
    private fun pixelToBin(pixel: Int, blurredPixel:Int, threshold: Int): Int {
        val repPixel = Color.green(pixel)
        val repBlurPixel = Color.green(blurredPixel)
        return if(repBlurPixel - repPixel > threshold) 0xffffff else 0
    } // End of function to  convert to binary a RGBA pixel. //

    /**
     *  Function to check if a coordinate in a bitmap is in bounds of the
     *  height of the image.
     *  @param y An int value that represent the row that you want check in the
     *  image.
     *  @return A boolean value that denotes if the coordinate its inside the image.
     */
    private fun insideOfBoundsY(y: Int): Boolean = (y in 0 until height )

    /**
     *  Function to check if a coordinate in a bitmap is in bounds of the
     *  width of the image.
     *  @param x An int value that represent the column that you want check in the
     *  image.
     *  @return A boolean value that denotes if the coordinate its inside the image.
     */
    private fun insideOfBoundsX(x: Int): Boolean = ( x in 0 until width )

    /**
     * Function to convert any bitmap into black and withe image.
     */
    private fun featureExtraction(){
        val blurImage = boxBlur(20)
        for(r in 0 until height)
            for(c in 0 until width)
                pixelToBin(image.getPixel(c, r), blurImage[r * width + c], 20).also {
                    filterBuffer[r * width + c] = it
                }
        //filterBuffer = blurImage
    } // End of the function to binarization an image. //

    /**
     * Function to calculate a prefix table.
     * @return An int array that represent the prefix sum table.
     */
    private fun getPrefixTable(): IntArray{
        var table = IntArray(height * width)
        var src = 0; var dst = 0
        for(row in 0 until height){
            for(col in 0 until width){
                var pref = Color.green(image[col, row])
                if(col > 0) pref += table[dst - 1]
                if(row > 0) pref += table[dst - width]
                if(row > 0 && col > 0) pref -= table[dst - width -1]
                table[dst] = pref
                src++; dst++
            } // End for the column cycle. //
        } // End for the row cycle. //
        return table
    } // End of the function to get the prefix table. //

    /**
     * Function to apply box blur to image, reducing the noise of the image
     * setting a average of a region of he pixels.
     * @param radius the area around of the pixel with the average has been
     * calculated.
     */
    private fun boxBlur(radius: Int): IntArray{
        val prefPixelSum = getPrefixTable()
        var blurredImage = IntArray(height * width)
        /* function to get the prefix sum without indexes problem. */
        val sumByPrefix = {row : Int, col: Int ->
            val y = if(row <= 0) 0 else row-1
            val x = if(col <= 0) 0 else col-1
            prefPixelSum[y * width + x]
        }
        for(row in 0 until height){
            for(col in 0 until width){
                /* minimum and maximum indexes for the box. */
                val minCol = 0.coerceAtLeast(col - radius); val maxCol = (width-1).coerceAtMost(col + radius)
                val minRow = 0.coerceAtLeast(row - radius); val maxRow = (height-1).coerceAtMost(row + radius)
                val denominator = (maxCol - minCol) * (maxRow - minRow)
                var pixelSum = sumByPrefix(maxRow, maxCol) +
                        sumByPrefix(minRow, minCol) -
                        sumByPrefix(maxRow, minCol) -
                        sumByPrefix(minRow, maxCol)
                val average = (pixelSum / denominator).toInt() // Total average value of the box. //
                val pixel = -0x1000000 or   // Alpha value. //
                        average shl 16 or   // Red value. //
                        average shl 8 or    // Green value. //
                        average             // Blue value. //
                blurredImage[row * width + col] = pixel
            } // End of the column cycle. //
        } // End of the row cycle. //
        return blurredImage
    } // End of the box blur function. //

    /**
     * Function to apply gaussian blur to images and reduces noise to
     * improve the seek features task.
     */
    private fun gaussianBlur(): IntArray{
        val blurredImage = IntArray(height*width)
        for(row in 0 until height){
            for(col in 0 until width){
                var red = 0F; var green = 0F; var blue = 0F
                var denominator  = 0F
                /* Apply the kernel to the image to get the pixel. */
                for(k in kernel.indices){
                    /* getting the pixel coordinate. */
                    val tmpRow = row - (kernelRadius /2) + (k / kernelRadius)
                    val tmpCol = col - (kernelRadius /2) + (k % kernelRadius)
                    /* check if can calculate that pixel. */
                    if( insideOfBoundsY(tmpRow) and insideOfBoundsX(tmpCol) ){
                        val tmpPixel = image.getPixel(tmpCol, tmpRow)
                        red += Color.red(tmpPixel) * kernel[k]
                        green += Color.green(tmpPixel) * kernel[k]
                        blue += Color.blue(tmpPixel) * kernel[k]
                        denominator += kernel[k]
                    }
                } // End of the applying of kernel. //
                // Get an int value in pixel format
                val pixel = -0x1000000 or                    // Alpha value. //
                        (red/denominator).toInt() shl 16 or  // Red value. //
                        (green/denominator).toInt() shl 8 or // Green value. //
                        (blue/denominator).toInt()           // Blue value. //
                blurredImage[row * width + col] = pixel
            } // End of the columns cycle. //
        } // End of the rows cycle. //
        return blurredImage
    } // End fo the function to apply gaussian blur. //

    /**
     * Function to get the image with the features extracted.
     * @return A bitmap in black and white format with the features.
     */
    fun getFeatureMap(): IntArray {
        featureExtraction()
        return filterBuffer
    }

} // End of the class. //