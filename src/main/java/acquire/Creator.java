package acquire;

import association.datamining_apriori.AprioriTool;

import java.io.*;
import java.util.List;

/**
 * @author liuguotao
 * @date 2018/6/5 00058:50
 */
public class Creator{
    public static void main(String[] args) {
       Modeling  creator = null;
        String ccc = creatorFile(creator, "ccc");
        AprioriTool tool = new AprioriTool(ccc, 2);//输入数据，并输入支持度
        tool.printAttachRule(0.7);
        System.out.println(ccc);
    }
    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:42
        * @param [modeling, fileName]
        * @return java.lang.String
        * 对外开方接口，用于算法学习模型生成（不是必须的，自己存数据库也可以，不过学习模型经常变动，又不是重要数据，而且数据读取频率较高，存数据库会加大数据库负担）
    　*/
    public static String creatorFile(Modeling modeling,String fileName){
        List<List<String>> creator = modeling.Creator();
        String path = create(fileName);//执行文件创建
        File file = new File(path);
        for (List<String> list : creator) {
            String data = fatList(list);
            bufferedWriterFile(file,data);//执行写入操作
        }
        return path;
    }
    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:42
        * @param [fileName]
        * @return java.lang.String
        * 创建文件
    　*/
    private static String create(String fileName){
        File file = new  File("data");
        try {
            file.mkdirs();
            File file1 = new  File("data",fileName+".txt");
            if(file.exists()){
                file1.delete();
            }
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath()+"\\"+fileName+".txt";
    }
    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:43
        * @param [list]
        * @return java.lang.String
        * 格式化数据
    　*/
    private static String fatList(List<String> list){
        String result = null;
        for (String s : list) {
           if(result==null){
               result=s;
           }else{
               result+=" "+s;
           }
        }
        return result;
    }
    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:43
        * @param [fout, data]
        * @return void
        * 写入
    　*/
     static void bufferedWriterFile(File fout,String data){
        FileOutputStream fos;
        BufferedWriter bw;
        try {
            fos = new FileOutputStream(fout,true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
                bw.write(data);
                bw.newLine();
                bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
