import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
  * This class provides static methods to merge two sorted collections into 
  * a single sorted List.
  */
public class MergeTwoSortedCollection {

    // ******************************* TEST *******************************
    public static void main(String[] args) {
        List<Integer> a = Arrays.asList(1,2,3,4,5);
        List<Integer> b = Arrays.asList(2,3,4,5);
        List<Integer> res = merge(a, b);
        for (int i : res) {
            System.out.println(i);
        }
    }


    private static final boolean DEFAULT_ALLOW_DUP = true;  // allow duplicates by default.

    // This method calls main merge method below with default natural order and default allow duplication.
    public static <T extends Comparable<? super T>> List<T> merge(Collection<T> c1, Collection c2) {
        return merge(c1, c2, Comparator.naturalOrder(), DEFAULT_ALLOW_DUP);
    }

    // This method calls main merge method below with default natural order.
    public static <T extends Comparable<? super T>> List<T> merge(Collection<T> c1, Collection c2, boolean allowDup) {
        return merge(c1, c2, Comparator.naturalOrder(), allowDup);
    }

    /**
     * This method merges two sorted Collections into a single sorted List.
     * A comparator can be passed in for customized ordering.
     * 
     * Uses the standard O(n) merge algorithm for combining two sorted lists.
     *
     * @param <T>  the element type
     * @param c1  the first collection, must not be null
     * @param c2  the second collection, must not be null
     * @param cp  comparator that is passed in for customized ordering
     * @param allowDup boolean to flag if duplicated elements are allowed in merged List
     * @return a new sorted List, containing the elements of input Collection
     * @throws NullPointerException if either collection is null
     */
    public static <T> List<T> merge(Collection<T> c1, Collection<T> c2, Comparator<T> cp, boolean allowDup) {
        if (c1 == null || c2 == null) {
            throw new NullPointerException("The collections must not be null");
        }
        if (cp == null) {
            throw new NullPointerException("The comparator must not be null");
        }

        List<T> mergedList = new ArrayList<T>(c1.size() + c2.size());
        Iterator<T> iter1 = c1.iterator();
        Iterator<T> iter2 = c2.iterator();

        // Use prev pointers to avoid iterator from skipping an element that 
        // has not been added to merged list.
        T t1Prev = null;
        T t2Prev = null;
        while (iter1.hasNext() && iter2.hasNext()) {
            T t1 = t1Prev == null ? iter1.next() : t1Prev;
            T t2 = t2Prev == null ? iter2.next() : t2Prev;
            if (t1.equals(t2)) {
                if (allowDup) {
                    mergedList.add(t1);
                    mergedList.add(t2);
                    t1Prev = null;
                    t2Prev = null;
                } else {
                    mergedList.add(t1);
                    t1Prev = null;
                    t2Prev = null;
                }
            } else if (cp.compare(t1, t2) < 0) {
                mergedList.add(t1);
                t1Prev = null;
                t2Prev = t2;
            } else {
                mergedList.add(t2);
                t2Prev = null;
                t1Prev = t1;
            }
        }
        // Process possible remaining elements.
        while (iter1.hasNext()) {
            mergedList.add(iter1.next());
        }
        while (iter2.hasNext()) {
            mergedList.add(iter2.next());
        }

        return mergedList;
    }

}
