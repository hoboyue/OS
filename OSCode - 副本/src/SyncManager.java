import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
同步管理类，包含锁、条件变量
*/
public class SyncManager {
    // 共享锁
    public static final Lock lock = new ReentrantLock();

    // 条件变量
    public static final Condition clkCondition = lock.newCondition();
    public static final Condition pstCondition = lock.newCondition();
    public static final Condition ioCondition = lock.newCondition();
    public static final Condition msgCondition = lock.newCondition();
    public static final Condition rdBufCondition = lock.newCondition(); //用于控制消息缓冲区每次访问要1秒时间

}