package com.example.sudokuscan

import androidx.versionedparcelable.ParcelImpl


class MathMatrix(private val n: Int = 0, private val m: Int = 0) {

    private lateinit var matrix: Array<DoubleArray>
    constructor(predMat: Array<DoubleArray>) : this(predMat.size, predMat[0].size){
        this.matrix = predMat
    }


    /**
     * Initialization of the matrix object.
     */
    init {
        if(n != 0 && m != 0)
            zeroes()
    }

    /**
     * Function to initialize the matrix as a zero matrix.
     */
    private fun zeroes() {
        matrix = Array(n) { DoubleArray(m) {0.0} }
    } // End fo zeros function. //

    /**
     * Function to convert the matrix to a matrix identity.
     */
    fun identity(){
        for(i in 0 until n)
            matrix[i][i] = 1.0
    } // End of the function to convert the matrix to identity. //

    /**
     * Function to access to an specific element fo the matrix.
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The element that was wanted.
     */
    operator fun get(row: Int, col: Int): Double = this.matrix[row][col]

    /**
     * Function to access to an specific row of a matrix.
     * @param row The row that you want.
     * @return The row of values.
     */
    operator fun get(row: Int): DoubleArray = this.matrix[row]

    /**
     * Function to set specific value on a matrix position.
     * @param row The row of the element.
     * @param col The column fo the element.
     * @param value The value that you want set on the matrix.
     */
    operator fun set(row: Int, col: Int, value: Double) {
        this.matrix[row][col] = value
    } // End of the set operator overloading. //

    /**
     * Function to overload multiplication on matrix object.
     * @param other The other matrix which this will be multiply.
     * @return The product matrix if the multiplication.
     */
    operator fun times(other: MathMatrix): MathMatrix{
        if(this.m != other.n)
            throw ArithmeticException("It is not possible to multiply two matrix with distinct row column relation.")
        var result = MathMatrix(this.m, other.n)
        for(i in 0 until n) {
            for(j in 0 until m)
                for(k in 0 until n)
                    result[i, j] += this.matrix[i][k] * other[k, j]
        }
        return result
    } // End of the overloading multiplication operator. //


    /**
     * Function to get the transpose of the matrix.
     * @return A MathMatrix with the transpose configuration.
     */
    fun transpose(): MathMatrix {
        var transpose = MathMatrix(this.n, this.m)
        for(c in 0 until  m)
            for(r in n-1 downTo 0)
                transpose[r, c] = this.matrix[c][r]
        return transpose
    } // End fo the function to get the matrix transpose. //

    /**
     * Function to make Gauss elimination on the matrix.
     * @param rvalues A matrix that represents the other side of the equation.
     * @return The result of the elimination.
     */
    fun gaussElimination(rvalues: MathMatrix): MathMatrix {
        for(step in 0 until n){
            rvalues[step] /= this.matrix[step][step]
            this.matrix[step] /= this.matrix[step][step]
            for(i in 0 until n){
                if(i != step) {
                    val factor = this.matrix[i][step]
                    for (j in 0 until m)
                        this.matrix[i][j] -= this.matrix[step][j] * factor
                    for (j in 0 until rvalues.m)
                        rvalues[i, j] -= rvalues[step, j] * factor
                }
            }
        } // End fo the steps cycle. //
        return rvalues
    } // End of the function to get the matrix elimination. //

    fun getLUMatrix(): Pair<MathMatrix, MathMatrix> {
        var l = MathMatrix(n,m)
        l.identity()
        var u = this
        for(step in 0 until n) {
            val denominator = u[step, step]
            for(i in step+1 until n) {
                val numerator = u[i, step]
                l[i, step] = numerator/denominator
                for (j in 0 until m)
                    u[i, j] -= u[step, j] * (numerator/denominator)
            }
        }
        return Pair(l, u)
    } // End of the function to get L and U matrices. //

    /**
     * Function to get the determinant of the matrix.
     * @return A double value that represents the determinant.
     */
    fun determinant(): Double{
        val luMatrices = getLUMatrix()
        var det = 1.0
        for(diagonal in 0 until n)
            det *= luMatrices.second[diagonal, diagonal]
        return det
    } // End of the function to calculate the determinant of the matrix.

    /**
     * Function to get the inverse of the matrix.
     * @return The inverse matrix.
     */
    fun inverse(): MathMatrix{
        val inv = MathMatrix(n, m)
        inv.identity()
        return gaussElimination(inv)
    } // End of the function to factorising the matrix. //

    fun printmatrix(){
        for(r in matrix){
            for(i in r)
                print(" ${i} ")
            println()
        }
    }

}