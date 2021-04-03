package cn.beichenhpy.order.controller;

import cn.beichenhpy.order.entity.Order;
import cn.beichenhpy.order.service.OrderService;
import org.springframework.core.task.TaskExecutor;
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
    private OrderService orderService;
    @PostMapping("/add")
    public String add(@RequestBody Order order){
        BlockingQueue<Order> orders = ORDER_QUEUE_MAP.get(order.getName());
        if (orders != null){
            orders.add(order);
        }else {
            BlockingQueue<Order> queue =  new LinkedBlockingDeque<>();
            ORDER_QUEUE_MAP.put(order.getName(),queue);
            queue.add(order);
        }
        boolean ok = orderService.doSomething(ORDER_QUEUE_MAP.get(order.getName()));
        return String.valueOf(ok);
    }
}
