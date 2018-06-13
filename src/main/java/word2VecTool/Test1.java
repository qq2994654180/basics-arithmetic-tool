package word2VecTool;
import org.deeplearning4j.models.word2vec.Word2Vec;
import java.io.FileNotFoundException;

/**
 * @author liuguotao
 * @date 2018/6/9 000914:31
 *分词模板的使用
 */
public class Test1 {
    public static void main(String[] args) throws FileNotFoundException {
    /***/
        Word2Vec word2Vec = Word2VecUtils
                .restore("E:\\word2vec\\result");
        System.out.println(word2Vec.wordsNearest("谁", 100));

        /***/

    }
}
