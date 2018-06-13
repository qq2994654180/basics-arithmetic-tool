package word2VecTool;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liuguotao
 * @date 2018/6/12 001214:41
 *段落向量模板训练模型
 */
public class test4 {
    private static Logger log = LoggerFactory.getLogger(test4.class);
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[\u4e00-\u9fa50-9a-zA-Z]+");
    private static Charset charset = Charsets.UTF_8;
    private static final ImmutableSet.Builder<File> files = ImmutableSet.builder();
    public static void main(String[] args) throws FileNotFoundException {
        String filePath = "E:\\word2vec\\天龙八部.txt";
        files.add(new File(filePath));
        CharSequence cs = Word2VecUtils.readAllText(files.build(), charset);
        Matcher matcher = SENTENCE_PATTERN.matcher(cs);
        List<String> sentences = new ArrayList<>();
        while (matcher.find()) {
            sentences.add(matcher.group());
        }



        log.info("Load & Vectorize Sentences....");

        SentenceIterator iter = new CollectionSentenceIterator(sentences);

        AbstractCache<VocabWord> cache=new AbstractCache<>();
        TokenizerFactory t=new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        LabelsSource source = new LabelsSource("DOC_");
        ParagraphVectors vectors=new ParagraphVectors.Builder().minWordFrequency(1).iterations(5)
                .epochs(1)
                .layerSize(100)
                .learningRate(0.025)
                .labelsSource(source)
                .windowSize(5)
                .iterate(iter)
                .trainWordVectors(false)
                .vocabCache(cache)
                .tokenizerFactory(t)
                .sampling(0)
                .build();
        vectors.fit();
        /*WordVectorSerializer.writeParagraphVectors(vectors,"E:\\word2vec\\vectors");*/
        INDArray arr1=vectors.inferVector("你的");
        INDArray arr2=vectors.inferVector("你");

        System.out.println("文档1和文档2的相似度是："+ Transforms.cosineSim(arr1, arr2));
    }
}
