package dl4j.test.regression;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author liuguotao
 * @date 2018/6/6 000616:29
 * 使用DL4J可以很方便的完成一个线性回归学习算法
 */
public class OneRegression {
    //随机数种子，用于结果复现
    private static final int seed = 12345;
    //对于每个miniBatch的迭代次数
    private static final int iterations = 10;
    //epoch数量(全部数据的训练次数)
    private static final int nEpochs = 50;
    //一共生成多少样本点
    private static final int nSamples = 1000;
    //批处理大小
    private static final int batchSize = 100;
    //网络模型学习率
    private static final double learningRate = 0.01;//严重影响到梯度下降的有效性
    //随机数据生成的范围
    private static int MIN_RANGE = 0;
    private static int MAX_RANGE = 3;

    private static final Random rng = new Random(seed);

    public static void main(String[] args) {
        //Create the network
        int numInput = 1;
        int numOutputs = 1;

        /**
         * 神经网络的配置
         我们需要配置神经网络的超参数->为什么叫超参数呢？
         超参数 -> 用于辅助模型学习参数的参数 -> hyper-parameter -> 超参数
         学习的参数是什么？ y = ax + b
         其中的x,y是已知的，这是用于神经网络的训练样本。
         参数 a,b是未知的，所以a,b是我们神经网络需要学习的参数
         同时我们需要配置超参数来辅助神经网络学习到a 和 b这两个参数
         */
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                /**
                 * 随机种子 -> 随机数生成通常需要一个起点，我们所生成的随机数都是伪随机数。
                 * 因为神经网络训练时，模型的初试权重和偏置是随机生成的
                 * 我们需要随机数种子保证每次初始化的权重大体一致
                 * 这样可以保证模型结果的可复现性
                 * 只有模型结果可复现->进行神经网络的调参->我们的调参对于模型效果提升是有意义的
                 */
                .seed(seed)
                /**
                 *算法优化
                 */
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)//随机梯度下降法
                /**
                 * 对神经网络的权重进行随机初始化
                 * 随机的权重要比全0的权重对神经网络训练更有意义
                 */
                .weightInit(WeightInit.XAVIER)
                /**
                 * 学习速率根据数据多少，如果值越大速度越慢越精准
                 */
                .updater(new Sgd(learningRate))
                .list()
                .layer(0, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)//训练开始
                        /**
                         * y = x
                         * None
                         */
                        .activation(Activation.IDENTITY)//激活层
                        /**
                         * 输入层个数
                         */
                        .nIn(numInput)
                        /**
                         * 输出层个数
                         */
                        .nOut(numOutputs).build())
                /**
                 * 预训练
                 */
                .pretrain(false)
                /**
                 * 反向传播
                 */
                .backprop(true).build();
        /**以上对设置了神经网络*/
        // 使用 MultiLayerNetwork 对我们的 conf 进行一个包装
        // 对神经网络进行了构建
        MultiLayerNetwork net = new MultiLayerNetwork(conf);

        // 必须调用 init() 方法
        // 是对于模型参数的初始化使用Workspace Modes启动多层网络
        net.init();

        System.out.println(net.summary());//输出网络信息

        /**
         * 有监听器，用于监听我们神经网络训练时候的状态
         * 主要是用于监听我们神经网络训练时候的损失函数的得分
         * 目前参数为1，则说明网络每训练一次，就会打印一次损失函数的得分
         */
//        net.setListeners(new ScoreIterationListener(1));

        DataSetIterator iterator = getTrainingData(batchSize, rng);

        // 测试两个数字，判断
        final INDArray input = Nd4j.create(new double[] { 10, 100 }, new int[] { 2, 1 });
        INDArray out = net.output(input, false);
        System.out.println(out);
        //初始化用户界面后端
        UIServer uiServer = UIServer.getInstance();

        //设置网络信息（随时间变化的梯度、分值等）的存储位置。这里将其存储于内存。
        StatsStorage statsStorage = new InMemoryStatsStorage();         //或者： new FileStatsStorage(File)，用于后续的保存和载入

        //将StatsStorage实例连接至用户界面，让StatsStorage的内容能够被可视化
        uiServer.attach(statsStorage);

        //然后添加StatsListener来在网络定型时收集这些信息
        net.setListeners(new StatsListener(statsStorage));

        // 训练整个数据集nEpochs次
        for( int i=0; i<nEpochs; i++ ){
            iterator.reset();//重置
            /**
             * 用于训练模型
             */
            net.fit(iterator);
            //选择神经模型//装配样本参数
            Map<String, INDArray> params = net.paramTable();//获取参数表
            params.forEach((key, value) -> System.out.println("key:" + key +", value = " + value));//输出参数表梯度w b的值

        }

    }

    private static DataSetIterator getTrainingData(int batchSize, Random rand) {
        /**
         * 如何构造我们的训练数据
         * 现有的模型主要是有监督学习
         * 我们的训练集必须有  特征+标签
         * 特征-> x
         * 标签->y
         */
        double [] output = new double[nSamples];
        double [] input = new double[nSamples];
        //随机生成0到3之间的x
        //并且构造 y = 0.5x + 0.1
        //a -> 0.5  b ->0.1
        for (int i= 0; i< nSamples; i++) {//随机1000个样本
            input[i] = MIN_RANGE + (MAX_RANGE - MIN_RANGE) * rand.nextDouble();

            output[i] = 0.5 * input[i] + 0.1;
        }

        /**
         * 我们nSamples条数据
         * 每条数据只有1个x
         */
        INDArray inputNDArray = Nd4j.create(input, new int[]{nSamples,1});//把1000个样本数据放入1000行1列的二维数组

        INDArray outPut = Nd4j.create(output, new int[]{nSamples, 1});

        /**
         * 构造喂给神经网络的数据集
         * DataSet是将  特征+标签  包装成为一个类
         *
         */
        DataSet dataSet = new DataSet(inputNDArray, outPut);
        List<DataSet> listDs = dataSet.asList();

        return new ListDataSetIterator(listDs,batchSize);//设置批处理大小和训练的样本
    }
}
