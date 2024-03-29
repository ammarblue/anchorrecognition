package StripeReader.barcodetypes;

import StripeReader.core.BarcodeFormat;
import StripeReader.core.DecodeHintType;
import StripeReader.core.ReaderException;
import StripeReader.core.Result;
import StripeReader.common.BitArray;

import java.util.Hashtable;
import java.util.Vector;

public final class MultiFormatUPCEANReader extends AbstractOneDReader {

    private final Vector readers;

    public MultiFormatUPCEANReader(Hashtable hints) {
        Vector possibleFormats = hints == null ? null : (Vector) hints.get(DecodeHintType.POSSIBLE_FORMATS);
        readers = new Vector();
        if (possibleFormats != null) {
            if (possibleFormats.contains(BarcodeFormat.EAN_13)) {
                readers.addElement(new EAN13Reader());
            } else if (possibleFormats.contains(BarcodeFormat.UPC_A)) {
                readers.addElement(new UPCAReader());
            }
            if (possibleFormats.contains(BarcodeFormat.EAN_8)) {
                readers.addElement(new EAN8Reader());
            }
            if (possibleFormats.contains(BarcodeFormat.UPC_E)) {
                readers.addElement(new UPCEReader());
            }
        }
        if (readers.isEmpty()) {
            readers.addElement(new EAN13Reader());
            // UPC-A is covered by EAN-13
            readers.addElement(new EAN8Reader());
            readers.addElement(new UPCEReader());
        }
    }

    public Result decodeRow(int rowNumber, BitArray row, Hashtable hints) throws ReaderException {
        // Compute this location once and reuse it on multiple implementations
        int[] startGuardPattern = AbstractUPCEANReader.findStartGuardPattern(row);
        int size = readers.size();
        for (int i = 0; i < size; i++) {
            UPCEANReader reader = (UPCEANReader) readers.elementAt(i);
            Result result;
            try {
                result = reader.decodeRow(rowNumber, row, startGuardPattern);
            } catch (Exception re) {
                continue;
            }
            // Special case: a 12-digit code encoded in UPC-A is identical to a "0"
            // followed by those 12 digits encoded as EAN-13. Each will recognize such a code,
            // UPC-A as a 12-digit string and EAN-13 as a 13-digit string starting with "0".
            // Individually these are correct and their readers will both read such a code
            // and correctly call it EAN-13, or UPC-A, respectively.
            //
            // In this case, if we've been looking for both types, we'd like to call it
            // a UPC-A code. But for efficiency we only run the EAN-13 decoder to also read
            // UPC-A. So we special case it here, and convert an EAN-13 result to a UPC-A
            // result if appropriate.
            if (result.getBarcodeFormat().equals(BarcodeFormat.EAN_13) && result.getText().charAt(0) == '0') {
                return new Result(result.getText().substring(1), null, result.getResultPoints(), BarcodeFormat.UPC_A);
            }
            return result;
        }

        throw ReaderException.getInstance();
    }
}
