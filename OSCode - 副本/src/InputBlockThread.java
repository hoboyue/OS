/*
进入阻塞线程，处理屏幕显示阻塞和键盘输入阻塞，主要任务为进入此线程之后2s，进行唤醒
 */

public class InputBlockThread extends Thread{
    public void run() {
        while (true) {
            SyncManager.lock.lock();
            try {

//                System.out.println(ClockInterruptHandlerThread.getCurrentTime() + "------------------------------等待阻塞唤醒信号...");
                SyncManager.ioCondition.await();
//                System.out.println(ClockInterruptHandlerThread.getCurrentTime() + "--------开始进行阻塞唤醒----------");

                //进行尝试唤醒
                ResumeBlocked();
                // 唤醒等待在时钟中断处理线程上的线程
                SyncManager.clkCondition.signalAll();
                SyncManager.pstCondition.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                SyncManager.lock.unlock(); // 释放锁
            }
        }
    }

    public void ResumeBlocked() {
//        检查并唤醒 Block1Queue 中的进程，使用while，方便一次性找出所有符合以及完成2s阻塞的进程
        while (!OSKernel.Block1Queue.isEmpty()) {
            PCB processFromBlock1 = OSKernel.Block1Queue.peek(); // 查看队列中的第一个进程，但不移除
            if(ALL_IO(processFromBlock1)) {
                processFromBlock1 = OSKernel.Block1Queue.poll(); // 阻塞时间足够，移除进程
                PCB.resumeProcess(processFromBlock1);
                processFromBlock1.setResumeTime(ClockInterruptHandlerThread.getCurrentTime());
                FileUtils2.logSaveBlocks1(processFromBlock1);
            }
            else {
                break;
            }
        }

        // 检查并唤醒 Block2Queue 中的进程
        while (!OSKernel.Block2Queue.isEmpty()) {
            PCB processFromBlock2 = OSKernel.Block2Queue.peek(); // 查看队列中的第一个进程，但不移除
            if(ALL_IO(processFromBlock2)) {
                processFromBlock2 = OSKernel.Block2Queue.poll(); // 阻塞时间足够，移除进程
                PCB.resumeProcess(processFromBlock2);
                processFromBlock2.setResumeTime(ClockInterruptHandlerThread.getCurrentTime());
                FileUtils2.logSaveBlocks2(processFromBlock2);
            }
            else {
                break;
            }
        }
    }

    public boolean ALL_IO(PCB pcb) {
        // 当时间差达到或超过 2 秒，解除阻塞
        if(ClockInterruptHandlerThread.getCurrentTime() - pcb.getBlockTime() >= 2)
        {
            return true;
        }
        return false;
    }
}
