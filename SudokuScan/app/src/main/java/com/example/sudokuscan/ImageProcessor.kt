package com.example.sudokuscan

import android.graphics.*
import androidx.core.graphics.get


class ImageProcessor (private var image: Bitmap){

    private val width = image.width
    private val height = image.height
    private var filterBuffer = IntArray(width * height)
    private val threshold = 20
    private val radius = 20

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
     *  of the pixel, bigger than this.threshold inactive (remove noise), otherwise active.
     *  @return One value 255 or 0 as a binary representation of color.
     */
    private fun pixelToBin(pixel: Int, blurredPixel:Int): Int {
        val repPixel = Color.green(pixel)
        val repBlurPixel = Color.green(blurredPixel)
        return if(repBlurPixel - repPixel > this.threshold) 0xffffff else 0
    } // End of function to  convert to binary a RGBA pixel. //


    /**
     * Function to convert any bitmap into black and withe image.
     */
    private fun featureExtraction(){
        val blurImage = boxBlur()
        for(r in 0 until height)
            for(c in 0 until width)
                pixelToBin(image.getPixel(c, r), blurImage[r * width + c]).also {
                    filterBuffer[r * width + c] = it
                }
        //filterBuffer = blurImage
    } // End of the function to binarization an image. //

    /**
     * Function to calculate a prefix table.
     * @return An int array that represent the prefix sum table.
     */
    private fun getPrefixTable(): IntArray{
        val table = IntArray(height * width)
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
     * calculated.
     */
    private fun boxBlur(): IntArray{
        val prefPixelSum = getPrefixTable()
        val blurredImage = IntArray(height * width)
        /* function to get the prefix sum without indexes problem. */
        val sumByPrefix = {row : Int, col: Int ->
            val y = if(row <= 0) 0 else row-1
            val x = if(col <= 0) 0 else col-1
            prefPixelSum[y * width + x]
        }
        for(row in 0 until height){
            for(col in 0 until width){
                /* minimum and maximum indexes for the box. */
                val minCol = 0.coerceAtLeast(col - this.radius); val maxCol = (width-1).coerceAtMost(col + radius)
                val minRow = 0.coerceAtLeast(row - this.radius); val maxRow = (height-1).coerceAtMost(row + radius)
                val denominator = (maxCol - minCol) * (maxRow - minRow)
                val pixelSum = sumByPrefix(maxRow, maxCol) +
                        sumByPrefix(minRow, minCol) -
                        sumByPrefix(maxRow, minCol) -
                        sumByPrefix(minRow, maxCol)
                val average = pixelSum / denominator // Total average value of the box. //
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
     * Function to get the image with the features extracted.
     * @return A bitmap in black and white format with the features.
     */
    fun getFeatureMap(): IntArray {
        featureExtraction()
        return filterBuffer
    }

} // End of the class. //