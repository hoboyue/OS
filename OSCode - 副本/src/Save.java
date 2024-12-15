import java.io.*;
/*
实现GUI中保存按钮功能
 */
public class Save {
    //将三个记录日志区合并在一个txt文件里
    public void saveToFile(String filePath1, String filePath2, String filePath3, String outputDir) {
        File file1 = new File(filePath1);
        File file2 = new File(filePath2);
        File file3 = new File(filePath3);
        String time = String.valueOf(ClockInterruptHandlerThread.getCurrentTime());
        File outputFile = new File(outputDir + "/ProcessResults-" + time + "-DJFK.txt");

        try (BufferedReader reader1 = new BufferedReader(new FileReader(file1));
             BufferedReader reader2 = new BufferedReader(new FileReader(file2));
             BufferedReader reader3 = new BufferedReader(new FileReader(file3));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // 读取第一个文件内容
            String line;
            while ((line = reader1.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            // 读取第二个文件内容
            while ((line = reader2.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            // 读取第三个文件内容
            while ((line = reader3.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            System.out.println("文件合并成功，保存到: " + outputFile.getPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

