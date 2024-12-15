import java.io.IOException;
import java.util.Queue;
import java.util.logging.*;
/*
该函数专门用于记录  作业进程调度事件.txt  使用了Lab文件中提供的logger函数方法
 */
public class FileUtils1 {

    public static final Logger logger = Logger.getLogger(FileUtils1.class.getName());

    static {
        try {
            // 创建 FileHandler，将日志输出到 output 文件中
            FileHandler fileHandler = new FileHandler("output/作业进程调度事件.txt", true);
            // 设置自定义格式化器，将仿真时间添加到日志中
            fileHandler.setFormatter(new CustomFormatter());
            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
            // 设置日志级别：记录 INFO 及以上级别的日志
            logger.setLevel(Level.INFO);
            // 可选择关闭默认的控制台输出
            logger.setUseParentHandlers(false);
            //txt文件的开头记录该txt文件的名称
            logger.log(Level.INFO,"作业进程调度事件");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法创建日志文件处理器", e);
        }
    }

    // 自定义格式化器类
    static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s %n ",
                    record.getMessage());
        }
    }

    // 新增作业
    public static void logNewJob(Job job) {
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[新增作业: 作业编号: %d, 请求时间: %s, 指令数量: %d]", job.getJobsID(), job.getInTimes(),job.getInstrucNum());
        logger.log(Level.INFO, message);
    }

    // 创建进程
    public static void logCreateProcess(PCB pcb) {
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[创建进程: 进程 ID: %d, PCB 内存块始地址: %d, 分配内存大小: %d]", pcb.getPid(), pcb.getBaseAddress(), pcb.getMemorySize());
        logger.log(Level.INFO, message);
    }

    // 进入就绪队列
    public static void logReadyQueue(PCB pcb) {
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[进入就绪队列: 进程 ID: %d, 待执行的指令数: %d]", pcb.getPid(), pcb.getRemainInstructNum());
        logger.log(Level.INFO, message);
    }

    // 运行进程
    public static void logRunProcess(PCB pcb) {
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[运行进程: 进程 ID: %d, 指令编号: %d, 指令类型编号: %d, 物理地址: %d, 数据大小: %dB]",
                pcb.getPid(), (pcb.getir() + 1) ,pcb.getInstruction(pcb.getir()).getInstruction_State(), pcb.getIrAddress(pcb.getir()) ,pcb.getInstruction(pcb.getir()).getDataSize());
        logger.log(Level.INFO, message);
    }

    // 阻塞进程
    public static void logBlockProcess(Queue<PCB> BlockQueue, String str) {
        String ids = joinBlockQueueId(BlockQueue, "/");
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[阻塞进程: 阻塞队列编号: %s, 进程 ID 列表: %s]", str, ids);
        logger.log(Level.WARNING, message);
    }

    // 重新进入就绪队列
    public static void logResumeProcess(Queue<PCB> readyQueue) {
        String ids = joinReadyQueueId(readyQueue, "/");
        String instructions = joinReadyQueueRemainInstruc(readyQueue, "/");
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[重新进入就绪队列: 进程 ID 列表: %s, 剩余未执行指令数: %s]", ids, instructions);
        logger.log(Level.INFO, message);
    }

    // CPU 空闲
    public static void logCpuIdle() {
        logger.log(Level.INFO, ClockInterruptHandlerThread.getCurrentTime() + ":[CPU 空闲]");
    }

    // 终止进程
    public static void logTerminateProcess(PCB pcb) {
        String message = String.format(ClockInterruptHandlerThread.getCurrentTime() + ":[终止进程 ID: %d]", pcb.getPid());
        logger.log(Level.INFO, message);
    }


    // 辅助方法：将阻塞队列元素用指定分隔符拼接成字符串
    public static String joinBlockQueueId(Queue<PCB> BlockQueue, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for(PCB pcb : BlockQueue) {
            int id = pcb.getPid();
            builder.append(id);
            builder.append(delimiter);
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    // 辅助方法：将就绪队列元素用指定分隔符拼接成字符串
    public static String joinReadyQueueId(Queue<PCB> ReadyQueue, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for(PCB pcb : ReadyQueue) {
            int id = pcb.getPid();
            builder.append(id);
            builder.append(delimiter);
        }
        if(builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    // 辅助方法：将就绪队列元素用指定分隔符拼接成字符串
    public static String joinReadyQueueRemainInstruc(Queue<PCB> ReadyQueue, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for(PCB pcb : ReadyQueue) {
            int id = pcb.getRemainInstructNum();
            builder.append(id);
            builder.append(delimiter);
        }
        if(builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}


