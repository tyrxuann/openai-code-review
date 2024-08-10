package dev.tyrxuan.middleware.test;

public class HeapSortTest {

    /**
     * 测试堆排序算法
     * @param array 待排序的整数数组
     */
    public static void testHeapSort(int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null.");
        }

        // 构建最大堆
        for (int i = array.length / 2 - 1; i >= 0; i--) {
            heapify(array, array.length, i);
        }

        // 堆排序
        for (int i = array.length - 1; i > 0; i--) {
            int temp = array[0];
            array[0] = array[i];
            array[i] = temp;
            heapify(array, i, 0);
        }

        // 打印排序后的数组
        for (int value : array) {
            System.out.println(value);
        }
    }

    /**
     * 将数组调整为最大堆
     * @param array 整数数组
     * @param size 数组的有效大小
     * @param i 当前节点的索引
     */
    private static void heapify(int[] array, int size, int i) {
        int largest = i; // 初始化最大值索引
        int left = 2 * i + 1; // 左子节点
        int right = 2 * i + 2; // 右子节点

        // 如果左子节点大于根
        if (left < size && array[left] > array[largest]) {
            largest = left;
        }

        // 如果右子节点大于目前的最大值
        if (right < size && array[right] > array[largest]) {
            largest = right;
        }

        // 如果最大值不是根
        if (largest != i) {
            int swap = array[i];
            array[i] = array[largest];
            array[largest] = swap;

            // 递归地调整受影响的子树
            heapify(array, size, largest);
        }
    }

    // 测试函数
    public static void main(String[] args) {
        int[] array = {1, 3, 2, 5, 4};
        testHeapSort(array);
    }
}
