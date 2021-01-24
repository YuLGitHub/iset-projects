package cn.net.iset.sort;

import java.util.Arrays;

/**
 * ClassName: QuickSort.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/3
 */
public class QuickSort {

    public static void main(String[] args) {
        int[] array = new int[]{1, 2,43, 23, 56, 77};
        quickSort(array);
    }

    public static void sortArray(int[] a, int low, int high) {
        int i = low;
        int j = high;
        // 最低和最大的相遇了
        if (i >= j) {
            return;
        }
        // 数组已经排完或者剩下一个了，则返回
        if (a.length <= 1) {
            return;
        }

        int index = a[low];
        while (i < j) {
            while (i < j && a[j] >= index) {
                j--;
            }
            while (i < j && a[i] <= index) {
                i++;
            }
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
        }
        a[low] = a[i];
        a[i] = index;
        sortArray(a, low, j - 1);
        sortArray(a, i + 1, high);
    }


    public static void quickSort(int[] array) {
        sortArray(array, 0, array.length - 1);
        System.out.println(Arrays.toString(array));
    }
}
