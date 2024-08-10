package dev.tyrxuan.middleware.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ApiTest {

    @Test
    public void contextLoad() {
        log.info("context load...");
    }

    /**
     * 测试冒泡排序算法的正确性。
     */
    @Test
    public void testBubbleSort() {
        // 待排序的数组
        int[] array = {1, 3, 2, 5, 4};

        // 冒泡排序算法
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                // 如果当前元素大于下一个元素，则交换它们的位置
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }

        // 打印排序后的数组
        for (int value : array) {
            System.out.println(value);
        }
    }
}
