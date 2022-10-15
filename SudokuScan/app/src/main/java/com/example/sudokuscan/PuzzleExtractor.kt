package com.example.sudokuscan

import java.util.*
import kotlin.math.abs

class PuzzleExtractor (
    private var bytes: Array<IntArray>

        ){

    private var h = 0
    private var w = 0
    private var puzzle = ConnectedElement()

    /**
     * Class to set the largest connected region in the image.
     */
    inner class ConnectedElement : Comparable<ConnectedElement>{
        var maxX = 0
        var maxY = 0
        var minX = bytes[0].size
        var minY = bytes.size

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
        //var region: ConnectedElement()
        var nodes = Stack<Coordinate>()
        val region = ConnectedElement()
        nodes.push(Coordinate(x, y))
        /* backtrack on the pixels to know connected element. */
        while(!nodes.empty()){
            val coord = nodes.pop()
            region.addPixelToRegion(coord)
            bytes[coord.y][coord.x] = 0
            val minX = 0.coerceAtLeast(coord.x - 1); val maxX = w.coerceAtMost(coord.x+1)
            val minY = 0.coerceAtLeast(coord.y - 1); val maxY = h.coerceAtMost(coord.y+1)
            for(y in minY until maxY){
                for(x in minX until maxX) {
                    val active: Boolean = (bytes[y][x] == 255)
                    if (active)
                        nodes.push(Coordinate(x, y))
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
        var x = coordinate.x; var y = coordinate.y
        var closest = Coordinate(x, y)
        val horizontal = if(x == puzzle.minX) (puzzle.minX until puzzle.maxX) else (puzzle.maxX downTo puzzle.minX)
        val vertical = if(y == puzzle.minY) (puzzle.minY until puzzle.maxY) else (puzzle.maxY downTo puzzle.minY)
        /* checking for vertical corner. */
        for(hx in horizontal) {
            val current = Coordinate(hx, y)
            closest = if (manhattanDistance(current, coordinate) < manhattanDistance(closest, coordinate))
                current else
                closest
        }
        /* checking for horizontal corner. */
        for(vy in vertical) {
            val current = Coordinate(x, vy)
            closest = if (manhattanDistance(current, coordinate) < manhattanDistance(closest, coordinate))
                current else
                closest
        }
        return Coordinate(x,y)
    } // End of the manhattan distance function. //

    /**
     * Function to get the largest connected region of the image, which
     * supposed to be the puzzle.
     * @return Return the coordinates of the puzzle.
     */
    fun getPuzzle(): RegionCorners{
        var puzzle: ConnectedElement = ConnectedElement()
        for(y in bytes.indices){
            for(x in 0 until bytes[0].size) {
                if(bytes[x][y] == 255) {
                    val region = depthSearch(x, y)
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