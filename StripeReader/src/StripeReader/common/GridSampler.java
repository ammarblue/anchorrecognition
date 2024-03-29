package StripeReader.common;

import StripeReader.core.ReaderException;

public abstract class GridSampler {

    private static GridSampler gridSampler = new DefaultGridSampler();

    /**
     * Sets the implementation of {@link GridSampler} used by the library. One global
     * instance is stored, which may sound problematic. But, the implementation provided
     * ought to be appropriate for the entire platform, and all uses of this library
     * in the whole lifetime of the JVM. For instance, an Android activity can swap in
     * an implementation that takes advantage of native platform libraries.
     *
     * @param newGridSampler The platform-specific object to install.
     */
    public static void setGridSampler(GridSampler newGridSampler) {
        if (newGridSampler == null) {
            throw new IllegalArgumentException();
        }
        gridSampler = newGridSampler;
    }

    /**
     * @return the current implementation of {@link GridSampler}
     */
    public static GridSampler getInstance() {
        return gridSampler;
    }

    /**
     * <p>Samples an image for a square matrix of bits of the given dimension. This is used to extract
     * the black/white modules of a 2D barcode like a QR Code found in an image. Because this barcode
     * may be rotated or perspective-distorted, the caller supplies four points in the source image
     * that define known points in the barcode, so that the image may be sampled appropriately.</p>
     *
     * <p>The last eight "from" parameters are four X/Y coordinate pairs of locations of points in
     * the image that define some significant points in the image to be sample. For example,
     * these may be the location of finder pattern in a QR Code.</p>
     *
     * <p>The first eight "to" parameters are four X/Y coordinate pairs measured in the destination
     * {@link BitMatrix}, from the top left, where the known points in the image given by the "from"
     * parameters map to.</p>
     *
     * <p>These 16 parameters define the transformation needed to sample the image.</p>
     *
     * @param image image to sample
     * @param dimension width/height of {@link BitMatrix} to sample from image
     * @return {@link BitMatrix} representing a grid of points sampled from the image within a region
     *   defined by the "from" parameters
     * @throws ReaderException if image can't be sampled, for example, if the transformation defined
     *   by the given points is invalid or results in sampling outside the image boundaries
     */
    public abstract BitMatrix sampleGrid(BitMatrix image,
            int dimension,
            float p1ToX, float p1ToY,
            float p2ToX, float p2ToY,
            float p3ToX, float p3ToY,
            float p4ToX, float p4ToY,
            float p1FromX, float p1FromY,
            float p2FromX, float p2FromY,
            float p3FromX, float p3FromY,
            float p4FromX, float p4FromY) throws ReaderException;

    /**
     * <p>Checks a set of points that have been transformed to sample points on an image against
     * the image's dimensions to see if the point are even within the image.</p>
     *
     * <p>This method will actually "nudge" the endpoints back onto the image if they are found to be
     * barely (less than 1 pixel) off the image. This accounts for imperfect detection of finder
     * patterns in an image where the QR Code runs all the way to the image border.</p>
     *
     * <p>For efficiency, the method will check points from either end of the line until one is found
     * to be within the image. Because the set of points are assumed to be linear, this is valid.</p>
     *
     * @param image image into which the points should map
     * @param points actual points in x1,y1,...,xn,yn form
     * @throws ReaderException if an endpoint is lies outside the image boundaries
     */
    protected static void checkAndNudgePoints(BitMatrix image, float[] points)
            throws ReaderException {
        int width = image.getWidth();
        int height = image.getHeight();
        // Check and nudge points from start until we see some that are OK:
        boolean nudged = true;
        for (int offset = 0; offset < points.length && nudged; offset += 2) {
            int x = (int) points[offset];
            int y = (int) points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw ReaderException.getInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = width - 1;
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
            } else if (y == height) {
                points[offset + 1] = height - 1;
                nudged = true;
            }
        }
        // Check and nudge points from end:
        nudged = true;
        for (int offset = points.length - 2; offset >= 0 && nudged; offset -= 2) {
            int x = (int) points[offset];
            int y = (int) points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw ReaderException.getInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = width - 1;
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
            } else if (y == height) {
                points[offset + 1] = height - 1;
                nudged = true;
            }
        }
    }
}
