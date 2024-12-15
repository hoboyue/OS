import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/*
OS内核，存放各种队列和与OS相关的信息
 */
public class OSKernel {
    public static CPU cpu = new CPU();

    public static int coutJobs = 0;  // 计算共有几个Job，为页面新增作业分配ID
    public static List<Job> jobRequests = new ArrayList<>();  // 临时数组，将作业临时都读入到这个数组里面
    public static Queue<Job> backupQueue = new LinkedList<>(); // 后备队列，用于存放备份的作业
    public static Queue<Job> tmpQueue = new LinkedList<>();  // 临时存放无法创建进程的作业
    public static int totalJobs = 0;  // 计算文件给出的job总数

    //记录输出日志的阻塞队列编号，从编号1开始
    public static int Block1QueueIdx = 1;
    public static int Block2QueueIdx = 1;
    public static int Block3QueueIdx = 1;

    //三个阻塞队列，使用线程安全型阻塞队列
    public static  BlockingQueue<PCB> Block1Queue = new LinkedBlockingQueue<>();
    public static  BlockingQueue<PCB> Block2Queue = new LinkedBlockingQueue<>();
    public static BlockingQueue<PCB> Block3Queue = new LinkedBlockingQueue<>();

    public static Queue<PCB> readyQueue = new LinkedList<>();    // 就绪队列，用于存放已准备好执行的进程
    public static LinkedList<PCB> pcbTable = new LinkedList<>(); //记录生命周期在建立好到终止过程的所以pcb的pid
    public static final int PCBMax = 12; //定义调度最大并发度为 12
}
