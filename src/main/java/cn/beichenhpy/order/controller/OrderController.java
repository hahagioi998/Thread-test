package cn.beichenhpy.order.controller;

import cn.beichenhpy.order.entity.Order;
import cn.beichenhpy.order.service.OrderCallable;
import cn.beichenhpy.order.service.OrderRunnable;
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
            //写在这里的话，就只有新建一个队列时才创建一个线程去计算
            //Callable while(true)的话，不能return true 否则循环打破了，因此只需要判断 异常停止的情况 即 返回值 = false，成功默认为true
            Future<Boolean> submit = taskExecutor.submit(new OrderCallable(orders));
            if (submit.isDone()){
                try {
                    ok = submit.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            //Runnable实现
            //taskExecutor.execute(new OrderRunnable(orders));
        }
        return String.valueOf(ok);
    }
}
