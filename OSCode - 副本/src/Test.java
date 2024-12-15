import javax.swing.*;
import java.io.File;
import java.io.IOException;
/*
main函数的入口
 */
public class Test {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI(); // 创建GUI实例
            gui.setVisible(true); // 设置可见
        });

        //保证gui完全建立好
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String inputFolderPath = "input4";
        try {
            OSKernel.jobRequests = InstructionLoader.loadJobsFromFolder(inputFolderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        OSKernel.totalJobs = OSKernel.jobRequests.size();
        OSKernel.coutJobs = OSKernel.totalJobs;

        // 启动进程调度线程
        Thread ProcessSchedulingHandlerThread = new ProcessSchedulingHandlerThread();
        ProcessSchedulingHandlerThread.start();

        // 启动时钟中断线程
        Thread clockInterruptHandlerThread = new ClockInterruptHandlerThread();
        clockInterruptHandlerThread.start();

        // 确保进程调度线程和时钟中断线程启动后，再启动后续线程
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MessageThread thread = new MessageThread(new MessageBuffer());
        thread.start();

        //启动进程堵塞唤醒线程
        Thread InputBlockThread = new InputBlockThread();
        InputBlockThread.start();

        Save save = new Save();
        //  设置一个钩子，在 JVM 关闭时执行删除操作，保证output内只有一个txt文件
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            String filePath1 = "output/作业进程调度事件.txt";
            String filePath2 = "output/消息缓冲区处理事件.txt";
            String filePath3 = "output/状态统计信息.txt";
            String outputDir = "output";  // 输出目录
            save.saveToFile(filePath1, filePath2, filePath3, outputDir);  // 调用保存方法

            File file1 = new File("output/作业进程调度事件.txt");
            File file2 = new File("output/消息缓冲区处理事件.txt");
            File file3 = new File("output/状态统计信息.txt");

            if (file1.exists() && file1.delete()) {
                System.out.println("文件 " + file1.getName() + " 已成功删除。");
            } else {
                System.out.println("无法删除文件 " + file1.getName());
            }

            if (file2.exists() && file2.delete()) {
                System.out.println("文件 " + file2.getName() + " 已成功删除。");
            } else {
                System.out.println("无法删除文件 " + file2.getName());
            }

            if (file3.exists() && file3.delete()) {
                System.out.println("文件 " + file3.getName() + " 已成功删除。");
            } else {
                System.out.println("无法删除文件 " + file3.getName());
            }
        }));
    }
}
