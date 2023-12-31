package com.example.sudokuscan

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

/**
 * Operator overloading extension to divide an entire array.
 * @param div The value that you want to divide the vector.
 */
operator fun DoubleArray.divAssign(div: Double) {
    for(col in indices)
        this[col] /= div
}