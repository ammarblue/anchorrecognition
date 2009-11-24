package StripeReader.common;

import java.util.Vector;

public final class Collections {

    private Collections() {
    }

    /**
     * Sorts its argument (destructively) using insert sort; in the context of this package
     * insertion sort is simple and efficient given its relatively small inputs.
     *
     * @param vector vector to sort
     * @param comparator comparator to define sort ordering
     */
    public static void insertionSort(Vector vector, Comparator comparator) {
        int max = vector.size();
        for (int i = 1; i < max; i++) {
            Object value = vector.elementAt(i);
            int j = i - 1;
            Object valueB;
            while (j >= 0 && comparator.compare((valueB = vector.elementAt(j)), value) > 0) {
                vector.setElementAt(valueB, j + 1);
                j--;
            }
            vector.setElementAt(value, j + 1);
        }
    }
}
