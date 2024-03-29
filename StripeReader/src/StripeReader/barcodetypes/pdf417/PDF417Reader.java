package StripeReader.barcodetypes.pdf417;

import StripeReader.core.BarcodeFormat;
import StripeReader.core.BinaryBitmap;
import StripeReader.core.DecodeHintType;
import StripeReader.core.Reader;
import StripeReader.core.ReaderException;
import StripeReader.core.Result;
import StripeReader.core.ResultPoint;
import StripeReader.common.BitMatrix;
import StripeReader.common.DecoderResult;
import StripeReader.common.DetectorResult;
import StripeReader.barcodetypes.pdf417.decoder.Decoder;
import StripeReader.barcodetypes.pdf417.detector.Detector;

import java.util.Hashtable;

public final class PDF417Reader implements Reader {

    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
    private final Decoder decoder = new Decoder();

    /**
     * Locates and decodes a PDF417 code in an image.
     *
     * @return a String representing the content encoded by the PDF417 code
     * @throws ReaderException if a PDF417 code cannot be found, or cannot be decoded
     */
    public Result decode(BinaryBitmap image) throws ReaderException {
        return decode(image, null);
    }

    public Result decode(BinaryBitmap image, Hashtable hints)
            throws ReaderException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            BitMatrix bits = extractPureBits(image);
            decoderResult = decoder.decode(bits);
            points = NO_POINTS;
        } else {
            DetectorResult detectorResult = new Detector(image).detect();
            decoderResult = decoder.decode(detectorResult.getBits());
            points = detectorResult.getPoints();
        }
        return new Result(decoderResult.getText(), decoderResult.getRawBytes(), points,
                BarcodeFormat.PDF417);
    }

    /**
     * This method detects a barcode in a "pure" image -- that is, pure monochrome image
     * which contains only an unrotated, unskewed, image of a barcode, with some white border
     * around it. This is a specialized method that works exceptionally fast in this special
     * case.
     */
    private static BitMatrix extractPureBits(BinaryBitmap image) throws ReaderException {
        // Now need to determine module size in pixels
        BitMatrix matrix = image.getBlackMatrix();
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        int minDimension = Math.min(height, width);

        // First, skip white border by tracking diagonally from the top left down and to the right:
        int borderWidth = 0;
        while (borderWidth < minDimension && !matrix.get(borderWidth, borderWidth)) {
            borderWidth++;
        }
        if (borderWidth == minDimension) {
            throw ReaderException.getInstance();
        }

        // And then keep tracking across the top-left black module to determine module size
        int moduleEnd = borderWidth;
        while (moduleEnd < minDimension && matrix.get(moduleEnd, moduleEnd)) {
            moduleEnd++;
        }
        if (moduleEnd == minDimension) {
            throw ReaderException.getInstance();
        }

        int moduleSize = moduleEnd - borderWidth;

        // And now find where the rightmost black module on the first row ends
        int rowEndOfSymbol = width - 1;
        while (rowEndOfSymbol >= 0 && !matrix.get(rowEndOfSymbol, borderWidth)) {
            rowEndOfSymbol--;
        }
        if (rowEndOfSymbol < 0) {
            throw ReaderException.getInstance();
        }
        rowEndOfSymbol++;

        // Make sure width of barcode is a multiple of module size
        if ((rowEndOfSymbol - borderWidth) % moduleSize != 0) {
            throw ReaderException.getInstance();
        }
        int dimension = (rowEndOfSymbol - borderWidth) / moduleSize;

        // Push in the "border" by half the module width so that we start
        // sampling in the middle of the module. Just in case the image is a
        // little off, this will help recover.
        borderWidth += moduleSize >> 1;

        int sampleDimension = borderWidth + (dimension - 1) * moduleSize;
        if (sampleDimension >= width || sampleDimension >= height) {
            throw ReaderException.getInstance();
        }

        // Now just read off the bits
        BitMatrix bits = new BitMatrix(dimension);
        for (int y = 0; y < dimension; y++) {
            int iOffset = borderWidth + y * moduleSize;
            for (int x = 0; x < dimension; x++) {
                if (matrix.get(borderWidth + x * moduleSize, iOffset)) {
                    bits.set(x, y);
                }
            }
        }
        return bits;
    }
}
