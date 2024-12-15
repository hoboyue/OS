import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
根据Lab文件进一步改写过后的文件，用于读取给定的txt文件
 */

// 读取txt文件内的指令
public class InstructionLoader {
    public static List<Instruction> loadInstructions(String filePath) throws IOException {
        List<Instruction> instructions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                //读取第0，1，2类指令
                if (parts.length == 2) {
                    int id = Integer.parseInt(parts[0].trim());
                    int state = Integer.parseInt(parts[1].trim());
                    instructions.add(new Instruction(id, state));
                }
                //读取第3类指令
                else if(parts.length == 3) {
                    int id = Integer.parseInt(parts[0].trim());
                    int state = Integer.parseInt(parts[1].trim());
                    int Mesg_Name = Integer.parseInt(parts[2].trim());
                    instructions.add(new Instruction(id, state, Mesg_Name));
                }
            }
        }
        return instructions;
    }

//读取input4中的内容，每个txt文件的标题
    public static List<Job> loadJobsFromFolder(String folderPath) throws IOException {
        List<Job> jobs = new ArrayList<>();
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("文件夹不存在或不是一个文件夹: " + folderPath);
        }

        // 遍历文件夹中的每个 .txt 文件
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                // 获取文件名并解析作业信息
                String fileName = file.getName().replace(".txt", "");
                String[] jobInfo = fileName.split("-");

                if (jobInfo.length == 3) {
                    // 从文件名提取 jobId, inTime, instructionCount
                    int jobId = Integer.parseInt(jobInfo[0].trim());
                    int inTime = Integer.parseInt(jobInfo[1].trim());

                    // 加载该文件中的所有指令
                    List<Instruction> instructions = InstructionLoader.loadInstructions(file.getPath());

                    //用于消息通信线程
                    for (Instruction instruction : instructions) {
                        instruction.setSendId(jobId);
                    }
                    int instructionCount = instructions.size();

                    // 创建新的 Job 并添加到 jobRequests 列表中
                    Job job = new Job(jobId, inTime, instructionCount);
                    job.setInstructions(instructions);
                    jobs.add(job);
                }
            }
        }
        return jobs;
    }
}
