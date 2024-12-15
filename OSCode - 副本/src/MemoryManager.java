import java.util.Arrays;
/*
内存类，实现内存的分配，释放，MMU转换
 */

public class MemoryManager {
    private final static int BLOCK_SIZE = 1000; // 每个物理块大小为 1000B
    private final static int TOTAL_BLOCKS = 16; // 总共有 16 个物理块
    private static boolean[] memoryBitmap = new boolean[TOTAL_BLOCKS]; // 位示图，表示物理块是否被占用

    // 初始化位示图
    public MemoryManager() {Arrays.fill(memoryBitmap, false);} // false 表示物理块是空闲的

    // 为进程分配内存（最佳适配方法）
    public static int allocateMemory(PCB pcb) {
        int requiredBlocks = (int) Math.ceil((double) pcb.getMemorySize() / BLOCK_SIZE); // 需要的物理块数，如果不是整块，则向上取整
        int[] allocatedBlocks = new int[requiredBlocks];  // 存储分配的物理块索引
        int allocatedCount = 0;  // 已分配的物理块数量
        int bestFitStartIndex = -1;  // 最佳适配的空闲块起始索引
        int bestFitSize = TOTAL_BLOCKS + 1;  // 最佳适配的空闲块大小（初始为一个不可能的最大值）
        int currentFreeBlockStart = -1;  // 当前空闲块的起始索引
        int currentFreeBlockSize = 0;  // 当前空闲块的大小

        // 遍历位示图，寻找最佳适配的空闲块
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            if (!memoryBitmap[i]) {  // 物理块是空闲的
                if (currentFreeBlockStart == -1) {
                    currentFreeBlockStart = i;  // 标记空闲块的开始
                }
                currentFreeBlockSize++;

                // 如果遍历到最后一个块，判断当前空闲块是否符合最佳适配
                if (i == TOTAL_BLOCKS - 1 && currentFreeBlockSize >= requiredBlocks) {
                    if (currentFreeBlockSize < bestFitSize) {
                        bestFitStartIndex = currentFreeBlockStart;
                        bestFitSize = currentFreeBlockSize;
                    }
                }
            } else {  // 当前块已被占用，检查当前连续的空闲块是否可以作为最佳适配
                if (currentFreeBlockSize >= requiredBlocks) {
                    if (currentFreeBlockSize < bestFitSize) {
                        bestFitStartIndex = currentFreeBlockStart;
                        bestFitSize = currentFreeBlockSize;
                    }
                }
                // 重新记录，重置当前空闲块记录
                currentFreeBlockStart = -1;
                currentFreeBlockSize = 0;
            }
        }

        // 如果没有找到合适的空闲块
        if (bestFitStartIndex == -1) {
//            System.out.println("内存不足，无法为进程 " + pcb.getPid() + " 分配所需的物理块。");
            return -1;
        }

        // 分配内存块
        for (int i = bestFitStartIndex; i < bestFitStartIndex + requiredBlocks; i++) {
            memoryBitmap[i] = true;  // 标记物理块已分配
            allocatedBlocks[allocatedCount++] = i; //记录用了那些块的索引
        }

        pcb.setAllocatedBlocks(allocatedBlocks);  // 将分配的物理块信息保存到 PCB
        pcb.setInTimes(ClockInterruptHandlerThread.getCurrentTime());
//        System.out.println("为进程 " + pcb.getPid() + " 分配了物理块：" + Arrays.toString(allocatedBlocks));
        int baseAddress = bestFitStartIndex * 1000;  // 物理块的起始地址
//        System.out.println(baseAddress);
        int unit = pcb.getMemorySize() / 100; //记录占用了一个物理块内的几个物理块单元
        recordGUI.recordAllocateMemoryArea(bestFitStartIndex,unit); //更新GUI
        return baseAddress;
    }

    // 释放物理块
    public static void freeMemory(PCB pcb) {
        // 更新GUI
        recordGUI.recordFreeMemoryArea(pcb.getBaseAddress() / 1000, pcb.getMemorySize() / 100);
        // 获取进程所分配的物理块
        int[] allocatedBlocks = pcb.getAllocatedBlocks();

        // 检查是否有物理块被分配
        if (allocatedBlocks != null) {
            // 遍历物理块，将它们标记为未使用（false 表示空闲）
            for (int blockIndex : allocatedBlocks) {
                memoryBitmap[blockIndex] = false;  // 释放物理块
//                System.out.println("--------------物理块 " + blockIndex + " 已释放。-----------------------------");
            }
            //更新位示图  0代表没有占据 GUI
            // 清除 PCB 中的物理块分配信息，避免后续再使用这些已释放的块
            pcb.setAllocatedBlocks(null);
            PCB.terminateProcess(pcb);
//            System.out.println("进程 " + pcb.getPid() + " 的所有物理块已释放。");
        } else {
//            System.out.println("进程 " + pcb.getPid() + " 没有分配任何物理块。");
        }
    }

    // MMU地址转换：逻辑地址 -> 物理地址
    public static int logicalToPhysical(PCB pcb) {
        int physicalAddress = pcb.getBaseAddress() + pcb.getUserIndex() * 100;
        return physicalAddress;
    }


}
