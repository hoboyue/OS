import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
/*
消息缓冲区类，包括缓冲区写，缓冲区读，以及释放内存块
 */
public class MessageBuffer {
    private static final int BUFFER_SIZE = 10; // 缓冲区大小
    private static final int BUFFER_UNIT_SIZE = 200; // 每个单元大小200B
    public static final Semaphore empty = new Semaphore(BUFFER_SIZE); // 空缓冲区单元信号量
    public static final Semaphore full = new Semaphore(0); // 满缓冲区单元信号量
    public static final Semaphore mutex = new Semaphore(1); // 互斥信号量

    private byte[] messageBuffer = new byte[BUFFER_SIZE * BUFFER_UNIT_SIZE]; // 一维缓冲区数组
    private int[] bufferStatus = new int[BUFFER_SIZE]; // 0表示空闲，1表示占用

    public static void isBufferEmpty(){
        if(empty.availablePermits() == BUFFER_SIZE){
            FileUtils3.bufferFree();
        }
    }

    public int writeMessage(byte[] messageData, int startTime, Instruction instruction) throws InterruptedException {
        empty.acquire(); // 等待空缓冲区单元
        FileUtils3.recordEmptyP();

        // 获取同步锁
        SyncManager.lock.lock();
        try {
            // 模拟等待1秒，仿真访问耗时
            if (!SyncManager.rdBufCondition.await(ClockInterruptHandlerThread.MILLISECONDS, TimeUnit.MILLISECONDS)) {
                //为了使时钟显示正常，不会出现停顿
                SyncManager.clkCondition.signal();
            }
        } finally {
            //为了使时钟显示正常，不会出现停顿
            SyncManager.clkCondition.signal();
            SyncManager.lock.unlock();
        }

        try{
            mutex.acquire(); // 进入互斥区
            FileUtils3.recordMutexP();
            try {
                int bufferIndex = -1;

                // 找到空闲的缓冲区单元
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    if (bufferStatus[i] == 0) {
                        bufferStatus[i] = 1; // 标记为占用
                        bufferIndex = i;
                        recordGUI.recordAllocateBufferArea(bufferIndex);

                        // 拷贝数据到缓冲区
                        System.arraycopy(messageData, 0, messageBuffer, i * BUFFER_UNIT_SIZE, BUFFER_UNIT_SIZE);
                        FileUtils3.copyInBuffer(instruction);
                        break;
                    }
                }
                return bufferIndex;
            } finally {
                mutex.release(); // 释放互斥锁
                FileUtils3.recordMutexV();
            }
        } finally {
            full.release(); // 增加满缓冲区单元信号量
            FileUtils3.recordFullV();
        }
    }

    // 读取消息数据
    public byte[] readMessage(int bufferIndex, Instruction instruction) throws InterruptedException {
        full.acquire(); // 等待满缓冲区单元
        FileUtils3.recordFullP();

        // 获取同步锁
        SyncManager.lock.lock();
        try {
            // 模拟等待1秒，仿真访问耗时
            if (!SyncManager.rdBufCondition.await(ClockInterruptHandlerThread.MILLISECONDS, TimeUnit.MILLISECONDS)) {
                //为了使时钟显示正常，不会出现停顿
                SyncManager.clkCondition.signal();
            }
        } finally {
            //为了使时钟显示正常，不会出现停顿
            SyncManager.clkCondition.signal();
            SyncManager.lock.unlock(); // 确保锁在等待后被释放
        }

        try {
            mutex.acquire(); // 进入互斥区
            FileUtils3.recordMutexP();
            try {
                byte[] messageData = new byte[BUFFER_UNIT_SIZE];
                if (bufferStatus[bufferIndex] == 1) { // 检查该单元是否占用
                    System.arraycopy(messageBuffer, bufferIndex * BUFFER_UNIT_SIZE, messageData, 0, BUFFER_UNIT_SIZE); // 从缓冲区读取数据
                    bufferStatus[bufferIndex] = 0; // 标记为空闲
                    recordGUI.recordFreeBufferArea(bufferIndex);
                    FileUtils3.copyOutBuffer(instruction);
                }
                return messageData;
            } finally {
                mutex.release(); // 释放互斥锁
                FileUtils3.recordMutexV();
            }
        } finally {
            empty.release(); // 增加空缓冲区单元信号量
            FileUtils3.recordEmptyV();
        }
    }

    // 释放指定缓冲区单元
    public void releaseBuffer(int bufferIndex) throws InterruptedException {
        if (bufferIndex < 0 || bufferIndex >= BUFFER_SIZE) {
//            System.out.println("无效的缓冲区索引: " + bufferIndex);
            return;
        }
        mutex.acquire(); // 进入互斥区
        FileUtils3.recordMutexP();
        try {
            if (bufferStatus[bufferIndex] == 1) { // 检查该单元是否占用
                bufferStatus[bufferIndex] = 0; // 标记为空闲
                // 清空该缓冲区单元的数据
                for (int i = bufferIndex * BUFFER_UNIT_SIZE; i < (bufferIndex + 1) * BUFFER_UNIT_SIZE; i++) {
                    messageBuffer[i] = 0;
                }
//                System.out.println("缓冲区单元 " + bufferIndex + " 已释放");
            } else {
//                System.out.println("缓冲区单元 " + bufferIndex + " 已空闲，无需释放");
            }
        } finally {
            mutex.release(); // 释放互斥锁
            FileUtils3.recordMutexV();
            empty.release(); // 增加空缓冲区单元信号量
            FileUtils3.recordEmptyV();
        }
    }
}
