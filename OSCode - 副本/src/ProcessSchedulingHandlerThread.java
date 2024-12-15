import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
/*
进程调度线程，实现多级反馈
 */
public class ProcessSchedulingHandlerThread extends Thread {

    private int timeSlice; // 当前时间片
    // 进程队列
    static final Queue<PCB> level1Queue = new ConcurrentLinkedQueue<>(); //第一级
    static final Queue<PCB> level2Queue = new ConcurrentLinkedQueue<>(); //第二级
    static final Queue<PCB> level3Queue = new ConcurrentLinkedQueue<>(); //第三级

    @Override
    public void run() {
        while (true) {
            SyncManager.lock.lock(); // 获取锁，确保线程安全
            try {
                // 等待线程调度信号
                try {
                    SyncManager.pstCondition.await();
                    System.out.println(ClockInterruptHandlerThread.getCurrentTime() + "开始进行多级反馈调度");
                    MFQ();
                } catch (InterruptedException e) {
                    SyncManager.clkCondition.signal();
                    SyncManager.lock.unlock(); // 释放锁
                }

                //如果所有作业执行完毕，则暂停时钟
                if(OSKernel.totalJobs == 0) {
//                    System.out.println("------------*****************-----------------");
//                    System.out.println("----------------时钟应该暂停，不再运行--------------");
                    ClockInterruptHandlerThread.set_Pause();
                }

//                // 唤醒等待在时钟中断处理线程上的线程
//                SyncManager.clkCondition.signal();
            } finally {
                // 唤醒等待在时钟中断处理线程上的线程
                SyncManager.clkCondition.signal();
                SyncManager.lock.unlock(); // 释放锁
            }
        }
    }

    //获得下一个应该被执行的进程
    public static PCB getNextExecuteProcess() {
        if (!level1Queue.isEmpty()) {
//            System.out.println("从第一级调度选取 PCB ID: " + level1Queue.peek() + " 指令数量: " + level1Queue.peek().instructions.size());
            return level1Queue.peek();
        } else if (!level2Queue.isEmpty()) {
//            System.out.println("从第一级调度选取 PCB ID: " + level2Queue.peek() + " 指令数量: " + level2Queue.peek().instructions.size());
            return level2Queue.peek();
        } else if (!level3Queue.isEmpty()) {
//            System.out.println("从第一级调度选取 PCB ID: " + level3Queue.peek() + " 指令数量: " + level3Queue.peek().instructions.size());
            return level3Queue.peek();
        }
        return null;
    }

    //获得相应等级的队列
    private Queue<PCB> getQueueByLevel(int level) {
        if (level == 1) {
            return level1Queue;
        } else if (level == 2) {
            return level2Queue;
        } else {
            return level3Queue;
        }
    }

    //将所要求的PCB从其所在队列移除
    private void pollFromQueue(PCB pcb)
    {   // 从队列中出来
        switch (pcb.getLevel())
        {
            case 1:
                ProcessSchedulingHandlerThread.level1Queue.remove(pcb);
                break;
            case 2:
                ProcessSchedulingHandlerThread.level2Queue.remove(pcb);
                break;
            case 3:
                ProcessSchedulingHandlerThread.level3Queue.remove(pcb);
                break;
        }
    }

    //  根据PCB中执行函数所返回的flag标志，进行下一步
    // （设置理由：因为时间片内会进行循环操作，即一个时间片内执行几条指令，将这部分放到PCB的执行函数容易造成混乱）
    private void handleProcessAfterExecution(PCB process, int flag) {
//        System.out.println("执行后的 flag 值: " + flag);
        if (process.isCompleted()) {
//            System.out.println(process.getPid() + "全部执行完毕");
            pollFromQueue(process);
            MemoryManager.freeMemory(process);
        }

        else if (flag == 2){
//            System.out.println(process.getPid() + "时间片用尽或不足，进入下一级");
            pollFromQueue(process);
            process.promote();
            getQueueByLevel(process.getLevel()).add(process);
//            System.out.println("添加进程 " + process.getPid() + " 到队列 " + process.getLevel());
        }

        else if(flag == 1){
//            System.out.println("进程被抢占，返回当前队列");
            pollFromQueue(process);
            getQueueByLevel(process.getLevel()).add(process);
        }

        else if(flag == 0){
//            System.out.println("进行进程调度切换");
            pollFromQueue(process);
        }
    }

    //根据PCB的level获得时间片大小
    private int getTimeSlice(PCB pcb){
        int timeSlice;
        if (pcb.getLevel() == 1) {
            timeSlice = 1;
        } else if (pcb.getLevel() == 2) {
            timeSlice = 2;
        } else {
            timeSlice = 4;
        }
        return timeSlice;
    }

    //将每一次执行完时间片以及之后的对pcb操作封装成一个函数，使得结构整洁
    public void executeUserOrBlock(PCB pcb){
        if (pcb == null) {
            FileUtils1.logCpuIdle();
        } else {
            OSKernel.cpu.CPU_REC(pcb); // 恢复进程的现场
            int flag;
            try {
                flag = pcb.execute(getTimeSlice(pcb));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            handleProcessAfterExecution(pcb, flag);
        }
}

    //处理进程调度的逻辑。
    private void MFQ() {
        PCB currentProcess = getNextExecuteProcess();
        if (currentProcess == null) {
            FileUtils1.logCpuIdle();
        } else {
            //当是堵塞指令时，循环执行，直到是需要耗时的计算指令，跳出去执行一次计算指令
            while (currentProcess.getInstruction(currentProcess.getir()).getInstruction_State() != 0) {
                executeUserOrBlock(currentProcess);
                currentProcess = getNextExecuteProcess();
                if(currentProcess == null) {
                    FileUtils1.logCpuIdle();
                    break;
                }
            }
            //执行计算指令,仍要判断是否为null，否则会报越界错误
            currentProcess = getNextExecuteProcess();
            if(currentProcess != null) {
                executeUserOrBlock(currentProcess);
            }
        }
    }
}




