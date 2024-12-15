import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
/*
整个界面的GUI设置，包括四个按钮监听器
 */
public class GUI extends JFrame {
    // 界面组件定义
    private JButton executeButton;   // 执行按钮
    private JButton pauseButton;      // 暂停按钮
    private JButton saveButton;       // 保存按钮
    private JButton realTimeButton;   // 实时按钮

    public static JLabel clockDisplayLabel;  // 时钟显示区
    public static JTextArea jobRequestArea;  // 作业请求区
    public static JTextArea readyQueueArea;  // 进程就绪区
    public static JTextArea runningArea;     // 进程运行区
    public static JTextArea blockedArea;     // 进程阻塞区
    public static JTextArea messageCommunicationArea;  // 消息通信区
    private JPanel memoryPanel; // 内存区面板
    private JPanel messageBufferPanel; //消息缓冲区面板
    Font largeFont = new Font("SansSerif", Font.PLAIN, 24);  // 大字体，24号

    static int NUM_BLOCKS = 16; // 内存块数量
    static int BLOCK_UNITS = 10; // 每个块中的单元数量
    static int BUFFER_SIZE = 10; //消息缓冲区的数量（最大并发数）
    public static JPanel[][] memoryBlocks; // 存储所有的内存单元格
    public static JPanel[] messageBufferBlocks; // 存储所有的消息内存单元格

    // 构造方法，用于初始化界面
    public GUI() {
        // 设置窗口属性
        setTitle("进程调度仿真程序"); // 窗口标题
        setSize(2000, 1500);            // 窗口大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗口时退出程序
        setLayout(new BorderLayout()); // 使用边界布局管理器
        this.setVisible(true); // 设置可见

        // 创建时钟显示标签
        clockDisplayLabel = new JLabel("0", SwingConstants.CENTER);
        clockDisplayLabel.setFont(new Font("Arial", Font.BOLD, 48));  // 设置字体和大小
        clockDisplayLabel.setOpaque(true);  // 允许设置背景颜色
        clockDisplayLabel.setBackground(Color.WHITE);  // 设置背景颜色
        clockDisplayLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3)); // 设置灰色边框

        // 创建文本区域及其标题
        jobRequestArea = createTextArea();
        readyQueueArea = createTextArea();
        runningArea = createTextArea();
        blockedArea = createTextArea();
        messageCommunicationArea = createTextArea();

        // 创建并设置内存区域
        memoryPanel = new JPanel(new GridLayout(BLOCK_UNITS, NUM_BLOCKS, 1, 1));
        // 使用自定义字体创建标题边框
        memoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 3),  // 边框颜色和粗细
                "内存区",                                       // 标题文本
                TitledBorder.LEFT,                              // 标题对齐方式
                TitledBorder.TOP,                               // 标题位置
                largeFont.deriveFont(Font.BOLD)                                       // 标题字体
        ));
        memoryBlocks = new JPanel[NUM_BLOCKS][BLOCK_UNITS];
        for (int i = 0; i < BLOCK_UNITS; i++) {
            for (int j = 0; j < NUM_BLOCKS; j++) {
                memoryBlocks[j][i] = new JPanel();
                memoryBlocks[j][i].setBackground(new Color(255, 255, 255)); // 初始状态为白色
                memoryBlocks[j][i].setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 3)); // 设置单元格边框
                memoryPanel.add(memoryBlocks[j][i]);
            }
        }

        // 创建并设置消息缓冲区
        messageBufferPanel = new JPanel(new GridLayout(1, BUFFER_SIZE, 1, 1));
        messageBufferPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 3),  // 边框颜色和粗细
                "消息缓冲区",                                       // 标题文本
                TitledBorder.LEFT,                              // 标题对齐方式
                TitledBorder.TOP,                               // 标题位置
                largeFont.deriveFont(Font.BOLD)                                        // 标题字体
        ));
        messageBufferBlocks = new JPanel[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            messageBufferBlocks[i] = new JPanel();
            messageBufferBlocks[i].setBackground(Color.WHITE); // 初始状态为未占用
            messageBufferBlocks[i].setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2)); // 单元格边框
            messageBufferPanel.add(messageBufferBlocks[i]);
        }

        // 创建包含标题和文本区域的面板
        JPanel jobRequestPanel = createLabeledPanel("作业请求区", jobRequestArea);
        JPanel readyQueuePanel = createLabeledPanel("进程就绪区", readyQueueArea);
        JPanel runningPanel = createLabeledPanel("进程运行区", runningArea);
        JPanel blockedPanel = createLabeledPanel("进程阻塞区", blockedArea);
        JPanel messageCommunicationPanel = createLabeledPanel("消息通信区", messageCommunicationArea);

        // 创建按钮面板并设置按钮
        JPanel buttonPanel = new JPanel(new FlowLayout()); // 按钮面板，使用流式布局
        executeButton = new JButton("执行");  // 创建执行按钮
        executeButton.setFont(largeFont.deriveFont(Font.BOLD));
        pauseButton = new JButton("暂停");    // 创建暂停按钮
        pauseButton.setFont(largeFont.deriveFont(Font.BOLD));
        saveButton = new JButton("保存");     // 创建保存按钮
        saveButton.setFont(largeFont.deriveFont(Font.BOLD));
        realTimeButton = new JButton("实时");  // 创建实时按钮
        realTimeButton.setFont(largeFont.deriveFont(Font.BOLD));

        // 为按钮添加事件监听器
        executeButton.addActionListener(new ExecuteButtonListener());
        pauseButton.addActionListener(new PauseButtonListener());
        saveButton.addActionListener(new SaveButtonListener());
        realTimeButton.addActionListener(new RealTimeButtonListener());

        // 将按钮添加到面板中
        buttonPanel.add(executeButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(realTimeButton);

        // 创建顶部的主面板，包含时钟显示区和按钮
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(clockDisplayLabel, BorderLayout.NORTH);  // 时钟区在最上方
        topPanel.add(buttonPanel, BorderLayout.SOUTH); // 按钮在时钟区下方

        // 创建中央面板，用于布局内存区和消息缓冲区以及其他区域
        JPanel centerPanel = new JPanel(new GridLayout(3, 2)); // 3行2列网格布局
        centerPanel.add(messageBufferPanel);  // 第一行第一列：消息缓冲区
        centerPanel.add(memoryPanel);         // 第一行第二列：内存区
        centerPanel.add(jobRequestPanel);     // 第二行第一列：作业请求区
        centerPanel.add(readyQueuePanel);     // 第二行第二列：进程就绪区
        centerPanel.add(runningPanel);        // 第三行第一列：进程运行区
        centerPanel.add(blockedPanel);        // 第三行第二列：进程阻塞区

        // 创建底部的消息通信区面板，并设置其独占一行
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageCommunicationPanel, BorderLayout.CENTER);

        // 将主布局组件添加到窗口中
        add(topPanel, BorderLayout.NORTH);    // 最顶部是时钟区和按钮面板
        add(centerPanel, BorderLayout.CENTER); // 中部是3×2的区域布局
        add(bottomPanel, BorderLayout.SOUTH); // 最底部是消息通信区
    }

    // 创建文本区域
    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea(10, 20); // 创建文本区域，10行20列
        textArea.setEditable(false); // 设置为不可编辑
        textArea.setLineWrap(true); // 自动换行
        textArea.setFont(largeFont.deriveFont(Font.BOLD));  // 设置大字体
        // 创建一个边框
        Border border = BorderFactory.createLineBorder(Color.GRAY, 3); // 创建黑色边框，宽度为1像素
        textArea.setBorder(border); // 设置边框
        return textArea; // 返回创建的文本区域
    }

    // 创建带标题的面板方法
    private JPanel createLabeledPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title);
        label.setFont(largeFont.deriveFont(Font.BOLD)); // 设置为加粗字体
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 可选：设置内边距
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    // 执行按钮监听器功能实现
    private class ExecuteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // 执行进程调度代码
            ClockInterruptHandlerThread.set_Execute(); // 切换时钟线程的状态
        }
    }

    // 暂停按钮监听器功能实现
    private class PauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // 暂停进程调度代码
            ClockInterruptHandlerThread.set_Pause(); // 切换时钟线程的状态
        }
    }

    // 保存按钮监听器功能实现
    private class SaveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Save save = new Save();  // 创建 Save 类的实例
            String filePath1 = "output/作业进程调度事件.txt";
            String filePath2 = "output/消息缓冲区处理事件.txt";
            String filePath3 = "output/状态统计信息.txt";
            String outputDir = "output";  // 输出目录

            save.saveToFile(filePath1, filePath2, filePath3, outputDir);  // 调用保存方法
        }
    }

    // 实时按钮监听器功能实现
    public class RealTimeButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // 生成实时作业的指令集
            List<Instruction> realTimeInstructions = RealTimeJobGenerator.generateRealTimeJob();
            OSKernel.totalJobs++;
            OSKernel.coutJobs++;

            //设置实时作业的sendID属性
            for(Instruction instruction : realTimeInstructions) {
                if(instruction.getInstruction_State() == 3)
                {
                    instruction.setSendId(OSKernel.coutJobs);
                }
            }

            // 将生成的指令集加入到系统的作业队列
            Job realTimeJob = new Job(OSKernel.coutJobs, ClockInterruptHandlerThread.getCurrentTime(), realTimeInstructions.size());
            realTimeJob.setProcessTime(ClockInterruptHandlerThread.getCurrentTime());
            realTimeJob.setInstructions(realTimeInstructions);

            // 添加到作业请求队列中
            OSKernel.jobRequests.add(realTimeJob);

             //获取锁并发出信号，通知其他线程有新的作业请求
            SyncManager.lock.lock();

            if (ClockInterruptHandlerThread.isPause == true) {
                ClockInterruptHandlerThread.set_Execute();
            }
            try {
                SyncManager.clkCondition.signalAll(); // 发出作业调度信号
            } finally {
                SyncManager.lock.unlock(); // 确保锁的释放
            }
        }
    }
}