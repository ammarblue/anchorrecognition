package StripeReader.core;

import StripeReader.common.BitArray;
import StripeReader.common.BitMatrix;

public final class BinaryBitmap {

    private final Binarizer binarizer;
    private BitMatrix matrix;

    public BinaryBitmap(Binarizer binarizer) {
        if (binarizer == null) {
            throw new IllegalArgumentException("Binarizer must be non-null.");
        }
        this.binarizer = binarizer;
        matrix = null;
    }

    /**
     * @return The width of the bitmap.
     */
    public int getWidth() {
        return binarizer.getLuminanceSource().getWidth();
    }

    /**
     * @return The height of the bitmap.
     */
    public int getHeight() {
        return binarizer.getLuminanceSource().getHeight();
    }

    /**
     * Converts one row of luminance data to 1 bit data. May actually do the conversion, or return
     * cached data. Callers should assume this method is expensive and call it as seldom as possible.
     * This method is intended for decoding 1D barcodes and may choose to apply sharpening.
     *
     * @param y The row to fetch, 0 <= y < bitmap height.
     * @param row An optional preallocated array. If null or too small, it will be ignored.
     *            If used, the Binarizer will call BitArray.clear(). Always use the returned object.
     * @return The array of bits for this row (true means black).
     */
    public BitArray getBlackRow(int y, BitArray row) throws ReaderException {
        return binarizer.getBlackRow(y, row);
    }

    /**
     * Converts a 2D array of luminance data to 1 bit. As above, assume this method is expensive
     * and do not call it repeatedly. This method is intended for decoding 2D barcodes and may or
     * may not apply sharpening. Therefore, a row from this matrix may not be identical to one
     * fetched using getBlackRow(), so don't mix and match between them.
     *
     * @return The 2D array of bits for the image (true means black).
     */
    public BitMatrix getBlackMatrix() throws ReaderException {
        // The matrix is created on demand the first time it is requested, then cached. There are two
        // reasons for this:
        // 1. This work will never be done if the caller only installs 1D Reader objects, or if a
        //    1D Reader finds a barcode before the 2D Readers run.
        // 2. This work will only be done once even if the caller installs multiple 2D Readers.
        if (matrix == null) {
            matrix = binarizer.getBlackMatrix();
        }
        return matrix;
    }

    /**
     * @return Whether this bitmap can be cropped.
     */
    public boolean isCropSupported() {
        return binarizer.getLuminanceSource().isCropSupported();
    }

    /**
     * Returns a new object with cropped image data. Implementations may keep a reference to the
     * original data rather than a copy. Only callable if isCropSupported() is true.
     *
     * @param left The left coordinate, 0 <= left < getWidth().
     * @param top The top coordinate, 0 <= top <= getHeight().
     * @param width The width of the rectangle to crop.
     * @param height The height of the rectangle to crop.
     * @return A cropped version of this object.
     */
    public BinaryBitmap crop(int left, int top, int width, int height) {
        LuminanceSource newSource = binarizer.getLuminanceSource().crop(left, top, width, height);
        return new BinaryBitmap(binarizer.createBinarizer(newSource));
    }

    /**
     * @return Whether this bitmap supports counter-clockwise rotation.
     */
    public boolean isRotateSupported() {
        return binarizer.getLuminanceSource().isRotateSupported();
    }

    /**
     * Returns a new object with rotated image data. Only callable if isRotateSupported() is true.
     *
     * @return A rotated version of this object.
     */
    public BinaryBitmap rotateCounterClockwise() {
        LuminanceSource newSource = binarizer.getLuminanceSource().rotateCounterClockwise();
        return new BinaryBitmap(binarizer.createBinarizer(newSource));
    }
}
