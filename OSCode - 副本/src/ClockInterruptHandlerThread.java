import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/*
时钟线程，控制系统仿真时间以及作业调度，同时发出条件信号唤醒
 */
public class ClockInterruptHandlerThread extends Thread {

    // 共享时间变量
    public static int simulationTime = 0;//系统当前时间， 以秒为单位
    public static final int MILLISECONDS = 100; //表示1秒的毫秒数
    public static volatile boolean isPause = false; //暂停位，用于控制执行和暂停按钮的标志位，volatile保证其他线程能够及时收到标志位的变化
    static final Lock pauselock = new ReentrantLock(); // 独立的锁控制
    static final Condition pauseCondition = pauselock.newCondition(); // 条件变量控制暂停和执行，实现暂停和执行的按钮功能

    @Override
    public void run() {
        while (true) {
            {
                SyncManager.lock.lock();
                try {
                    // 检查暂停状态
                    while (isPause) {
                        pauselock.lock();
                        try {
                            pauseCondition.await(); // 在暂停状态下进入等待
                        } finally {
                            pauselock.unlock();
                        }
                    }
                    //每秒记录GUI的时间变化
                    recordGUI.recordClockDisplayArea(ClockInterruptHandlerThread.getCurrentTime());

                    //时间增1
                    simulateTimePassing();

                    //模拟时钟每秒激活一次
                    Thread.sleep(MILLISECONDS);

                    //将时钟的更新和msg条件变量关联，实现每秒进行receive查询
                    SyncManager.msgCondition.signal();

                    //唤醒作业调度
                    SyncManager.pstCondition.signal();

                    //每秒都去检查是否有满足阻塞唤醒的进程，如果有就唤醒
                    SyncManager.ioCondition.signal();

                    //每秒输出缓冲区的状态
                    MessageBuffer.isBufferEmpty();
//                    System.out.println("----------------------");

                    //作业查询
                    handleJobRequests();
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime() + "-------时钟等待唤醒-----");

                    // 等待时钟中断信号
                    SyncManager.clkCondition.await();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    SyncManager.lock.unlock();
                }
            }
        }
    }

    //实现整个系统停止调度，暂停按钮
    public static void set_Pause() {
        pauselock.lock();
        try {
            isPause = true; // 设置暂停状态
        } finally {
            pauselock.unlock();
        }
    }

    //实现整个系统执行，执行按钮
    public static void set_Execute() {
        pauselock.lock();
        try {
            isPause = false; // 取消暂停状态
            pauseCondition.signalAll(); // 唤醒暂停的线程
        } finally {
            pauselock.unlock();
        }
    }

    public static int getCurrentTime() {
        return simulationTime;
    }

    //时钟计时器
    public static void simulateTimePassing() {
        SyncManager.lock.lock();
        try {
            simulationTime++;
        } finally {
            SyncManager.lock.unlock();
        }
    }

    public static void JobRequest() {
        SyncManager.lock.lock();
        try {
            int currentTime = getCurrentTime();
            //存放当前时间上满足有job变为PCB的作业列表
            List<Job> jobsToProcess = new ArrayList<>();

            for (Job job : OSKernel.jobRequests) {
                //作业到达时间和系统时间进行比较，是否从临时队列进入后备队列
                if (job.getInTimes() <= currentTime) {
                    //设置进程处理开始时间
                    job.setProcessTime(ClockInterruptHandlerThread.getCurrentTime());
                    jobsToProcess.add(job);
//                        System.out.println(job.getJobsID() + "开始进入后备队列");
                    //记录日志和GUI
                    FileUtils1.logNewJob(job);
                    recordGUI.recordJobRequestArea(job);
                }
            }

            // 将临时集合的元素加入到队列
            OSKernel.backupQueue.addAll(jobsToProcess);

            // 移除 `OSKernel.jobRequests` 中对应的元素
            OSKernel.jobRequests.removeAll(jobsToProcess);

            //如果后备队列有作业，就尝试去为它创建PCB
            while (!OSKernel.backupQueue.isEmpty()) {
                PCB.createProcess(OSKernel.backupQueue.poll());
            }

            //当那些因为内存不足或pcbtable已达最大上限而放入tmpQueue的内容不空，将其放入后备队列
            while (!OSKernel.tmpQueue.isEmpty()) {
                OSKernel.backupQueue.add(OSKernel.tmpQueue.poll());
            }
        } finally {
            SyncManager.lock.unlock();
        }
    }

    private void handleJobRequests() {
        SyncManager.lock.lock();
        try{
            //实现每两秒查询一次作业
            if (ClockInterruptHandlerThread.getCurrentTime() % 2 == 0) {
//                System.out.println(ClockInterruptHandlerThread.getCurrentTime() + ":[JRT]");
                JobRequest();
            }
//            System.out.println("进来了");
            // 有了作业，就要通知进程调度线程
            SyncManager.pstCondition.signalAll();
        }
        finally{
            SyncManager.lock.unlock();
        }

    }
}