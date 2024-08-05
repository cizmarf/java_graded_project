package cz.cuni.mff.mandelbrot;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

/**
 * Represents the set and compute values of each point.
 * Provides multithreading.
 *
 * @author Filip Cizmar
 * @version 1.0
 */
class Set {

    /**
     * This class represents one thread of computation of array of points.
     *
     * @author Filip Cizmar
     * @version 1.0
     */
    static class ThreadSet implements Runnable {

        /**
         * Sets static variables.
         *
         * @param start first column
         * @param end   last colunm
         * @param data  array of points
         */
        ThreadSet(int start, int end, double[][] data) {
            startData = start;
            endData = end;
            ThreadSet.startPointX = Global.pointX - Global.sizeX / 2;
            ThreadSet.startPointY = Global.pointY + Global.sizeY / 2;
            ThreadSet.data = data;
        }

        /**
         * Main method runs the thread.
         * Computes value of all points in given range. For antialiasing
         * it computes the value 4 times in slightly shifted positions.
         */
        @Override
        public void run() {
            int iterations = Global.countIterations();

            for (int i = startData; i < endData; ++i) {
                double[] tempData = new double[Global.height];
                for (int j = 0; j < Global.height; ++j) {
                    double gnu = 0.0;

                    gnu += comPoint(i, j, 0.2, 0.2, iterations, tempData);
                    gnu += comPoint(i, j, 0.2, 0.8, iterations, tempData);
                    gnu += comPoint(i, j, 0.8, 0.2, iterations, tempData);
                    gnu += comPoint(i, j, 0.8, 0.8, iterations, tempData);

                    tempData[j] = ((long) tempData[j] / 4) + 1 - gnu / 4;
                }
                data[i] = tempData;
            }
        }

        /**
         * Computes the exact value of point.
         * Implements standard algorithm for computation of mandelbrot set.
         *
         * @param i          x position of point in array
         * @param j          y position of point in array
         * @param tx         shift in x axis
         * @param ty         shift in y axis
         * @param iterations number of iteration to use
         * @param tempData   template array for storing results
         * @return this value is used in linear approximation of color
         */
        private static double comPoint(int i, int j, double tx, double ty,
                int iterations, double[] tempData) {
            double x0, y0;
            x0 = ((i + tx) / Global.width) * Global.sizeX + startPointX;
            y0 = ((j + ty) / Global.height) * Global.sizeY - startPointY;
            double mx = 0, my = 0, px;
            long n = 0;
            while (n < iterations && mx * mx + my * my < 4) {
                px = mx * mx - my * my + x0;
                my = 2 * mx * my + y0;
                mx = px;
                n++;
            }
            double nu = 1;
            if (n < iterations) {
                double log_zn = log(mx * mx + my * my) / 2;
                nu = log(log_zn / log(2)) / log(2);
            }
            tempData[j] += n;
            return nu;
        }

        private int startData, endData;
        private static double startPointX, startPointY;
        private static double[][] data;
    }

    /**
     * Initials array of points and starts computation.
     * A new array is constructed due to it is ready to provides resizing of canvas
     * in GUI.
     */
    static void setConstruct() {
        data = new double[Global.width][Global.height];
        fillCanvas();
    }

    /**
     * Return the value of pixel.
     * If given coordinates is out of array it returns -1 as error code.
     *
     * @param i x coordinates
     * @param j y coordinates
     * @return value of point at given coordinates
     */
    static double getValueError(int i, int j) {
        if (i < 0 || i >= Global.width || j < 0 || j >= Global.height)
            return -1;
        return data[i][j];
    }

    /**
     * Return the value of pixel.
     *
     * @param i x coordinates
     * @param j y coordinates
     * @return value of point at given coordinates
     */
    static double getValue(int i, int j) {
        return data[i][j];
    }

    /**
     * Stops the all threads processing the task.
     */
    static void killThreads() {
        executor.shutdownNow();
    }

    /**
     * Main method manages distribution of tasks.
     * It runs thread using the ThreadPoolExecutor and create thread that computes
     * values of point in given range.
     */
    private static void fillCanvas() {
        int subArraysForOneThread = 30;
        int threadsCount = Runtime.getRuntime().availableProcessors();

        executor = new ThreadPoolExecutor(threadsCount + 4,
                threadsCount + threadsCount, 1,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (int i = 0; i < Global.width; i += subArraysForOneThread) {
            int end = i + subArraysForOneThread;
            if (end > Global.width)
                end = Global.width;
            executor.submit(new ThreadSet(i, end, data));
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            Thread.yield();
        }
    }

    private static ThreadPoolExecutor executor;
    private static double[][] data;
}
