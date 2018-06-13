package association.datamining_apriori;

/**
 * @author liuguotao
 * @date 2018/6/1 000116:12
 */
public class FrequentItem  implements Comparable<FrequentItem>{
    // 频繁项集的集合ID
    private String[] idArray;
    // 频繁项集的支持度计数
    private int count;
    //频繁项集的长度，1项集或是2项集，亦或是3项集
    private int length;

    public FrequentItem(String[] idArray, int count){
        this.idArray = idArray;
        this.count = count;
        length = idArray.length;
    }

    public String[] getIdArray() {
        return idArray;
    }

    public void setIdArray(String[] idArray) {
        this.idArray = idArray;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
//重写比较器用来商品ID排序
    @Override
    public int compareTo(FrequentItem o) {
        // TODO Auto-generated method stub
        Integer int1 = Integer.parseInt(this.getIdArray()[0]);//取到商品ID并转换为int类型
        Integer int2 = Integer.parseInt(o.getIdArray()[0]);

        return int1.compareTo(int2);//根据商品ID排序
    }
}
