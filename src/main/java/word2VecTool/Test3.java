package word2VecTool;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author liuguotao
 * @date 2018/6/11 001111:02
 * 做局部分词性标注
 */
public class Test3 {
    public static void test() {
        //只关注这些词性的词
        Set<String> expectedNature = new HashSet<String>() {{
            add("n");add("v");add("vd");add("vn");add("vf");
            add("vx");add("vi");add("vl");add("vg");
            add("nt");add("nz");add("nw");add("nl");
            add("ng");add("userDefine");add("wh");
        }};
        String str = "小王是谁？" ;
        Result result = ToAnalysis.parse(str); //分词结果的一个封装，主要是一个List<Term>的terms
        System.out.println(result.getTerms());

        List<Term> terms = result.getTerms(); //拿到terms
        System.out.println(terms.size());

        for(int i=0; i<terms.size(); i++) {
            String word = terms.get(i).getName(); //拿到词
            String natureStr = terms.get(i).getNatureStr(); //拿到词性
            System.out.println(word + ":" + natureStr);
            if(expectedNature.contains(natureStr)) {
                System.out.println(word + ":" + natureStr);
            }
        }
        Collection<Keyword> keywords = keywords(str);
        System.out.println(keywords);
    }
    public static Collection<Keyword> keywords(String conent){
        KeyWordComputer kwc = new KeyWordComputer(5);

        Collection<Keyword> result = kwc.computeArticleTfidf(conent);
        return  result;
    }

    public static void main(String[] args) {
       test();
    }
}
