package dl4j.test.twoarr;

import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * @author liuguotao
 * @date 2018/6/6 000616:00
 * 二维阵列的setget
 */
public class Nd4jTwoArrGetSet {
    public static void main(String[] args) {
        INDArray nd = Nd4j.create(new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, new int[]{2, 6});
        System.out.println("打印原有数组");
        System.out.println(nd);

        /*
            获取指定索引的值
         */
        System.out.println("获取数组下标为0, 3的值");
        double value = nd.getDouble(0, 3);
        System.out.println(value);

        /*
            修改指定索引的值
         */
        System.out.println("修改数组下标为0, 3的值");
        //scalar 标量
        nd.putScalar(0, 3, 100);

        System.out.println(nd);
        /*
            使用索引迭代器遍历ndarray，使用c order
         */
        System.out.println("使用索引迭代器遍历ndarray");
        NdIndexIterator iter = new NdIndexIterator(2, 6);
        while (iter.hasNext()) {
            int[] nextIndex = iter.next();
            double nextVal = nd.getDouble(nextIndex);

            System.out.println(nextVal);
        }
          /*
            获取一行
         */
        System.out.println("获取数组中的一行");
        INDArray singleRow = nd.getRow(0);
        System.out.println(singleRow);

        /*
            获取多行
         */
        System.out.println("获取数组中的多行");
        INDArray multiRows = nd.getRows(0, 1);
        System.out.println(multiRows);

        /*
            替换其中的一行
         */
        System.out.println("替换原有数组中的一行");
        INDArray replaceRow = Nd4j.create(new float[]{1, 3, 5, 7, 9, 11});
        nd.putRow(0, replaceRow);
        System.out.println(nd);
    }
}
