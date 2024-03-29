package StripeReader.common;

import StripeReader.core.ResultPoint;

/**
 * <p>Encapsulates the result of detecting a barcode in an image. This includes the raw
 * matrix of black/white pixels corresponding to the barcode, and possibly points of interest
 * in the image, like the location of finder patterns or corners of the barcode in the image.</p>
 *
 * @author Sean Owen
 */
public final class DetectorResult {

    private final BitMatrix bits;
    private final ResultPoint[] points;

    public DetectorResult(BitMatrix bits, ResultPoint[] points) {
        this.bits = bits;
        this.points = points;
    }

    public BitMatrix getBits() {
        return bits;
    }

    public ResultPoint[] getPoints() {
        return points;
    }
}
