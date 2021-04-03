package cn.beichenhpy.order.controller;

import cn.beichenhpy.order.entity.Order;
import cn.beichenhpy.order.service.OrderRunnable;
import cn.beichenhpy.order.service.OrderThread;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.*;

@RestController
public class OrderController {
    public static final Map<String , BlockingQueue<Order>> ORDER_QUEUE_MAP = new ConcurrentHashMap<>();
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;
    @PostMapping("/add")
    public String add(@RequestBody Order order){
        boolean ok = true;
        BlockingQueue<Order> orders = ORDER_QUEUE_MAP.get(order.getName());
        if (orders != null){
            orders.add(order);
        }else {
            orders =  new LinkedBlockingDeque<>();
            orders.add(order);
            ORDER_QUEUE_MAP.put(order.getName(),orders);
            //写在这里的话，就只有新建一个队列时才创建一个线程去计算，但是Callable不能while(true)不懂为什么？使用Runnable就可以
//            Future<Boolean> submit = taskExecutor.submit(new OrderThread(orders));
//            if (submit.isDone()){
//                try {
//                    ok = submit.get();
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
            taskExecutor.execute(new OrderRunnable(orders));
        }
        return String.valueOf(ok);
    }
}
