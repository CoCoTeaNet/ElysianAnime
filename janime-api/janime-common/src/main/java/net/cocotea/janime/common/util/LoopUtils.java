package net.cocotea.janime.common.util;

import java.util.function.Consumer;

/**
 * 循环工具类
 * @author CoCoTea
 * @since 2023-3-9 11:49:05
 */
public class LoopUtils {

    /**
     * 通用循环n次，从 1~n
     * @param count 循环次数
     * @param consumer 回调函数，接收每次循环的次数
     */
    public static void loop(int count, Consumer<? super Integer> consumer) {
        if (count <= 0) {
            System.out.println("count 值不能小于或等于0");
            return;
        }
        int i = 1;
        while (i <= count) {
            consumer.accept(i);
            i++;
        }
    }

}
