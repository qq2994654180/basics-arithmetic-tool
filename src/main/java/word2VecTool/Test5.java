package word2VecTool;

import com.google.common.base.Strings;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import javax.sound.midi.SoundbankResource;
import java.io.IOException;

/**
 * @author liuguotao
 * @date 2018/6/12 001214:50
 * 段落向量模板训练模型使用
 */
public class Test5 {
    public static void main(String[] args) {
        try {
            //Preconditions.checkArgument(!Strings.isNullOrEmpty("E:\\word2vec\\vectors"),"illegal path");
            TokenizerFactory t = new DefaultTokenizerFactory();
            t.setTokenPreProcessor(new CommonPreprocessor());


                    ParagraphVectors paragraphVectors = WordVectorSerializer.readParagraphVectors("E:\\word2vec\\vectors");
            // we load externally originated model
            paragraphVectors.setTokenizerFactory(t);
            paragraphVectors.getConfiguration().setIterations(1);
                    INDArray aaa = paragraphVectors.inferVector("你");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
