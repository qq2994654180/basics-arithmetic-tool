package dl4j.lesson3;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import java.io.File;
import java.util.List;
import java.util.Map;

/*/**
　  * @author liuguotao
    * @date 2018/6/7 0007 16:40
　*/
public class MLPClassifierLinear {


    public static void main(String[] args) throws Exception {
        int seed = 123;
        double learningRate = 0.01;//学习率
        int batchSize = 50;//批处理大小
        int nEpochs = 30;//训练次数

        int numInputs = 2;
        int numOutputs = 2;
        int numHiddenNodes = 20;

        final String filenameTrain  = new ClassPathResource("/classification/linear_data_train.csv").getFile().getPath();//训练集数据
        final String filenameTest  = new ClassPathResource("/classification/linear_data_eval.csv").getFile().getPath();//测试数据
        RecordReader rr = new CSVRecordReader();//用于加载训练集，也可从数据库获取
        rr.initialize(new FileSplit(new File(filenameTrain)));
        //记录读取器数据集迭代器
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,2);//训练集迭代器

        RecordReader rrTest = new CSVRecordReader();//用于加载测试集，也可从数据库获取
        rrTest.initialize(new FileSplit(new File(filenameTest)));
        //记录读取器数据集迭代器
        DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize,0,2);//测试集迭代器

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()//构建网络的参数
                .seed(seed)//随机种子
                .updater(new Nesterovs(learningRate, 0.9))//学习率，冲率设置
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)//输入层数量和输出层数量
                        .weightInit(WeightInit.XAVIER)//权重类型
                        .activation(Activation.RELU)//激活
                        .build())//设置第一层神经网络
                .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX).weightInit(WeightInit.XAVIER)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //每10个参数更新打印分数

        for ( int n = 0; n < nEpochs; n++) {
            model.fit( trainIter );//训练次数
        }
        //训练完成
        System.out.println("Evaluate model....");
        Evaluation eval = new Evaluation(numOutputs);
        while(testIter.hasNext()){
            DataSet t = testIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            INDArray predicted = model.output(features,false);

            eval.eval(lables, predicted);

        }

        //训练后的参数输出
        System.out.println(eval.stats());


        //------------------------------------------------------------------------------------
        //Training is complete. Code that follows is for plotting the data & predictions only

        //分坐标
        double xMin = 0;
        double xMax = 1.0;
        double yMin = -0.2;
        double yMax = 0.8;

        //培训完成。下面的代码只用于绘制数据和预测
        int nPointsPerAxis = 100;
        double[][] evalPoints = new double[nPointsPerAxis*nPointsPerAxis][2];
        int count = 0;
        for( int i=0; i<nPointsPerAxis; i++ ){
            for( int j=0; j<nPointsPerAxis; j++ ){
                double x = i * (xMax-xMin)/(nPointsPerAxis-1) + xMin;
                double y = j * (yMax-yMin)/(nPointsPerAxis-1) + yMin;

                evalPoints[count][0] = x;
                evalPoints[count][1] = y;

                count++;
            }
        }

        INDArray allXYPoints = Nd4j.create(evalPoints);
        INDArray predictionsAtXYPoints = model.output(allXYPoints);

        //将所有的训练数据放在一个数组中，并绘制:
        rr.initialize(new FileSplit(new ClassPathResource("/classification/linear_data_train.csv").getFile()));
        rr.reset();
        int nTrainPoints = 1000;
        trainIter = new RecordReaderDataSetIterator(rr,nTrainPoints,0,2);
        DataSet ds = trainIter.next();
        PlotUtil.plotTrainingData(ds.getFeatures(), ds.getLabels(), allXYPoints, predictionsAtXYPoints, nPointsPerAxis);


        //获取测试数据，通过网络运行测试数据来生成预测，并绘制这些预测:
        rrTest.initialize(new FileSplit(new ClassPathResource("/classification/linear_data_eval.csv").getFile()));
        rrTest.reset();
        int nTestPoints = 500;
        testIter = new RecordReaderDataSetIterator(rrTest,nTestPoints,0,2);
        ds = testIter.next();
        INDArray testPredicted = model.output(ds.getFeatures());
        //******************************************************

       /* System.out.println(indArray);*/
        model.rnnClearPreviousState();
        List<INDArray> indArrays = model.feedForward();
        DataBuffer data = indArrays.get(0).data();
        DataBuffer data1 = indArrays.get(2).data();

        long length = data.length();
        long length1 = data1.length();
        System.out.println(length);
        System.out.println(length1);
        for (INDArray indArray : indArrays) {
            System.out.println(indArray.toString());
        }
        //***********************************************************
        PlotUtil.plotTestData(ds.getFeatures(), ds.getLabels(), testPredicted, allXYPoints, predictionsAtXYPoints, nPointsPerAxis);

        System.out.println("****************Example finished********************");
    }
}
