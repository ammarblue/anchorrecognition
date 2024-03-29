package StripeReader.barcodetypes.qrcode.detector;

import StripeReader.core.ReaderException;
import StripeReader.core.ResultPoint;
import StripeReader.common.BitMatrix;
import StripeReader.common.DetectorResult;
import StripeReader.common.GridSampler;
import StripeReader.barcodetypes.qrcode.decoder.Version;

import java.util.Hashtable;

public class Detector {

    private final BitMatrix image;

    public Detector(BitMatrix image) {
        this.image = image;
    }

    protected BitMatrix getImage() {
        return image;
    }

    /**
     * <p>Detects a QR Code in an image, simply.</p>
     *
     * @return {@link DetectorResult} encapsulating results of detecting a QR Code
     * @throws ReaderException if no QR Code can be found
     */
    public DetectorResult detect() throws ReaderException {
        return detect(null);
    }

    /**
     * <p>Detects a QR Code in an image, simply.</p>
     *
     * @param hints optional hints to detector
     * @return {@link DetectorResult} encapsulating results of detecting a QR Code
     * @throws ReaderException if no QR Code can be found
     */
    public DetectorResult detect(Hashtable hints) throws ReaderException {

        FinderPatternFinder finder = new FinderPatternFinder(image);
        FinderPatternInfo info = finder.find(hints);

        return processFinderPatternInfo(info);
    }

    protected DetectorResult processFinderPatternInfo(FinderPatternInfo info) throws ReaderException {

        FinderPattern topLeft = info.getTopLeft();
        FinderPattern topRight = info.getTopRight();
        FinderPattern bottomLeft = info.getBottomLeft();

        float moduleSize = calculateModuleSize(topLeft, topRight, bottomLeft);
        if (moduleSize < 1.0f) {
            throw ReaderException.getInstance();
        }
        int dimension = computeDimension(topLeft, topRight, bottomLeft, moduleSize);
        Version provisionalVersion = Version.getProvisionalVersionForDimension(dimension);
        int modulesBetweenFPCenters = provisionalVersion.getDimensionForVersion() - 7;

        AlignmentPattern alignmentPattern = null;
        // Anything above version 1 has an alignment pattern
        if (provisionalVersion.getAlignmentPatternCenters().length > 0) {

            // Guess where a "bottom right" finder pattern would have been
            float bottomRightX = topRight.getX() - topLeft.getX() + bottomLeft.getX();
            float bottomRightY = topRight.getY() - topLeft.getY() + bottomLeft.getY();

            // Estimate that alignment pattern is closer by 3 modules
            // from "bottom right" to known top left location
            float correctionToTopLeft = 1.0f - 3.0f / (float) modulesBetweenFPCenters;
            int estAlignmentX = (int) (topLeft.getX() + correctionToTopLeft * (bottomRightX - topLeft.getX()));
            int estAlignmentY = (int) (topLeft.getY() + correctionToTopLeft * (bottomRightY - topLeft.getY()));

            // Kind of arbitrary -- expand search radius before giving up
            for (int i = 4; i <= 16; i <<= 1) {
                try {
                    alignmentPattern = findAlignmentInRegion(moduleSize,
                            estAlignmentX,
                            estAlignmentY,
                            (float) i);
                    break;
                } catch (ReaderException re) {
                    // try next round
                }
            }
            // If we didn't find alignment pattern... well try anyway without it
        }

        BitMatrix bits = sampleGrid(image, topLeft, topRight, bottomLeft, alignmentPattern, dimension);

        ResultPoint[] points;
        if (alignmentPattern == null) {
            points = new ResultPoint[]{bottomLeft, topLeft, topRight};
        } else {
            points = new ResultPoint[]{bottomLeft, topLeft, topRight, alignmentPattern};
        }
        return new DetectorResult(bits, points);
    }

    private static BitMatrix sampleGrid(BitMatrix image,
            ResultPoint topLeft,
            ResultPoint topRight,
            ResultPoint bottomLeft,
            ResultPoint alignmentPattern,
            int dimension) throws ReaderException {
        float dimMinusThree = (float) dimension - 3.5f;
        float bottomRightX;
        float bottomRightY;
        float sourceBottomRightX;
        float sourceBottomRightY;
        if (alignmentPattern != null) {
            bottomRightX = alignmentPattern.getX();
            bottomRightY = alignmentPattern.getY();
            sourceBottomRightX = sourceBottomRightY = dimMinusThree - 3.0f;
        } else {
            // Don't have an alignment pattern, just make up the bottom-right point
            bottomRightX = (topRight.getX() - topLeft.getX()) + bottomLeft.getX();
            bottomRightY = (topRight.getY() - topLeft.getY()) + bottomLeft.getY();
            sourceBottomRightX = sourceBottomRightY = dimMinusThree;
        }

        GridSampler sampler = GridSampler.getInstance();
        return sampler.sampleGrid(
                image,
                dimension,
                3.5f,
                3.5f,
                dimMinusThree,
                3.5f,
                sourceBottomRightX,
                sourceBottomRightY,
                3.5f,
                dimMinusThree,
                topLeft.getX(),
                topLeft.getY(),
                topRight.getX(),
                topRight.getY(),
                bottomRightX,
                bottomRightY,
                bottomLeft.getX(),
                bottomLeft.getY());
    }

    /**
     * <p>Computes the dimension (number of modules on a size) of the QR Code based on the position
     * of the finder patterns and estimated module size.</p>
     */
    private static int computeDimension(ResultPoint topLeft,
            ResultPoint topRight,
            ResultPoint bottomLeft,
            float moduleSize) throws ReaderException {
        int tltrCentersDimension = round(ResultPoint.distance(topLeft, topRight) / moduleSize);
        int tlblCentersDimension = round(ResultPoint.distance(topLeft, bottomLeft) / moduleSize);
        int dimension = ((tltrCentersDimension + tlblCentersDimension) >> 1) + 7;
        switch (dimension & 0x03) { // mod 4
            case 0:
                dimension++;
                break;
            // 1? do nothing
            case 2:
                dimension--;
                break;
            case 3:
                throw ReaderException.getInstance();
        }
        return dimension;
    }

    /**
     * <p>Computes an average estimated module size based on estimated derived from the positions
     * of the three finder patterns.</p>
     */
    private float calculateModuleSize(ResultPoint topLeft, ResultPoint topRight,
            ResultPoint bottomLeft) throws ReaderException {
        // Take the average
        return (calculateModuleSizeOneWay(topLeft, topRight) +
                calculateModuleSizeOneWay(topLeft, bottomLeft)) / 2.0f;
    }

    /**
     * <p>Estimates module size based on two finder patterns -- it uses
     * {@link #sizeOfBlackWhiteBlackRunBothWays(int, int, int, int)} to figure the
     * width of each, measuring along the axis between their centers.</p>
     */
    private float calculateModuleSizeOneWay(ResultPoint pattern, ResultPoint otherPattern) {
        float moduleSizeEst1 = sizeOfBlackWhiteBlackRunBothWays((int) pattern.getX(),
                (int) pattern.getY(),
                (int) otherPattern.getX(),
                (int) otherPattern.getY());
        float moduleSizeEst2 = sizeOfBlackWhiteBlackRunBothWays((int) otherPattern.getX(),
                (int) otherPattern.getY(),
                (int) pattern.getX(),
                (int) pattern.getY());
        if (Float.isNaN(moduleSizeEst1)) {
            return moduleSizeEst2;
        }
        if (Float.isNaN(moduleSizeEst2)) {
            return moduleSizeEst1;
        }
        // Average them, and divide by 7 since we've counted the width of 3 black modules,
        // and 1 white and 1 black module on either side. Ergo, divide sum by 14.
        return (moduleSizeEst1 + moduleSizeEst2) / 14.0f;
    }

    /**
     * See {@link #sizeOfBlackWhiteBlackRun(int, int, int, int)}; computes the total width of
     * a finder pattern by looking for a black-white-black run from the center in the direction
     * of another point (another finder pattern center), and in the opposite direction too.</p>
     */
    private float sizeOfBlackWhiteBlackRunBothWays(int fromX, int fromY, int toX, int toY) {

        float result = sizeOfBlackWhiteBlackRun(fromX, fromY, toX, toY);

        // Now count other way -- don't run off image though of course
        int otherToX = fromX - (toX - fromX);
        if (otherToX < 0) {
            // "to" should the be the first value not included, so, the first value off
            // the edge is -1
            otherToX = -1;
        } else if (otherToX >= image.getWidth()) {
            otherToX = image.getWidth();
        }
        int otherToY = fromY - (toY - fromY);
        if (otherToY < 0) {
            otherToY = -1;
        } else if (otherToY >= image.getHeight()) {
            otherToY = image.getHeight();
        }
        result += sizeOfBlackWhiteBlackRun(fromX, fromY, otherToX, otherToY);
        return result - 1.0f; // -1 because we counted the middle pixel twice
    }

    /**
     * <p>This method traces a line from a point in the image, in the direction towards another point.
     * It begins in a black region, and keeps going until it finds white, then black, then white again.
     * It reports the distance from the start to this point.</p>
     *
     * <p>This is used when figuring out how wide a finder pattern is, when the finder pattern
     * may be skewed or rotated.</p>
     */
    private float sizeOfBlackWhiteBlackRun(int fromX, int fromY, int toX, int toY) {
        // Mild variant of Bresenham's algorithm;
        // see http://en.wikipedia.org/wiki/Bresenham's_line_algorithm
        boolean steep = Math.abs(toY - fromY) > Math.abs(toX - fromX);
        if (steep) {
            int temp = fromX;
            fromX = fromY;
            fromY = temp;
            temp = toX;
            toX = toY;
            toY = temp;
        }

        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);
        int error = -dx >> 1;
        int ystep = fromY < toY ? 1 : -1;
        int xstep = fromX < toX ? 1 : -1;
        int state = 0; // In black pixels, looking for white, first or second time
        for (int x = fromX, y = fromY; x != toX; x += xstep) {

            int realX = steep ? y : x;
            int realY = steep ? x : y;
            if (state == 1) { // In white pixels, looking for black
                if (image.get(realX, realY)) {
                    state++;
                }
            } else {
                if (!image.get(realX, realY)) {
                    state++;
                }
            }

            if (state == 3) { // Found black, white, black, and stumbled back onto white; done
                int diffX = x - fromX;
                int diffY = y - fromY;
                return (float) Math.sqrt((double) (diffX * diffX + diffY * diffY));
            }
            error += dy;
            if (error > 0) {
                y += ystep;
                error -= dx;
            }
        }
        int diffX = toX - fromX;
        int diffY = toY - fromY;
        return (float) Math.sqrt((double) (diffX * diffX + diffY * diffY));
    }

    /**
     * <p>Attempts to locate an alignment pattern in a limited region of the image, which is
     * guessed to contain it. This method uses {@link AlignmentPattern}.</p>
     *
     * @param overallEstModuleSize estimated module size so far
     * @param estAlignmentX x coordinate of center of area probably containing alignment pattern
     * @param estAlignmentY y coordinate of above
     * @param allowanceFactor number of pixels in all directions to search from the center
     * @return {@link AlignmentPattern} if found, or null otherwise
     * @throws ReaderException if an unexpected error occurs during detection
     */
    private AlignmentPattern findAlignmentInRegion(float overallEstModuleSize,
            int estAlignmentX,
            int estAlignmentY,
            float allowanceFactor)
            throws ReaderException {
        // Look for an alignment pattern (3 modules in size) around where it
        // should be
        int allowance = (int) (allowanceFactor * overallEstModuleSize);
        int alignmentAreaLeftX = Math.max(0, estAlignmentX - allowance);
        int alignmentAreaRightX = Math.min(image.getWidth() - 1, estAlignmentX + allowance);
        if (alignmentAreaRightX - alignmentAreaLeftX < overallEstModuleSize * 3) {
            throw ReaderException.getInstance();
        }

        int alignmentAreaTopY = Math.max(0, estAlignmentY - allowance);
        int alignmentAreaBottomY = Math.min(image.getHeight() - 1, estAlignmentY + allowance);

        AlignmentPatternFinder alignmentFinder =
                new AlignmentPatternFinder(
                image,
                alignmentAreaLeftX,
                alignmentAreaTopY,
                alignmentAreaRightX - alignmentAreaLeftX,
                alignmentAreaBottomY - alignmentAreaTopY,
                overallEstModuleSize);
        return alignmentFinder.find();
    }

    /**
     * Ends up being a bit faster than Math.round(). This merely rounds its argument to the nearest int,
     * where x.5 rounds up.
     */
    private static int round(float d) {
        return (int) (d + 0.5f);
    }
}
