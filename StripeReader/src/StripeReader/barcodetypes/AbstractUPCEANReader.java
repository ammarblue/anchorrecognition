package StripeReader.barcodetypes;

import StripeReader.core.BarcodeFormat;
import StripeReader.core.ReaderException;
import StripeReader.core.Result;
import StripeReader.core.ResultPoint;
import StripeReader.common.BitArray;

import java.util.Hashtable;

public abstract class AbstractUPCEANReader extends AbstractOneDReader implements UPCEANReader {

    private static final int MAX_AVG_VARIANCE = (int) (PATTERN_MATCH_RESULT_SCALE_FACTOR * 0.42f);
    private static final int MAX_INDIVIDUAL_VARIANCE = (int) (PATTERN_MATCH_RESULT_SCALE_FACTOR * 0.7f);
    /**
     * Start/end guard pattern.
     */
    static final int[] START_END_PATTERN = {1, 1, 1,};
    /**
     * Pattern marking the middle of a UPC/EAN pattern, separating the two halves.
     */
    static final int[] MIDDLE_PATTERN = {1, 1, 1, 1, 1};
    /**
     * "Odd", or "L" patterns used to encode UPC/EAN digits.
     */
    static final int[][] L_PATTERNS = {
        {3, 2, 1, 1}, // 0
        {2, 2, 2, 1}, // 1
        {2, 1, 2, 2}, // 2
        {1, 4, 1, 1}, // 3
        {1, 1, 3, 2}, // 4
        {1, 2, 3, 1}, // 5
        {1, 1, 1, 4}, // 6
        {1, 3, 1, 2}, // 7
        {1, 2, 1, 3}, // 8
        {3, 1, 1, 2} // 9
    };
    /**
     * As above but also including the "even", or "G" patterns used to encode UPC/EAN digits.
     */
    static final int[][] L_AND_G_PATTERNS;

    static {
        L_AND_G_PATTERNS = new int[20][];
        for (int i = 0; i < 10; i++) {
            L_AND_G_PATTERNS[i] = L_PATTERNS[i];
        }
        for (int i = 10; i < 20; i++) {
            int[] widths = L_PATTERNS[i - 10];
            int[] reversedWidths = new int[widths.length];
            for (int j = 0; j < widths.length; j++) {
                reversedWidths[j] = widths[widths.length - j - 1];
            }
            L_AND_G_PATTERNS[i] = reversedWidths;
        }
    }
    private final StringBuffer decodeRowStringBuffer;

    protected AbstractUPCEANReader() {
        decodeRowStringBuffer = new StringBuffer(20);
    }

    static int[] findStartGuardPattern(BitArray row) throws ReaderException {
        boolean foundStart = false;
        int[] startRange = null;
        int nextStart = 0;
        while (!foundStart) {
            startRange = findGuardPattern(row, nextStart, false, START_END_PATTERN);
            int start = startRange[0];
            nextStart = startRange[1];
            // Make sure there is a quiet zone at least as big as the start pattern before the barcode.
            // If this check would run off the left edge of the image, do not accept this barcode,
            // as it is very likely to be a false positive.
            int quietStart = start - (nextStart - start);
            if (quietStart >= 0) {
                foundStart = row.isRange(quietStart, start, false);
            }
        }
        return startRange;
    }

    public final Result decodeRow(int rowNumber, BitArray row, Hashtable hints)
            throws ReaderException {
        return decodeRow(rowNumber, row, findStartGuardPattern(row));
    }

    public final Result decodeRow(int rowNumber, BitArray row, int[] startGuardRange)
            throws ReaderException {
        StringBuffer result = decodeRowStringBuffer;
        result.setLength(0);
        int endStart = decodeMiddle(row, startGuardRange, result);
        int[] endRange = decodeEnd(row, endStart);

        // Make sure there is a quiet zone at least as big as the end pattern after the barcode. The
        // spec might want more whitespace, but in practice this is the maximum we can count on.
        int end = endRange[1];
        int quietEnd = end + (end - endRange[0]);
        if (quietEnd >= row.getSize() || !row.isRange(end, quietEnd, false)) {
            throw ReaderException.getInstance();
        }

        String resultString = result.toString();
        if (!checkChecksum(resultString)) {
            throw ReaderException.getInstance();
        }

        float left = (float) (startGuardRange[1] + startGuardRange[0]) / 2.0f;
        float right = (float) (endRange[1] + endRange[0]) / 2.0f;
        return new Result(resultString,
                null, // no natural byte representation for these barcodes
                new ResultPoint[]{
                    new ResultPoint(left, (float) rowNumber),
                    new ResultPoint(right, (float) rowNumber)},
                getBarcodeFormat());
    }

    abstract BarcodeFormat getBarcodeFormat();

    /**
     * @return {@link #checkStandardUPCEANChecksum(String)}
     */
    boolean checkChecksum(String s) throws ReaderException {
        return checkStandardUPCEANChecksum(s);
    }

    /**
     * Computes the UPC/EAN checksum on a string of digits, and reports
     * whether the checksum is correct or not.
     *
     * @param s string of digits to check
     * @return true iff string of digits passes the UPC/EAN checksum algorithm
     * @throws ReaderException if the string does not contain only digits
     */
    private static boolean checkStandardUPCEANChecksum(String s) throws ReaderException {
        int length = s.length();
        if (length == 0) {
            return false;
        }

        int sum = 0;
        for (int i = length - 2; i >= 0; i -= 2) {
            int digit = (int) s.charAt(i) - (int) '0';
            if (digit < 0 || digit > 9) {
                throw ReaderException.getInstance();
            }
            sum += digit;
        }
        sum *= 3;
        for (int i = length - 1; i >= 0; i -= 2) {
            int digit = (int) s.charAt(i) - (int) '0';
            if (digit < 0 || digit > 9) {
                throw ReaderException.getInstance();
            }
            sum += digit;
        }
        return sum % 10 == 0;
    }

    /**
     * Subclasses override this to decode the portion of a barcode between the start
     * and end guard patterns.
     *
     * @param row row of black/white values to search
     * @param startRange start/end offset of start guard pattern
     * @param resultString {@link StringBuffer} to append decoded chars to
     * @return horizontal offset of first pixel after the "middle" that was decoded
     * @throws ReaderException if decoding could not complete successfully
     */
    protected abstract int decodeMiddle(BitArray row, int[] startRange, StringBuffer resultString)
            throws ReaderException;

    int[] decodeEnd(BitArray row, int endStart) throws ReaderException {
        return findGuardPattern(row, endStart, false, START_END_PATTERN);
    }

    /**
     * @param row row of black/white values to search
     * @param rowOffset position to start search
     * @param whiteFirst if true, indicates that the pattern specifies white/black/white/...
     * pixel counts, otherwise, it is interpreted as black/white/black/...
     * @param pattern pattern of counts of number of black and white pixels that are being
     * searched for as a pattern
     * @return start/end horizontal offset of guard pattern, as an array of two ints
     * @throws ReaderException if pattern is not found
     */
    static int[] findGuardPattern(BitArray row, int rowOffset, boolean whiteFirst, int[] pattern)
            throws ReaderException {
        int patternLength = pattern.length;
        int[] counters = new int[patternLength];
        int width = row.getSize();
        boolean isWhite = false;
        while (rowOffset < width) {
            isWhite = !row.get(rowOffset);
            if (whiteFirst == isWhite) {
                break;
            }
            rowOffset++;
        }

        int counterPosition = 0;
        int patternStart = rowOffset;
        for (int x = rowOffset; x < width; x++) {
            boolean pixel = row.get(x);
            if (pixel ^ isWhite) {
                counters[counterPosition]++;
            } else {
                if (counterPosition == patternLength - 1) {
                    if (patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) < MAX_AVG_VARIANCE) {
                        return new int[]{patternStart, x};
                    }
                    patternStart += counters[0] + counters[1];
                    for (int y = 2; y < patternLength; y++) {
                        counters[y - 2] = counters[y];
                    }
                    counters[patternLength - 2] = 0;
                    counters[patternLength - 1] = 0;
                    counterPosition--;
                } else {
                    counterPosition++;
                }
                counters[counterPosition] = 1;
                isWhite ^= true; // isWhite = !isWhite;
            }
        }
        throw ReaderException.getInstance();
    }

    /**
     * Attempts to decode a single UPC/EAN-encoded digit.
     *
     * @param row row of black/white values to decode
     * @param counters the counts of runs of observed black/white/black/... values
     * @param rowOffset horizontal offset to start decoding from
     * @param patterns the set of patterns to use to decode -- sometimes different encodings
     * for the digits 0-9 are used, and this indicates the encodings for 0 to 9 that should
     * be used
     * @return horizontal offset of first pixel beyond the decoded digit
     * @throws ReaderException if digit cannot be decoded
     */
    static int decodeDigit(BitArray row, int[] counters, int rowOffset, int[][] patterns)
            throws ReaderException {
        recordPattern(row, rowOffset, counters);
        int bestVariance = MAX_AVG_VARIANCE; // worst variance we'll accept
        int bestMatch = -1;
        int max = patterns.length;
        for (int i = 0; i < max; i++) {
            int[] pattern = patterns[i];
            int variance = patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE);
            if (variance < bestVariance) {
                bestVariance = variance;
                bestMatch = i;
            }
        }
        if (bestMatch >= 0) {
            return bestMatch;
        } else {
            throw ReaderException.getInstance();
        }
    }
}
