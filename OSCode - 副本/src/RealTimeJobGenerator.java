import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
/*
实时按钮的功能，随机生成20条指令
 */
public class RealTimeJobGenerator {

    // 生成一个包含20条指令的实时作业
    public static List<Instruction> generateRealTimeJob() {
        List<Instruction> instructions = new ArrayList<>();
        List<Integer> instructionTypes = new ArrayList<>(); // 用于保存指令类型
        Random random = new Random();

        // 添加12条计算指令 (state = 0)
        for (int i = 0; i < 12; i++) {
            instructionTypes.add(0);  // 将计算指令类型保存到列表中
        }

        // 添加5条I/O指令 (state = 1 或 state = 2, 随机分配)
        for (int i = 0; i < 5; i++) {
            int state = random.nextBoolean() ? 1 : 2; // 随机生成1或2表示I/O类型
            instructionTypes.add(state);
        }

        // 添加3条消息指令 (state = 3)
        for (int i = 0; i < 3; i++) {
            instructionTypes.add(3);  // 将消息指令类型保存到列表中
        }

        // 打乱类型列表，保持每种类型数量不变，但顺序随机
        Collections.shuffle(instructionTypes);

        // 生成编号为1到20的指令
        for (int i = 1; i <= 20; i++) {
            int state = instructionTypes.get(i - 1);  // 按顺序分配打乱后的指令类型

            if (state == 3) {  // 如果是消息指令，需要生成 Mesg_Name
                int randomIndex = random.nextInt(OSKernel.coutJobs);
                int Mesg_Name = OSKernel.pcbTable.get(randomIndex).getPid();  // 随机获取一个进程ID作为 Mesg_Name
                instructions.add(new Instruction(i, state, Mesg_Name));
            } else {
                instructions.add(new Instruction(i, state));  // 非消息指令不需要 Mesg_Name
            }
        }
        return instructions;  // 返回按顺序编号但类型随机的指令列表
    }
}
