import java.io.IOException;
import java.util.logging.*;
/*
该函数专门用于记录  消息缓冲区处理事件.txt
 */
public class FileUtils3 {
    public static final Logger logger = Logger.getLogger(FileUtils3.class.getName());

    static {
        try {
            // 创建 FileHandler，将日志输出到 output 文件中
            FileHandler fileHandler = new FileHandler("output/消息缓冲区处理事件.txt", true);
            // 设置自定义格式化器，将仿真时间添加到日志中
            fileHandler.setFormatter(new FileUtils3.CustomFormatter());
            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
            // 设置日志级别：记录 INFO 及以上级别的日志
            logger.setLevel(Level.INFO);
            // 可选择关闭默认的控制台输出
            logger.setUseParentHandlers(false);
            logger.log(Level.INFO,"消息缓冲区处理事件");
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
    public static void copyInBuffer(Instruction instruction) {
        String message = String.format("%d:[拷贝入缓冲区: 进程ID: %d, 指令编号: %d, 所操作的缓冲区编号: %d]",
                ClockInterruptHandlerThread.getCurrentTime(), instruction.getSendId() ,instruction.getInstruction_ID(), instruction.getBufferId());
        logger.log(Level.INFO, message);
    }

    // 进程结束信息
    public static void copyOutBuffer(Instruction instruction) {
        String message = String.format("%d:[拷贝出缓冲区: 进程ID: %d, 指令编号: %d, 所操作的缓冲区编号: %d]",
                ClockInterruptHandlerThread.getCurrentTime(), instruction.getSendId() ,instruction.getInstruction_ID(), instruction.getBufferId());
        logger.log(Level.INFO, message);
    }

    //缓存区空闲
    public static void bufferFree()
    {
        String message = String.format("%d: [缓冲区无进程]",
                ClockInterruptHandlerThread.getCurrentTime());
        logger.log(Level.INFO, message);
    }

    //记录P操作之后空缓冲区信号量
    public static void recordEmptyP()
    {
        //信号变量名称 = 具体数组
        String message = String.format("%d: [P操作: 空缓冲区单元信号量 = %d]",
                ClockInterruptHandlerThread.getCurrentTime(),  MessageBuffer.empty.availablePermits());
        logger.log(Level.INFO, message);
    }

    //记录V操作之后的空缓冲区的信号量
    public static void recordEmptyV()
    {
        String message = String.format("%d: [V操作: 空缓冲区单元信号量 = %d]",
                ClockInterruptHandlerThread.getCurrentTime(),  MessageBuffer.empty.availablePermits());
        logger.log(Level.INFO, message);
    }

    //记录P操作之后的满缓冲区信号量
    public static void recordFullP()
    {
        //信号变量名称 = 具体数组
        String message = String.format("%d: [P操作: 满缓冲区单元信号量 = %d]",
                ClockInterruptHandlerThread.getCurrentTime(),  MessageBuffer.full.availablePermits());
        logger.log(Level.INFO, message);
    }

    //记录V操作满缓冲区信号量
    public static void recordFullV()
    {
        String message = String.format("%d: [V操作: 满缓冲区单元信号量 = %d]",
                ClockInterruptHandlerThread.getCurrentTime(),  MessageBuffer.full.availablePermits());
        logger.log(Level.INFO, message);
    }

    //记录P操作之后互斥信号量
    public static void recordMutexP()
    {
        //信号变量名称 = 具体数组
        String message = String.format("%d: [P操作: 互斥信号量 = %d]",
                ClockInterruptHandlerThread.getCurrentTime(),  MessageBuffer.mutex.availablePermits());
        logger.log(Level.INFO, message);
    }

    //记录V操作之后互斥信号量
    public static void recordMutexV()
    {
        String message = String.format("%d: [V操作: 互斥信号量 = %d]",
                ClockInterruptHandlerThread.getCurrentTime(),  MessageBuffer.mutex.availablePermits());
        logger.log(Level.INFO, message);
    }

}
