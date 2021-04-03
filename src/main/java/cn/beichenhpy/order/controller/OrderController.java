package cn.beichenhpy.order.controller;

import cn.beichenhpy.order.entity.Order;
import cn.beichenhpy.order.service.OrderCallable;
import cn.beichenhpy.order.service.OrderCallableLoop;
import cn.beichenhpy.order.service.OrderRunnable;
import cn.beichenhpy.order.service.OrderRunnableLoop;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.*;

@RestController
public class OrderController {
    public static final Map<String, BlockingQueue<Order>> ORDER_QUEUE_MAP = new ConcurrentHashMap<>();
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    @PostMapping("/add")
    public String add(@RequestBody Order order) {
        boolean ok = true;
        BlockingQueue<Order> orders = ORDER_QUEUE_MAP.get(order.getName());
        if (orders != null) {
            orders.add(order);
        } else {
            orders = new LinkedBlockingQueue<>();
            orders.add(order);
            ORDER_QUEUE_MAP.put(order.getName(), orders);
            /*
             * 写在这里的话，就只有新建一个队列时才创建一个线程去计算 这种需要while(true)
             * 这里while(true)会一直循环，所以当服务停止时会出发InterruptedException错误
             * 例子：
             *  [        order-1]  : 执行了order:Order(id=1, name=order1, price=12),时间：Sat Apr 03 14:13:17 CST 2021
             *  [        order-2]  : 执行了order:Order(id=1, name=order2, price=123),时间：Sat Apr 03 14:13:17 CST 2021
             *  [        order-3]  : 执行了order:Order(id=1, name=order3, price=123),时间：Sat Apr 03 14:13:18 CST 2021
             *  [        order-1]  : 执行了order:Order(id=1, name=order1, price=12),时间：Sat Apr 03 14:13:19 CST 2021
             *  [        order-2]  : 执行了order:Order(id=1, name=order2, price=123),时间：Sat Apr 03 14:13:19 CST 2021
             *  [        order-3]  : 执行了order:Order(id=1, name=order3, price=123),时间：Sat Apr 03 14:13:20 CST 2021
             */
            //doRunnable(orders,true);
            //ok = doCallable(orders,true);
        }
        /*
         * 写在这里的多线程执行，每条数据都会有新的线程来处理
         * 例子：
         * [        order-1]  : 计算order:Order(id=1, name=order1, price=12),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-2]  : 计算order:Order(id=1, name=order2, price=123),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-3]  : 计算order:Order(id=1, name=order3, price=123),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-4]  : 计算order:Order(id=1, name=order1, price=12),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-5]  : 计算order:Order(id=1, name=order2, price=123),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-6]  : 计算order:Order(id=1, name=order3, price=123),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-7]  : 计算order:Order(id=1, name=order1, price=12),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-8]  : 计算order:Order(id=1, name=order2, price=123),时间:Sat Apr 03 15:15:40 CST 2021
         * [        order-9]  : 计算order:Order(id=1, name=order3, price=123),时间:Sat Apr 03 15:15:40 CST 2021
         * [       order-10]  : 计算order:Order(id=1, name=order1, price=12),时间:Sat Apr 03 15:15:40 CST 2021
         */
        doRunnable(orders,false);
        //ok = doCallable(orders,false);
        return String.valueOf(ok);
    }

    /**
     * 使用Runnable实现多线程
     *
     * @param orders 队列
     */
    private void doRunnable(BlockingQueue<Order> orders, boolean isLoop) {
        if (isLoop){
            taskExecutor.execute(new OrderRunnableLoop(orders));
        }else {
            taskExecutor.execute(new OrderRunnable(orders));
        }
    }

    /**
     * 通过Callable接口实现多线程
     * Callable while(true)的话，不能return true 否则循环打破了，因此只需要判断 异常停止的情况 即 返回值 = false，成功默认为true
     *
     * @param orders 队列
     * @return 返回是否执行成功
     */
    private boolean doCallable(BlockingQueue<Order> orders, boolean isLoop) {
        Future<Boolean> submit;
        boolean ok = true;
        if (isLoop) {
            submit = taskExecutor.submit(new OrderCallableLoop(orders));
        } else {
            submit = taskExecutor.submit(new OrderCallable(orders));
        }
        if (submit.isDone()) {
            try {
                ok = submit.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }
}
