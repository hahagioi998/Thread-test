package cn.beichenhpy.order.service;


import cn.beichenhpy.order.entity.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OrderCallableLoop implements Callable<Boolean> {
    private final BlockingQueue<Order> orderQueue;

    public OrderCallableLoop(BlockingQueue<Order> orderQueue) {
        this.orderQueue = orderQueue;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public synchronized Boolean call() throws Exception {
        try {
            while (true){
                Order order = orderQueue.poll(1,TimeUnit.MINUTES);
                if (order != null){
                    //模拟耗时操作
                    Thread.sleep(500);
                    log.warn("执行了order:{},时间：{}",order,new Date());
                }
            }
        } catch (InterruptedException e) {
            log.warn("线程：{},强制中断",Thread.currentThread().getName());
            return false; // 注意这里如果不return的话，线程还会继续执行，所以任务超时后在这里处理结果然后返回
        }
    }
}
