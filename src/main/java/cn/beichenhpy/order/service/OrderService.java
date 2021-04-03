package cn.beichenhpy.order.service;

import cn.beichenhpy.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.*;

@Service
@Slf4j
public class OrderService {
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    public synchronized boolean doSomething(BlockingQueue<Order> orderBlockingQueue){
        boolean ok = true;
        Future<Boolean> booleanFuture = taskExecutor.submit(new OrderThread(orderBlockingQueue));
        if (booleanFuture.isDone()) {
            try {
                ok = booleanFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }
}
