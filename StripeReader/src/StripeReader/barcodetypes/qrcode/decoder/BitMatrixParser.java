package StripeReader.barcodetypes.qrcode.decoder;

import StripeReader.core.ReaderException;
import StripeReader.common.BitMatrix;

final class BitMatrixParser {

    private final BitMatrix bitMatrix;
    private Version parsedVersion;
    private FormatInformation parsedFormatInfo;

    /**
     * @param bitMatrix {@link BitMatrix} to parse
     * @throws ReaderException if dimension is not >= 21 and 1 mod 4
     */
    BitMatrixParser(BitMatrix bitMatrix) throws ReaderException {
        int dimension = bitMatrix.getDimension();
        if (dimension < 21 || (dimension & 0x03) != 1) {
            throw ReaderException.getInstance();
        }
        this.bitMatrix = bitMatrix;
    }

    /**
     * <p>Reads format information from one of its two locations within the QR Code.</p>
     *
     * @return {@link FormatInformation} encapsulating the QR Code's format info
     * @throws ReaderException if both format information locations cannot be parsed as
     * the valid encoding of format information
     */
    FormatInformation readFormatInformation() throws ReaderException {

        if (parsedFormatInfo != null) {
            return parsedFormatInfo;
        }

        // Read top-left format info bits
        int formatInfoBits = 0;
        for (int j = 0; j < 6; j++) {
            formatInfoBits = copyBit(8, j, formatInfoBits);
        }
        // .. and skip a bit in the timing pattern ...
        formatInfoBits = copyBit(8, 7, formatInfoBits);
        formatInfoBits = copyBit(8, 8, formatInfoBits);
        formatInfoBits = copyBit(7, 8, formatInfoBits);
        // .. and skip a bit in the timing pattern ...
        for (int i = 5; i >= 0; i--) {
            formatInfoBits = copyBit(i, 8, formatInfoBits);
        }

        parsedFormatInfo = FormatInformation.decodeFormatInformation(formatInfoBits);
        if (parsedFormatInfo != null) {
            return parsedFormatInfo;
        }

        // Hmm, failed. Try the top-right/bottom-left pattern
        int dimension = bitMatrix.getDimension();
        formatInfoBits = 0;
        int iMin = dimension - 8;
        for (int i = dimension - 1; i >= iMin; i--) {
            formatInfoBits = copyBit(i, 8, formatInfoBits);
        }
        for (int j = dimension - 7; j < dimension; j++) {
            formatInfoBits = copyBit(8, j, formatInfoBits);
        }

        parsedFormatInfo = FormatInformation.decodeFormatInformation(formatInfoBits);
        if (parsedFormatInfo != null) {
            return parsedFormatInfo;
        }
        throw ReaderException.getInstance();
    }

    /**
     * <p>Reads version information from one of its two locations within the QR Code.</p>
     *
     * @return {@link Version} encapsulating the QR Code's version
     * @throws ReaderException if both version information locations cannot be parsed as
     * the valid encoding of version information
     */
    Version readVersion() throws ReaderException {

        if (parsedVersion != null) {
            return parsedVersion;
        }

        int dimension = bitMatrix.getDimension();

        int provisionalVersion = (dimension - 17) >> 2;
        if (provisionalVersion <= 6) {
            return Version.getVersionForNumber(provisionalVersion);
        }

        // Read top-right version info: 3 wide by 6 tall
        int versionBits = 0;
        for (int i = 5; i >= 0; i--) {
            int jMin = dimension - 11;
            for (int j = dimension - 9; j >= jMin; j--) {
                versionBits = copyBit(i, j, versionBits);
            }
        }

        parsedVersion = Version.decodeVersionInformation(versionBits);
        if (parsedVersion != null && parsedVersion.getDimensionForVersion() == dimension) {
            return parsedVersion;
        }

        // Hmm, failed. Try bottom left: 6 wide by 3 tall
        versionBits = 0;
        for (int j = 5; j >= 0; j--) {
            int iMin = dimension - 11;
            for (int i = dimension - 9; i >= iMin; i--) {
                versionBits = copyBit(i, j, versionBits);
            }
        }

        parsedVersion = Version.decodeVersionInformation(versionBits);
        if (parsedVersion != null && parsedVersion.getDimensionForVersion() == dimension) {
            return parsedVersion;
        }
        throw ReaderException.getInstance();
    }

    private int copyBit(int i, int j, int versionBits) {
        return bitMatrix.get(j, i) ? (versionBits << 1) | 0x1 : versionBits << 1;
    }

    /**
     * <p>Reads the bits in the {@link BitMatrix} representing the finder pattern in the
     * correct order in order to reconstitute the codewords bytes contained within the
     * QR Code.</p>
     *
     * @return bytes encoded within the QR Code
     * @throws ReaderException if the exact number of bytes expected is not read
     */
    byte[] readCodewords() throws ReaderException {

        FormatInformation formatInfo = readFormatInformation();
        Version version = readVersion();

        // Get the data mask for the format used in this QR Code. This will exclude
        // some bits from reading as we wind through the bit matrix.
        DataMask dataMask = DataMask.forReference((int) formatInfo.getDataMask());
        int dimension = bitMatrix.getDimension();
        dataMask.unmaskBitMatrix(bitMatrix, dimension);

        BitMatrix functionPattern = version.buildFunctionPattern();

        boolean readingUp = true;
        byte[] result = new byte[version.getTotalCodewords()];
        int resultOffset = 0;
        int currentByte = 0;
        int bitsRead = 0;
        // Read columns in pairs, from right to left
        for (int j = dimension - 1; j > 0; j -= 2) {
            if (j == 6) {
                // Skip whole column with vertical alignment pattern;
                // saves time and makes the other code proceed more cleanly
                j--;
            }
            // Read alternatingly from bottom to top then top to bottom
            for (int count = 0; count < dimension; count++) {
                int i = readingUp ? dimension - 1 - count : count;
                for (int col = 0; col < 2; col++) {
                    // Ignore bits covered by the function pattern
                    if (!functionPattern.get(j - col, i)) {
                        // Read a bit
                        bitsRead++;
                        currentByte <<= 1;
                        if (bitMatrix.get(j - col, i)) {
                            currentByte |= 1;
                        }
                        // If we've made a whole byte, save it off
                        if (bitsRead == 8) {
                            result[resultOffset++] = (byte) currentByte;
                            bitsRead = 0;
                            currentByte = 0;
                        }
                    }
                }
            }
            readingUp ^= true; // readingUp = !readingUp; // switch directions
        }
        if (resultOffset != version.getTotalCodewords()) {
            throw ReaderException.getInstance();
        }
        return result;
    }
}
