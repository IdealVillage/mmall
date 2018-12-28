package com.mall.task;

import com.mall.common.Const;
import com.mall.service.IOrderService;
import com.mall.util.PropertiesUtil;
import com.mall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloseOrderTask {
    @Autowired
    private IOrderService  iOrderService;

//    @Scheduled(cron = "0 */1 * * * ?")
//    public void closeOrderTaskV1(){
//        log.info("关闭订单任务开始");
//        int hour = Integer.parseInt(PropertiesUtil.getPropery("close.order.task.time", "1"));
//        iOrderService.closeOrder(hour);
//        log.info("关闭订单任务结束");
//    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务开始");
        long lockTimeout = Long.parseLong(PropertiesUtil.getPropery("lock.timeout", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.Redis_Lock.ORDER_CLOSE_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult!=null&&setnxResult.intValue()==1){
            closeOrder(Const.Redis_Lock.ORDER_CLOSE_TASK_LOCK);
        }else {
            log.error("获取锁失败:{}", Const.Redis_Lock.ORDER_CLOSE_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    public void closeOrder(String lockName){
        //s
        RedisShardedPoolUtil.expire(lockName, 50);
        log.info("获取锁:{},Thread:{}", lockName, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getPropery("close.order.task.time", "1"));
        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(lockName);
        log.info("释放锁:{},Thread:{}", lockName, Thread.currentThread().getName());
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3(){
        log.info("关闭订单定时任务开始");
        String lockName=Const.Redis_Lock.ORDER_CLOSE_TASK_LOCK;
        long lockTimeout = Long.parseLong(PropertiesUtil.getPropery("lock.timeout", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(lockName,
                String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult!=null&&setnxResult.intValue()==1){
            closeOrder(Const.Redis_Lock.ORDER_CLOSE_TASK_LOCK);
        }else {
            String lockValue = RedisShardedPoolUtil.get(lockName);
            if (lockValue!=null&&System.currentTimeMillis()>Long.parseLong(lockValue)){
                //多个tomcat,可能导致值不一致
                String getSetResult = RedisShardedPoolUtil.getset(lockName,
                        String.valueOf(System.currentTimeMillis() + lockTimeout));
                if (getSetResult==null||(getSetResult!=null&& StringUtils.equals(lockValue,getSetResult))){
                    closeOrder(lockName);
                }else{
                    log.info("未能获取到分布式锁:{}",lockName);
                }
            }else {
                log.info("未能获取到分布式锁:{}",lockName);
            }
        }
        log.info("关闭订单定时任务结束");
    }
}
