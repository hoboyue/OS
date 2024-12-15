import javax.swing.*;
import java.awt.*;
/*
专门用来记录GUI变化内容的类
SwingUtilities.invokeLater() 在 Swing 应用程序中用于确保 GUI 更新在 事件调度线程（EDT）上执行，防止GUI死锁
 */
public class recordGUI {
    //记录作业请求内容
    public static void recordJobRequestArea(Job job) {
        SwingUtilities.invokeLater(() -> {
            GUI.jobRequestArea.append(job.getProcessTime()+ ":新增作业: 作业编号:" + job.getJobsID() + " 请求时间:" + job.getInTimes() + " 指令数量" + job.getInstrucNum() + "\n");
        });
    }

    //记录作业进入就绪队列
    public static void recordReadyQueueArea(PCB pcb, int readyTime) {
        SwingUtilities.invokeLater(() -> {
            GUI.readyQueueArea.append(readyTime + "进入就绪队列: 进程 ID: " + pcb.getPid() + " 待执行的指令数: " + pcb.getRemainInstructNum() + "\n");
        });
    }

    //记录PCB处在CPU内的状态
    public static void recordRunningArea(PCB pcb, int runningTime) {
        SwingUtilities.invokeLater(() -> {
            GUI.runningArea.append(runningTime + ":运行进程: 进程 ID: " + pcb.getPid() + " 指令编号: " + (pcb.getir() + 1) + "指令类型编号: " + pcb.getInstruction(pcb.getir()).getInstruction_State() + "物理地址:" + pcb.getIrAddress(pcb.getir()) + "数据大小:" + pcb.getInstruction(pcb.getir()).getDataSize() + "B" + "\n");
        });
    }


    //记录进程进入阻塞队列1
    public static void recordBlockedArea1(PCB pcb){
        SwingUtilities.invokeLater(() -> {
            GUI.blockedArea.append(pcb.getBlockTime() + ":阻塞队列1 键盘输入: 进程 ID:" + pcb.getPid() + "阻塞编号：" + pcb.getBlockId() + "阻塞时间：" + pcb.getBlockTime() +"\n");
        });
    }

    //记录进程进入阻塞队列2
    public static void recordBlockedArea2(PCB pcb) {
        SwingUtilities.invokeLater(() -> {
            GUI.blockedArea.append(pcb.getBlockTime() + ":阻塞队列2 屏幕显示: 进程 ID:" + pcb.getPid() + "阻塞编号：" + pcb.getBlockId() + "阻塞时间：" + pcb.getBlockTime() + "\n");
        });
    }

    //记录进程进入阻塞队列3
    public static void recordBlockedArea3(PCB pcb) {
        SwingUtilities.invokeLater(() -> {
            GUI.blockedArea.append(pcb.getBlockTime() + ":阻塞队列3 消息队列: 进程 ID:" + pcb.getPid() + "阻塞编号：" + pcb.getBlockId() + "阻塞时间：" + pcb.getBlockTime() + "\n");
        });
    }

    //记录进程成功发送消息
    public static void recordMessageSucessArea(Instruction instruction){
        SwingUtilities.invokeLater(() -> {
            GUI.messageCommunicationArea.append(ClockInterruptHandlerThread.getCurrentTime() + ":进程" + instruction.getSendId() + "第" + instruction.getInstruction_ID() +"条指令,成功发送200B消息至进程" + instruction.getMesg_Name() + "\n");
        });
    }

    //记录进程没有成功发送消息
    public static void recordMessageFailArea(Instruction instruction){
        SwingUtilities.invokeLater(() -> {
            GUI.messageCommunicationArea.append(ClockInterruptHandlerThread.getCurrentTime() + ":进程" +  instruction.getSendId() + "通信失败！！目标进程 已经终止！"+ "\n");
        });
    }

    //更新时钟
    public static void recordClockDisplayArea(int currentTime){
        SwingUtilities.invokeLater(() -> {
            String Time = String.valueOf(currentTime); // 获取当前时间
            GUI.clockDisplayLabel.setText(Time); // 替换原有文本
        });
    }

    // 更新内存分配显示
    public static void recordAllocateMemoryArea(int startBlock, int units) {
        SwingUtilities.invokeLater(() -> {
            Color color = Color.GREEN; // 分配时使用绿色
            int remainingUnits = units;

            for (int i = startBlock; i < GUI.NUM_BLOCKS && remainingUnits > 0; i++) {
                for (int j = GUI.BLOCK_UNITS - 1; j >= 0 && remainingUnits > 0; j--) {
                    GUI.memoryBlocks[i][j].setBackground(color);
                    remainingUnits--;
                }
            }
        });
    }

    // 更新消息缓冲区分配显示
    public static void recordAllocateBufferArea(int BufferId) {
        SwingUtilities.invokeLater(() -> {
            Color color = Color.RED; // 释放时使用白色
            GUI.messageBufferBlocks[BufferId].setBackground(color);
        });
    }

    // 更新内存释放显示
    public static void recordFreeMemoryArea(int startBlock, int units) {
        SwingUtilities.invokeLater(() -> {
            Color color = Color.WHITE; // 释放时使用白色
            int remainingUnits = units;

            for (int i = startBlock; i < GUI.NUM_BLOCKS && remainingUnits > 0; i++) {
                for (int j = GUI.BLOCK_UNITS - 1; j >= 0 && remainingUnits > 0; j--) {
                    GUI.memoryBlocks[i][j].setBackground(color);
                    remainingUnits--;
                }
            }
        });
    }

    // 更新消息缓冲区释放显示
    public static void recordFreeBufferArea(int BufferId) {
        SwingUtilities.invokeLater(() -> {
            Color color = Color.WHITE; // 释放时使用白色
            GUI.messageBufferBlocks[BufferId].setBackground(color);
        });
    }

}
