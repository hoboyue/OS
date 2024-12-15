import java.io.IOException;
import java.util.logging.*;
/*
该函数专门用于记录  状态统计信息.txt
 */
public class FileUtils2 {
    public static final Logger logger = Logger.getLogger(FileUtils2.class.getName());

    static {
        try {
            // 创建 FileHandler，将日志输出到 output 文件中
            FileHandler fileHandler = new FileHandler("output/状态统计信息.txt", true);
            // 设置自定义格式化器，将仿真时间添加到日志中
            fileHandler.setFormatter(new FileUtils2.CustomFormatter());
            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
            // 设置日志级别：记录 INFO 及以上级别的日志
            logger.setLevel(Level.INFO);
            // 可选择关闭默认的控制台输出
            logger.setUseParentHandlers(false);
            logger.log(Level.INFO,"状态统计信息");
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


    // 进程结束信息
    public static void logProcessEndInfo(PCB pcb) {
        String message = String.format("%d:结束时间: [进程 ID: %d, 作业请求时间: %d, 进入时间: %d, 总运行时间: %d]",
                ClockInterruptHandlerThread.getCurrentTime(), pcb.getPid(),pcb.getAskTime(), pcb.getInTimes(),pcb.getTurnTimes());
        logger.log(Level.INFO, message);
    }

    // 阻塞队列1内进程信息
    public static void logSaveBlocks1(PCB pcb)
    {
        String message = String.format("%d:BB1:[阻塞队列 1,键盘输入:进程 ID: %d 进入时间：%d 唤醒时间: %d]",
                ClockInterruptHandlerThread.getCurrentTime(), pcb.getPid(),pcb.getBlockTime(),pcb.getResumeTime());
        logger.log(Level.INFO, message);
    }

    // 阻塞队列2内进程信息
    public static void logSaveBlocks2(PCB pcb)
    {
        String message = String.format("%d:BB2:[阻塞队列 2,屏幕显示:进程 ID: %d 进入时间：%d 唤醒时间: %d]",
                ClockInterruptHandlerThread.getCurrentTime(), pcb.getPid(),pcb.getBlockTime(),pcb.getResumeTime());
        logger.log(Level.INFO, message);
    }

    // 阻塞队列3内进程信息
    public static void logSaveBlocks3(PCB pcb)
    {
        String message = String.format("%d:BB3:[阻塞队列 3,消息队列:进程 ID: %d 进入时间：%d 唤醒时间: %d]",
                ClockInterruptHandlerThread.getCurrentTime(), pcb.getPid(),pcb.getBlockTime(),pcb.getResumeTime());
        logger.log(Level.INFO, message);
    }

}
