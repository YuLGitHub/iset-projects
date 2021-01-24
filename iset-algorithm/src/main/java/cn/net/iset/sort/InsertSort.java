package cn.net.iset.sort;

import java.util.Arrays;

/**
 * ClassName: InsertSort.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/3
 */
public class InsertSort {

    public static void insertSort(int[] array) {
        int temp;
        for (int i = 1; i < array.length; i++) {
            temp = array[i];
            int j;
            for (j = i - 1; j > 0 ; j--) {
                if (temp < array[j]) {
                    array[j + 1] = array[j];
                } else {
                    break;
                }
            }
            array[j + 1] = temp;
        }
    }

    public static void main(String[] args) {
        int[] array = new int[]{1,22,32,11,2,33,44,21};
        insertSort(array);
        System.out.println(Arrays.toString(array));
    }
}
