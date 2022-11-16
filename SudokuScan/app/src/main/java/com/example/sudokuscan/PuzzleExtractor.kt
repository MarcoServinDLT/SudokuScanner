package com.example.sudokuscan

import android.util.Log
import java.security.PrivateKey
import java.util.*
import kotlin.math.abs

class PuzzleExtractor {

    private lateinit var bytes: IntArray
    private lateinit var copy: IntArray
    private var h = 0
    private var w = 0
    private var puzzle = ConnectedElement()


    /**
     * Class to set the largest connected region in the image.
     */
    inner class ConnectedElement : Comparable<ConnectedElement>{
        var maxX = 0
        var maxY = 0
        var minX = w
        var minY = h

        /**
         * Function to set new pixel coordinate to connected element and update the
         * values of the size.
         * @param coordinate The coordinates of the new pixel.
         */
        fun addPixelToRegion(coordinate: Coordinate){
            this.maxX = this.maxX.coerceAtLeast(coordinate.x)
            this.minX = this.minX.coerceAtMost(coordinate.x)
            this.maxY = this.maxY.coerceAtLeast(coordinate.y)
            this.minY = this.minY.coerceAtMost(coordinate.y)
        } // End of the function to set new pixel to the region. //

        /**
         * Compare operator overloading to check which connected region is larger
         * each other.
         * @param other The other connected region which will be compare.
         * @return A boolean value that indicate if the left
         */
        override operator fun compareTo(other: ConnectedElement): Int {
            val ownPerimeter = 2 * (this.maxX - this.minX) + 2 * (this.maxY - this.minY)
            val otherPerimeter = 2 * (other.maxX - other.minX) + 2 * (other.maxY - other.minY)
            return if (ownPerimeter > otherPerimeter) 1 else 0
        } // End of the compare operator overloading. //

    } // End of the  connected element class. //

    /**
     * Function to search a connected element.
     * @param x The column of the image.
     * @param y The row of the image.
     * @return A connected element object of all the region connected.
     */
    private fun depthSearch(x: Int, y: Int): ConnectedElement{
        val nodes = Stack<Coordinate>()
        val region = ConnectedElement()
        nodes.push(Coordinate(x, y))
        /* backtrack on the pixels to know connected element. */
        while(!nodes.empty()){
            val coord = nodes.pop()
            region.addPixelToRegion(coord)
            val minX = 0.coerceAtLeast(coord.x - 1); val maxX = w.coerceAtMost(coord.x+2)
            val minY = 0.coerceAtLeast(coord.y - 1); val maxY = h.coerceAtMost(coord.y+2)
            for(row in minY until maxY){
                for(col in minX until maxX) {
                    val active: Boolean = (copy[row * w + col] != 0)
                    if (active) {
                        nodes.push(Coordinate(col, row))
                        copy[row * w + col] = 0
                    }
                } // End of the cycle
            }
        } // End of the stack backtrack cycle. //
        return region
    } // End of the function of the depth search. //

    /**
     * Function to get the manhattan distance of two points at the image.
     * @param
     */
    private fun manhattanDistance(pointA: Coordinate, pointB: Coordinate) =
        abs(pointA.x - pointB.x) + abs(pointA.y - pointB.y)

    /**
     * Function to get the exact corner coordinate of the puzzle.
     *
     */
    private fun getCorner(coordinate: Coordinate): Coordinate {
        val x = coordinate.x; val y = coordinate.y
        val midX = (puzzle.minX + puzzle.maxX); val midY = (puzzle.minY + puzzle.maxY)
        var closest = Coordinate(midX, midY)
        val horizontal = if(x == puzzle.minX) (puzzle.minX until puzzle.maxX) else (puzzle.maxX-1 downTo puzzle.minX)
        val vertical = if(y == puzzle.minY) (puzzle.minY until puzzle.maxY) else (puzzle.maxY-1 downTo puzzle.minY)
        /* checking for vertical corner. */
        for(hx in horizontal) {
            val current = Coordinate(hx, y)
            if(bytes[y * w + hx] != 0)
                closest = if (manhattanDistance(current, coordinate) < manhattanDistance(closest, coordinate))
                    current else closest
        }
        /* checking for horizontal corner. */
        for(vy in vertical) {
            val current = Coordinate(x, vy)
            if(bytes[vy * w + x] != 0)
                closest = if (manhattanDistance(current, coordinate) < manhattanDistance(closest, coordinate))
                    current else closest
        }
        return closest
    } // End of the manhattan distance function. //

    /**
     * Function to get the largest connected region of the image, which
     * supposed to be the puzzle.
     * @return Return the coordinates of the puzzle.
     */
    fun getPuzzle(image: IntArray, height: Int, width: Int): RegionCorners{
        bytes = image; copy = image.clone()
        Log.d("images","${bytes.toString()} ${copy.toString()}")
        h = height; w = width
        for(row in 0 until h){
            for(col in 0 until w) {
                if(copy[row * w + col] != 0) {
                    val region = depthSearch(col, row)
                    puzzle = if(region > puzzle) region else puzzle
                }
            } // End of the row cycle. //
        } // End of the rows cycle. //
        return RegionCorners(
            getCorner( Coordinate(puzzle.minX, puzzle.minY) ),
            getCorner( Coordinate(puzzle.maxX, puzzle.minY) ),
            getCorner( Coordinate(puzzle.minX, puzzle.maxY) ),
            getCorner( Coordinate(puzzle.maxX, puzzle.maxY) )
        )
    } // End of the get puzzle function. //

} // End of the puzzle extractor class. //