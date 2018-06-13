package word2VecTool;

import java.nio.charset.Charset;

/**
 * @author liuguotao
 * @date 2018/6/9 000914:35
 * 分词模板的训练
 */
public class Test2 {
    public static void main(String[] args) {
        Word2VecUtils
                .newWord2Vec()//创建模板
                .addAllTextFile("E:\\word2vec", file -> file.getName().endsWith(".txt"))//指定读取的训练文件
                .charset(Charset.forName("UTF-8"))//设置字符编码
                .saveAt("E:\\word2vec\\result", true)//储存模型
                .build();
    }
}
