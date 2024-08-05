package cz.cuni.mff.mandelbrot;

/**
 * Holds variables and function used through numerous classes.
 *
 * @author Filip Cizmar
 * @version 1.0
 */
class Global {
    static int numberOfColors = 4096;
    private static int iterations = 150;
    static double zoomNorm = 10;
    private static double originSizeX = 3, originSizeY = 2;
    static double sizeX = originSizeX, sizeY = originSizeY;
    static double pointX = -0.5, pointY = 0;
    static int height, width;
    private static double itPow = 0.1;

    /**
     * Returns zoom in human readable integer.
     * It is used in the zoom text field.
     *
     * @return zoom representation
     */
    static long getZoom() {
        return (long) (originSizeX / sizeX);
    }

    /**
     * Sets size of drawn sector of the set.
     *
     * @param zoom integer representing zoom
     */
    static void setSize(long zoom) {
        sizeX = originSizeX / zoom;
        sizeY = originSizeY / zoom;
    }

    /**
     * Returns number of iteration needed for clear coloring.
     * Depends only on actual size of sector.
     *
     * @return number of iterations needed
     */
    static int countIterations() {
        return (int) (iterations / Math.pow(sizeX / originSizeX, itPow));
    }

    /**
     * Compute iterations needed for clear coloring.
     * 
     * @param zoom new value of zoom
     * @return number of iterations needed
     */
    static int countIterations(long zoom) {
        return (int) (iterations / Math.pow(1.0 / zoom, itPow));
    }

    /**
     * Sets iteration according the value given in the iteration text field.
     *
     * @param newIterations custom value of iteration
     */
    static void setIterations(int newIterations) {
        iterations = (int) (Math.pow(sizeX / originSizeX, itPow) * newIterations);
    }

    /**
     * @return base iterations
     */
    static int getIterations() {
        return iterations;
    }

    /**
     * If zoom out of bounds the sector sizes are sets to default values.
     */
    static void resetSize() {
        sizeX = originSizeX;
        sizeY = originSizeY;
    }
}
