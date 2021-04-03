package cn.beichenhpy.order.service;


import cn.beichenhpy.order.entity.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OrderCallable implements Callable<Boolean> {
    private final BlockingQueue<Order> orderQueue;

    public OrderCallable(BlockingQueue<Order> orderQueue) {
        this.orderQueue = orderQueue;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public synchronized Boolean call() throws Exception {
        Order order = orderQueue.poll(1,TimeUnit.MINUTES);
        if (order != null){
            //模拟任务
            Thread.sleep(500);
            log.warn("执行order:{},时间：{}",order,new Date());
            return true;
        }else {
            return false;
        }
    }
}
