import java.util.HashMap;
import java.util.Map;
/*
指令类，包含指令的各种属性，在基础文件上新增了部分属性
 */
public class Instruction {
    private int id; //指令编号
    private int state; //指令类型
    private int InRunTimes; //执行时间
    private int blockFlag; //阻塞标志，通过此标志将IO进程或消息通信线程在下一次execute时进入CPU执行
    private int Mesg_Name; // 消息接收的进程id
    private int sendId; //指令所在作业的ID
    private int bufferId; //给消息通信指令的消息所分配的缓冲区编号
    private int startTime; //指令进入CPU的时间，与系统时间比较，用于判断是否满足指令的运行时间

    //计算指令，IO指令构造函数
    public Instruction(int id, int state) {
        this.id = id;
        this.state = state;
        this.startTime = 0;

        switch (state) {
            case 0:
                InRunTimes = 1; // 用户态计算操作指令
                this.blockFlag = 0;
                break;
            case 1:
                InRunTimes = 0; //键盘输入变量指令，不在CPU内执行，所以运行时间为0s
                this.blockFlag = 1;
                break;
            case 2:
                InRunTimes = 0; // 屏幕显示输出指令，不在CPU内执行，所以运行时间为0s
                this.blockFlag = 2;
                break;
        }
    }

    //消息通信指令构造函数
    public Instruction(int id, int state, int Mesg_Name) {
        this.id = id;
        this.state = state;
        this.Mesg_Name = Mesg_Name;
        this.InRunTimes = 0;
        this.blockFlag = 3;
    }

    //一系列set和get函数
    public int getStartTime(){
        return startTime;
    }
    public void setStartTime(int startTime){
        this.startTime = startTime;
    }

    public int getInstruction_ID() {
        return id;
    }

    public int getInstruction_State() {
        return this.state;
    }
    public void setInsturction_State(int state){
        this.state = state;
    }

    public void setBlockFlag(int flag){
        this.blockFlag = flag;
    }
    public int getBlockFlag() {
        return this.blockFlag;
    }

    public void setMesg_Name(int id){
        this.Mesg_Name = id;
    }
    public int getMesg_Name() {
        return this.Mesg_Name;
    }

    public void setSendId(int id){
        this.sendId = id;
    }
    public int getSendId() {
        return this.sendId;
    }

    public void setBufferId(int id){
        this.bufferId = id;
    }
    public int getBufferId() {
        return this.bufferId;
    }

    public int getInRunTimes() {
        return InRunTimes;
    }

    //0号指令占100B， 1，2号指令不占内存 ，3号内存200B
    public int getDataSize(){
        if(state == 0)
            return 100;
        else if (state == 3)
            return 200;
        else return 0;
    }
}
