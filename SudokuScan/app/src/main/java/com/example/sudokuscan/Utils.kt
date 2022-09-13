package com.example.sudokuscan

import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 *
 */
fun ByteBuffer.toByteArray(): ByteArray {
    rewind()    // Rewind the buffer to zero
    val data = ByteArray(remaining())
    get(data)   // Copy the buffer into a byte array
    return data // Return the byte array
}