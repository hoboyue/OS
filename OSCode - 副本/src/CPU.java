import java.util.HashMap;
import java.util.Map;

public class CPU {
    private int pc; // 程序计数器
    private int ir; // 指令寄存器
    private int psw; // 程序状态字
    private static Map<String, Integer> registerBackup; // 寄存器备份，用于进程切换时保存寄存器状态

    // 私有构造函数，防止外部实例化
    CPU() {
        this.pc = 0;
        this.ir = 0;
        this.psw = 0;
        this.registerBackup = new HashMap<>();
    }

    //运行当前的进程
    public void runProcess(PCB pcb){
        pcb.setPC(pcb.getPC() + 1);  // 每执行一条指令后，程序计数器 +1
        pcb.setIR(pcb.getir() + 1);  // 每执行一条指令后，指令寄存器 +1
        // System.out.println("进程 " + pcb.getPid() + " 正在执行指令，PC 更新为: " + pcb.getPC());
    }

    //CPU现场保护
    public void CPU_PRO(PCB pcb){
        //挨个保存进去，用pcb的id作为键（唯一性）放进
        String pidKey = String.valueOf(pcb.getPid());
        registerBackup.put(pidKey + "pc",this.pc);
        registerBackup.put(pidKey + "ir", this.ir);
        registerBackup.put(pidKey + "psw",this.psw);
//        System.out.println("CPU状态已经保存完毕");
    }

    //CPU现场恢复
    public void CPU_REC(PCB pcb){
        // 恢复进程的寄存器状态
        String pidKey = String.valueOf(pcb.getPid());
        if (registerBackup.containsKey(pidKey + "pc")) {
            this.pc = registerBackup.get(pidKey + "pc");
            this.ir = registerBackup.get(pidKey + "ir");
            this.psw = registerBackup.get(pidKey + "psw");
 //           System.out.println("CPU状态已经恢复完毕");
        }
    }
}
