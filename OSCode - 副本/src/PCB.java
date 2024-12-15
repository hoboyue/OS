import java.util.concurrent.BlockingQueue;
/*
PCB类，包括了创建，阻塞，唤醒，终止原语与PCB进程执行函数
 */
public class PCB extends Job {
    private int pid; //进程ID
    private int pc; // 程序计数器
    private int PSW; // 进程的状态
    private int askTime; //作业请求时间 -- 作业请求不一定就能分配到内存
    private int inTimes; // 进程创建时间
    private int endTimes; // 进程结束时间
    private int turnTimes; // 进程周转时间
    private int instrucNum; // 进程包含的指令数目
    private int ir; // 指令寄存器 (当前正在执行的指令)
    public int level; //多级调度的级别
    private int blockTime; // 记录进入堵塞队列的时间
    private int blockId;//记录进入阻塞队列的顺序
    private int resumeTime; //记录唤醒时间
    private int baseAddress = 0; // 记录基地址
    private int physicalAddress = 0; //物理地址
    private int countMemory = 0; // 记录用户操作语句（100B）的有多少条
    private int userIndex = 0; //记录用户操作的语句执行到了第几条
    private int MemorySize; // 该PCB所占的内存大小
    private int[] allocatedBlocks; // 分配的内存块编号形成的数组
    private byte[] messageRecv; // 接收的200B消息
    private byte[] messageSend; // 发送的200B消息

    public PCB(int jobId, int inTime, int instructionCount, int level) {
        super(jobId, inTime, instructionCount);
        this.pid = jobId;
        this.pc = 0;
        this.ir = 0; // 默认没有执行指令
        this.PSW = 0; // 0是就绪态， 1是运行态， 2是阻塞态
        this.inTimes = inTime;
        this.turnTimes = 0;
        this.instrucNum = instructionCount;
        this.level = level;
        this.MemorySize = this.countMemory * 100;
    }

    //一系列set和get函数
    public void setMessageRecv(byte[] messageRecv) {this.messageRecv = messageRecv;}
    public byte[] getMessageRecv() {return messageRecv;}

    public void setMessageSend(byte[] messageSend) {this.messageSend = messageSend;}
    public byte[] getMessageSend() {return messageSend;}

    //返回计算指令有多少个（真正占据内存的指令）
    public int getCountMemory(Job job) {
        int count = 0;
        for (Instruction instruction : job.instructions) {
            if (instruction.getInstruction_State() == 0)
                count++;
        }
        return count;
    }

    public void setMemorySize(int count) {
        this.MemorySize = count * 100;
    }
    public int getMemorySize() {
        return this.MemorySize;
    }

    public void setPhysicalAddress(int physicalAddress) {
        this.physicalAddress = physicalAddress;
    }
    public int getPhysicalAddress(){
        return this.physicalAddress;
    }

    public void setEndTimes(int endTimes) {
        this.endTimes = endTimes;
    }

    public void setAskTime(int askTime) {
        this.askTime = askTime;
    }
    public int getAskTime() {
        return askTime;
    }

    public void setInTimes(int inTimes) {
        this.inTimes = inTimes;
    }

    public int getPid() {
        return this.pid;
    }

    public Instruction getInstruction(int ir) {
        return instructions.get(ir);
    }

    public int getRemainInstructNum() {
        return this.instrucNum - this.ir;
    }

    public int getBlockId() {
        return this.blockId;
    }
    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
    public int getBlockTime() {
        return this.blockTime;
    }

    public int getResumeTime() {
        return this.resumeTime;
    }

    // 计算并更新周转时间 (Turnaround Time = 进程结束时间 - 进程创建时间)
    public void updateTurnTimes() {
        this.turnTimes = this.endTimes - this.inTimes;
    }

    public int getTurnTimes() {
        return this.turnTimes;
    }

    public int getCountMemory() {
        return countMemory;
    }

    // 设置程序计数器 (指向下一条指令)
    public void setPC(int pc) {
        this.pc = pc;
    }

    public int getPC() {
        return this.pc;
    }

    public void setBlockTime(int blockTime) {
        this.blockTime = blockTime;
    }

    public void setResumeTime(int resumeTime) {
        this.resumeTime = resumeTime;
    }

    public int getIrAddress(int ir) {
        int count = 0;
        for (int i = 0; i < ir; i++) {
            if (this.instructions.get(i).getInstruction_State() == 0) {
                count++;
            }
        }
        return count * 100 + this.getBaseAddress();

    }


    public void setAllocatedBlocks(int[] allocatedBlocks) {
        this.allocatedBlocks = allocatedBlocks;
    }

    public int[] getAllocatedBlocks() {
        return allocatedBlocks;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getBaseAddress() {
        return this.baseAddress;
    }

    public int getUserIndex() {
        return this.userIndex;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setInstrucNum(int instrucNum) {
        this.instrucNum = instrucNum;
    }

    public void setTurnTimes(int turnTimes) {
        this.turnTimes = turnTimes;
    }

    public int getPSW() {
        return this.PSW;
    }

    //获得指令
    public int getir() {
        return this.ir;
    }

    // 设置指令寄存器 (当前执行的指令)
    public void setIR(int ir) {
        this.ir = ir;
    }

    // 设置进程状态 (运行, 就绪, 阻塞)
    public void setPSW(int PSW) {
        this.PSW = PSW;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private boolean hasHigherPriorityProcess() {
        // 检查是否有更高优先级的任务在队列中
        if (this.getLevel() == 2 && !ProcessSchedulingHandlerThread.level1Queue.isEmpty()) {
            return true;  // 如果当前任务是第二级，并且第一级不空，则抢占
        } else if (this.getLevel() == 3 && (!ProcessSchedulingHandlerThread.level2Queue.isEmpty() || !ProcessSchedulingHandlerThread.level1Queue.isEmpty())) {
            return true;  // 如果当前任务是第三级，并且第一级或第二级不空，则抢占
        }
        return false;
    }

    //执行timeSlice
    public synchronized int execute(int timeSlice) throws InterruptedException {
        int timeConsumed = 0;
        //flag = 0代表阻塞或终止完成了，最终要将它poll
        //flag = -1代表指令还没有完成，直接跳过本次
        //flag = 1代表被抢占而终止
        //flag = 2代表时间片用完，要降级
        int flag = -1;
//        System.out.println("ir为" + this.ir + "指令大小为" + instructions.size());
        while (timeConsumed < timeSlice && this.ir < instructions.size()) {
//            System.out.println("要开始执行了------" + timeConsumed);
//            System.out.println("--------开始进行EXECUTE--------");
            Instruction instruction = instructions.get(ir);
            int instructionTime = instruction.getInRunTimes();
            // 在执行指令之前检查是否有高优先级任务到达
            if (hasHigherPriorityProcess()) {
//                System.out.println("有更高优先级的任务到达，抢占当前进程");
                flag = 1;  // 标记为需要抢占
                break;
            }

            if (this.ir == instructions.size()) terminateProcess(this);
            if (instructionTime <= timeSlice - timeConsumed) { // 确保有足够的时间片执行当前指令
//                System.out.println("----------进入时间片---------");
//                System.out.println("当前指令执行时间: " + instructionTime + "，剩余时间片: " + (timeSlice - timeConsumed));
                if (instruction.getBlockFlag() == 1) {
//                    System.out.println(this.getPid() + "进程进入阻塞队列1");
                    OSKernel.readyQueue.remove(this);
                    blockProcess(this, OSKernel.Block1Queue, "键盘输入变量指令阻塞1");
                    this.setBlockId(OSKernel.Block1QueueIdx);
                    OSKernel.Block1QueueIdx++;
                    this.setBlockTime(ClockInterruptHandlerThread.getCurrentTime());
                    recordGUI.recordBlockedArea1(this);
                    flag = 0;
                    break; // 遇到阻塞指令时退出循环
                }

                else if (instruction.getBlockFlag() == 2) {
//                  System.out.println(this.getPid() + "进程进入阻塞队列2");
                    OSKernel.readyQueue.remove(this);
                    blockProcess(this, OSKernel.Block2Queue, "屏幕显示输出指令阻塞2");
                    this.setBlockId(OSKernel.Block2QueueIdx);
                    OSKernel.Block2QueueIdx++;
                    this.setBlockTime(ClockInterruptHandlerThread.getCurrentTime());
                    recordGUI.recordBlockedArea2(this);
//                    OSKernel.readyQueue.remove(this);
                    flag = 0;
                    break; // 遇到阻塞指令时退出循环
                }

                else if(instruction.getBlockFlag() == 3) {
                    if (MessageThread.Send(instruction.getMesg_Name(), instruction.getSendId(), instruction.getInstruction_ID()) != -1) {
//                      System.out.println("收件人是" + instruction.getMesg_Name() + "发送人是" + instruction.getSendId() + "发送指令是第" + instruction.getInstruction_ID());
//                      MessageThread.Send(instruction.getMesg_Name(), instruction.getSendId(), instruction.getInstruction_ID());
                        OSKernel.readyQueue.remove(this);
                        blockProcess(this, OSKernel.Block3Queue, "消息队列指令阻塞3");
                        this.setBlockId(OSKernel.Block3QueueIdx);
                        OSKernel.Block3QueueIdx++;
                        this.setBlockTime(ClockInterruptHandlerThread.getCurrentTime());
                        recordGUI.recordBlockedArea3(this);
                        flag = 0;
                        break; // 遇到阻塞指令时退出循环
                    }
                    else{
                        OSKernel.cpu.runProcess(this); // 更新pc和ir
                        flag = 0;
                    }
                }

                else {
                    //执行指令
                    if(instruction.getStartTime() == 0)
                    {
                        //设置指令开始时间
                        instruction.setStartTime(ClockInterruptHandlerThread.getCurrentTime());
//                        System.out.println("指令开始时间是" + ClockInterruptHandlerThread.getCurrentTime());
                        //只有用户指令才会进入CPU运行
                        if(instruction.getInstruction_State() == 0)
                        {
                            FileUtils1.logRunProcess(this);
                            recordGUI.recordRunningArea(this, ClockInterruptHandlerThread.getCurrentTime());
                            setPSW(1);//设置为执行态
                        }
                    }
//                    System.out.println("当前时间是" + ClockInterruptHandlerThread.getCurrentTime() + "   " + instruction.getInstruction_ID() + "指令开始时间是" + instruction.getStartTime());
                    while(ClockInterruptHandlerThread.getCurrentTime() - instruction.getStartTime() < instructionTime)
                    {
                        //直接堵塞在这里
                       SyncManager.pstCondition.await();
                    }
//                    System.out.println("----------------进去了-------------");
                    OSKernel.cpu.runProcess(this); // 更新pc和ir
                    OSKernel.cpu.CPU_PRO(this);

//                  检查是否所有指令已执行完毕
                    if (this.ir >= this.instructions.size()) {
//                       System.out.println("所有指令执行完毕, 进程完成");
                        flag = 0;  // 进程已完成
                        break;
                    }

                    if (instructionTime == timeSlice - timeConsumed) //当时间片刚好够这个指令的时候，也得切换时间片
                    {
                        flag = 2;
                        break;
                    }

                    else {
//                            System.out.println("--------------更新前的时间是" + timeConsumed);
                        timeConsumed += instructionTime; // 更新已使用的时间
//                            System.out.println("--------------更新后的时间是" + timeConsumed);
                    }
                }
            }

            else {
                flag = 2; //表示需要进行进程调度切换
                break;
            }

        }
        return flag;
    }

    // 检查进程是否完成
    public boolean isCompleted() {
        return getir() >= getInstrucNum();
    }

    public void promote() {
        if (this.level < 3) {
//            System.out.println("进行降级");
            this.level++;
        }
    }

    //进程创建原语
    public static PCB createProcess(Job job) {
        PCB pcb = new PCB(job.getJobsID(), ClockInterruptHandlerThread.getCurrentTime(), job.getInstrucNum(), 1);
        pcb.setAskTime(job.getInTimes());
//        System.out.println("PCBID " + pcb.getPid() + " 到达时间 " + pcb.getAskTime());
        pcb.setInstructions(job.getInstructions());
        pcb.setMemorySize(pcb.getCountMemory(job));
        int baseAddress = MemoryManager.allocateMemory(pcb);

        if (baseAddress != -1 && OSKernel.pcbTable.size() < OSKernel.PCBMax) {   //成功分配了内存
            pcb.setBaseAddress(baseAddress);
            pcb.setPhysicalAddress(MemoryManager.logicalToPhysical(pcb));
//            System.out.println("分配的内存块为：");
//            for(int i = 0; i < pcb.allocatedBlocks.length; i ++) System.out.println(pcb.allocatedBlocks[i]);
            FileUtils1.logCreateProcess(pcb); // 日志记录创建新进程
            pcb.setPSW(0); //就绪态  只要刚开始创建就是就绪态，构造函数里面本来就是0
            ProcessSchedulingHandlerThread.level1Queue.add(pcb);
//            System.out.println("完成创建之后进入第一级队列的指令数量" + pcb.instructions.size());
            OSKernel.readyQueue.add(pcb);
//            for(Instruction instruction : pcb.getInstructions())
//            {
//                System.out.println("指令ID" + instruction.getInstruction_ID() + "指令状态" + instruction.getInstruction_State() + "\n");
//            }
            FileUtils1.logReadyQueue(pcb);
            recordGUI.recordReadyQueueArea(pcb, ClockInterruptHandlerThread.getCurrentTime());
            OSKernel.pcbTable.add(pcb);
//            System.out.println(pcb.getPid() + "已经加入系统的pcb表");
            pcb.setMemorySize(pcb.getCountMemory(job));
        } else {
//            System.out.println("内存不够分配");
            //不够分配，进入临时队列
            OSKernel.tmpQueue.add(job);
        }
        return pcb;
    }

    //终止原语
    public static void terminateProcess(PCB pcb) {
        pcb.setEndTimes(ClockInterruptHandlerThread.getCurrentTime());
        pcb.setTurnTimes(pcb.endTimes - pcb.inTimes);
        pcb.setEndTimes(ClockInterruptHandlerThread.getCurrentTime());
        MemoryManager.freeMemory(pcb);
        OSKernel.totalJobs--;
        FileUtils1.logTerminateProcess(pcb);
        FileUtils2.logProcessEndInfo(pcb);
        OSKernel.pcbTable.remove(pcb);
        OSKernel.readyQueue.remove(pcb);
//        System.out.println(pcb.getPid() + "已经移出系统的pcb表");
    }

    //阻塞原语
    public void blockProcess(PCB pcb, BlockingQueue<PCB> BlockQueue, String str) {
        pcb.setPSW(2);
        //就绪队列弹出
        OSKernel.cpu.CPU_PRO(pcb); // 保护现场
        BlockQueue.add(pcb);
//        System.out.println("==============" + BlockQueue.size());
        FileUtils1.logBlockProcess(BlockQueue, str);
    }

    //唤醒原语，实行“恢复优先级”策略，（txt文件中阻塞语句较多，若实行“提升优先级策略”，会有饥饿现象出现）
    public static void resumeProcess(PCB pcb) //过2秒就唤醒
    {
//            System.out.println("--------开始进程唤醒---------");
        switch (pcb.getLevel()) {   // 回到阻塞发生时的就绪队列队尾
            case 1:
                ProcessSchedulingHandlerThread.level1Queue.add(pcb);  // 1级反馈队列
                break;
            case 2:
                ProcessSchedulingHandlerThread.level2Queue.add(pcb);  // 2级反馈队列
                break;
            case 3:
                ProcessSchedulingHandlerThread.level3Queue.add(pcb);  // 3级反馈队列
                break;
        }
        Instruction instruction = pcb.instructions.get(pcb.getir());
        //设置阻塞标志位为状态0，防止再次因为状态而被阻塞
        instruction.setBlockFlag(0);
        pcb.setPSW(1);//设置为就绪态
        recordGUI.recordReadyQueueArea(pcb, ClockInterruptHandlerThread.getCurrentTime());
        OSKernel.readyQueue.add(pcb);
        FileUtils1.logResumeProcess(OSKernel.readyQueue);
    }
}