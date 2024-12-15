import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
/*
消息通信线程
 */
public class MessageThread extends Thread {
    static private LinkedList<Instruction> messageList = new LinkedList<>(); // 消息列表
    // 每次receive之后删除的进程信息会存储在receiveMessageList，为了方便之后每次查询消息队列是否还有指定进程
    //防止因为时间间隔不均导致的跳过某些进程的查询
    static private LinkedList<Instruction> receiveMessageList = new LinkedList<>();
    static private MessageBuffer messageBuffer;

    public MessageThread(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    public void run() {
        while (true) {
            SyncManager.lock.lock();
            try {
//                System.out.println("--------等待消息线程启动---------");
                SyncManager.msgCondition.await();
//                System.out.println("--------消息线程启动成功---------");

                // 遍历 messageList，检查接收方是否还存在,防止进程接收方早终止而带来该进程一直堵塞在队列3中
                Iterator<Instruction> iterator = messageList.iterator();
                while (iterator.hasNext()) {
                    Instruction message = iterator.next();
                    int receiver = message.getMesg_Name();

                    // 检查接收方是否存在
                    PCB receiverPcb = null;
                    for (PCB pcb : OSKernel.pcbTable) {
                        if (pcb.getPid() == receiver) {
                            receiverPcb = pcb;
                            break;
                        }
                    }

                    if (receiverPcb == null) {
                        // 接收方不存在，删除消息并释放缓冲区
//                        System.out.println("接收方 " + receiver + " 不存在，删除消息并释放缓冲区");
                        messageBuffer.releaseBuffer(message.getBufferId()); // 释放消息占用的缓冲区
                        iterator.remove(); // 从消息队列中删除消息
                        recordGUI.recordMessageFailArea(message);
                    }
                }

                // 每秒调用 Receive 函数，处理消息
                receive();

                Instruction instruc;
                if (!receiveMessageList.isEmpty()) {
                    instruc = receiveMessageList.getFirst();
//                    System.out.println("receive函数返回的指令的进程pid是" + instruc.getSendId());
                } else {
//                    System.out.println("receiveMessageList 为空，没有可处理的指令。");
                    continue;
                }

                if (ClockInterruptHandlerThread.getCurrentTime() % 2 == 0) { // 每2秒查询一次消息队列
//                    System.out.println("---------开始进入查询消息队列-------");
                    if (!messageList.contains(instruc)) {
//                        System.out.println("消息队列中已经没有" + instruc.getSendId() + ",可以将其进程唤醒");
                        // 找到发送进程，将其唤醒
                        PCB senderPcb = null;
                        for (PCB pcb : OSKernel.pcbTable) {
                            if (pcb.getPid() == instruc.getSendId()) {
                                senderPcb = pcb;
                                break;
                            }
                        }

                        if (senderPcb != null) {
                            receiveMessageList.remove(instruc);
                            PCB.resumeProcess(senderPcb);
//                            System.out.println(senderPcb.getPid() + "已经成功被唤醒");
                            senderPcb.setResumeTime(ClockInterruptHandlerThread.getCurrentTime());
                            FileUtils2.logSaveBlocks3(senderPcb);

                            recordGUI.recordMessageSucessArea(instruc);
//                          System.out.println("-----------------进程" + instruc.getSendId() +  "已经被记录了---------------");
                        } else {
//                            System.out.println("未找到 PID 为 " + instruc.getSendId() + " 的 PCB");
                        }
                    } else {
                        //                       System.out.println("进程仍在消息队列中，不能唤醒");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                // 唤醒时钟中断处理线程上的等待线程
                SyncManager.clkCondition.signalAll();
                SyncManager.lock.unlock();
            }
        }
    }

    // 返回发送线程的进程id，方便唤醒
    private synchronized void receive() {
        Instruction targetMessage = null;
        if (!messageList.isEmpty()) {
            targetMessage = messageList.removeFirst();//查找链表中的第一个，并将其删除
            receiveMessageList.add(targetMessage);
//           System.out.println(targetMessage.getSendId() + "已经从messageList获取");

            if (targetMessage != null) {
                int receiver = targetMessage.getMesg_Name();
                // 查找接收进程
                PCB receiverPcb = null;
                for (PCB pcb : OSKernel.pcbTable) {
                    if (pcb.getPid() == receiver) {
                        receiverPcb = pcb;
                        break;
                    }
                }

                if (receiverPcb == null) {
//                    System.out.println("--------------由receive函数得到的通信失败------------");
//                   System.out.println("接收消息的进程 " + receiver + " 不存在，接收终止。");
                    recordGUI.recordMessageFailArea(targetMessage);
                    return; // 找不到接收进程则退出当前 receive 调用
                }

                // 读取消息并传递给接收进程
                try {
                    byte[] messageData = messageBuffer.readMessage(targetMessage.getBufferId(), targetMessage); // 读取消息
                    receiverPcb.setMessageRecv(messageData); // 将消息数据传递到接收进程的用户区
//                   System.out.println("消息已接收: 来自进程ID " + targetMessage.getSendId());
                    PCB senderPcb = null;
                    for (PCB pcb : OSKernel.pcbTable) {
                        if (pcb.getPid() == targetMessage.getSendId()) {
                            senderPcb = pcb;
                            break;
                        }
                    }
                    //传送成功之后唤醒阻塞在阻塞队列3的进程
                    OSKernel.Block3Queue.remove(senderPcb);
                } catch (InterruptedException e) {
//                   System.err.println("读取消息时发生中断: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    public synchronized static int Send(int receiver, int senderPid, int instrucId) {
        byte[] messageData = new byte[200]; // 创建200字节数组
        Random random = new Random();
        // 填充数组中的每个字节为随机值
        random.nextBytes(messageData);

        PCB senderPcb = null;
        for (PCB pcb : OSKernel.pcbTable) {
            if (pcb.getPid() == senderPid) {
                senderPcb = pcb;
//                System.out.println("--------找到pcb表中的对应发送位------");
                break;
            }
        }

        if (senderPcb == null) {
//            System.out.println("未找到 PID 为 " + senderPid + " 的 PCB，检查 pcbTable 是否初始化正确。");
            return -1;
        }
        //在这里初始化PCB用户发送区的内容
        senderPcb.setMessageSend(messageData);

        // 检查接收进程是否存在
        PCB receiverPcb = null;
        for (PCB pcb : OSKernel.pcbTable) {
            if (pcb.getPid() == receiver) {
                receiverPcb = pcb;
                break;
            }
        }

        Instruction message = new Instruction(instrucId, 3);
        message.setMesg_Name(receiver); // 设置接收进程的名称或 ID
        message.setSendId(senderPcb.getPid());

        if (receiverPcb == null) { // 如果未找到接收进程
//            System.out.println("接收消息的进程 " + receiver + " 不存在，消息发送终止。");
//            System.out.println("--------------由send函数得到的通信失败------------");
            recordGUI.recordMessageFailArea(message);
            return -1;
        } else {
            try {
                int bufferId = messageBuffer.writeMessage(messageData, ClockInterruptHandlerThread.getCurrentTime(), message); // 写入缓冲区
                message.setBufferId(bufferId);
                messageList.add(message); // 添加消息到列表
//                System.out.println("messageList已经成功添加" + message.getSendId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}

