package com.example.sudokuscan

import android.util.Log
import kotlin.math.floor

object PerspectiveFixer {

    private var h = MathMatrix(8, 1)
    private var b = MathMatrix(8, 1)
    private var A = MathMatrix(8, 8)
    private var homography = MathMatrix(3,3)
    private const val size = 900.0
    private const val dims = size.toInt()
    private val fixedImage = IntArray( dims * dims )

    /**
     * Set up matrices of the object.
     */
    private fun setup(corners: RegionCorners) {
        /* definition of b values. */
        b[0,0] = corners.topLeft.x.toDouble()
        b[1,0] = corners.topLeft.y.toDouble()
        b[2,0] = corners.topRight.x.toDouble()
        b[3,0] = corners.topRight.y.toDouble()
        b[4,0] = corners.bottomLeft.x.toDouble()
        b[5,0] = corners.bottomLeft.y.toDouble()
        b[6,0] = corners.bottomRight.x.toDouble()
        b[7,0] = corners.bottomRight.y.toDouble()

        /* definition of A values. */
        A[0, 2] = 1.0
        A[1, 5] = 1.0
        A[2, 0] = size; A[2, 2] = 1.0; A[2, 6] = -size * corners.topRight.x
        A[3, 3] = size; A[3, 5] = 1.0; A[3, 6] = -size * corners.topRight.y
        A[4, 1] = size; A[4, 2] = 1.0; A[4, 7] = -size * corners.bottomLeft.x
        A[5, 4] = size; A[5, 5] = 1.0; A[5, 7] = -size * corners.bottomLeft.y
        A[6, 0] = size; A[6, 1] = size; A[6, 2] = 1.0; A[6, 6] = -size * corners.bottomRight.x; A[6, 7] = -size * corners.bottomRight.x
        A[7, 3] = size; A[7, 4] = size; A[7, 5] = 1.0; A[7, 6] = -size * corners.bottomRight.y; A[7, 7] = -size * corners.bottomRight.y

        val aux = A.transpose() * A
        h = ( aux.inverse() * A.transpose() ) * b

        homography[0,0] = h[0,0]; homography[0,1] = h[1,0]; homography[0,2] = h[2,0]
        homography[1,0] = h[3,0]; homography[1,1] = h[4,0]; homography[1,2] = h[5,0]
        homography[2,0] = h[6,0]; homography[2,1] = h[7,0]; homography[2,2] = 1.0

        Log.d("matrix", "${h[0,0]} | ${h[1,0]} | ${h[2,0]} | ${h[3,0]} | ${h[4,0]} | ${h[5,0]} | ${h[6,0]} | ${h[7,0]}")

    } // End of the object initialization. //


    /**
     * Function to fix the image orientation and perspective
     */
    fun getFixedImage(corners: RegionCorners, image: IntArray, width: Int): IntArray{
        setup(corners)
        for(row in 0 until dims) {
            val sxPre1 = homography[0,1] * row + homography[0,2]
            val sxPre2 = homography[2,1] * row + 1
            val syPre1 = homography[1,1] * row + homography[1,2]
            val syPre2 = homography[2,1] * row + 1
            for (col in 0 until  dims){
                val sx = floor((homography[0,0] * col + sxPre1) / (homography[2,0] * col + sxPre2))
                val sy = floor((homography[1,0] * col + syPre1) / (homography[2,0] * col + syPre2))
                fixedImage[row * dims + col] = image[(sy * width + sx).toInt()]
            }
        }
        return fixedImage
    } // End of the function to fix the image. //

} // End of the class to fix perspective. //