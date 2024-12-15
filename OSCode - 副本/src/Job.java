import java.util.ArrayList;
import java.util.List;
/*
作业类，在变成PCB进程之前类
 */
public class Job {
    private int jobId; //作业的ID
    private int inTime; //作业的达到时间
    private int instructionCount; //作业包含的指令条数
    private int processTime = 0; //作业进入后备队列的时间
    public List<Instruction> instructions = new ArrayList<>(); //作业包含的指令列表

    public Job(int jobId, int inTime, int instructionCount) {
        this.jobId = jobId;
        this.inTime = inTime;
        this.instructionCount = instructionCount;
    }

    //一系列set和get函数
    public int getJobsID() {
        return this.jobId;
    }

    public int getInTimes() {
        return this.inTime;
    }

    public void setProcessTime(int processTime){
        this.processTime = processTime;
    }

    public int getProcessTime(){
        return this.processTime;
    }

    public int getInstrucNum() {
        return this.instructionCount;
    }
    public void setInstrucNum(){
        this.instructionCount = this.instructions.size();
    }

    public List<Instruction> getInstructions(){
        return this.instructions;
    }
    public void setInstructions(List<Instruction> instructions){this.instructions = instructions;}

}