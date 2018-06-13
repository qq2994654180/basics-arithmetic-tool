package association.datamining_apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author liuguotao
 * @date 2018/6/1 000116:08
 * 关联规则算法
 */
public class AprioriTool {
    // 最小支持度计数
    private int minSupportCount;
    // 测试数据文件地址
    private String filePath;
    // 每个事务中的商品ID
    private ArrayList<String[]> totalGoodsIDs;
    // 过程中计算出来的所有频繁项集列表
    private ArrayList<FrequentItem> resultItem;
    // 过程中计算出来频繁项集的ID集合
    private ArrayList<String[]> resultItemID;
    //存放强类型
    private ArrayList<Map<String,String []>> strong;
    //存放弱类型
    private ArrayList<Map<String,String []>> weak;
    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:47
        * @param []
        * @return java.util.ArrayList<java.util.Map<java.lang.String,java.lang.String[]>>
        *     获取强类型
    　*/
    public ArrayList<Map<String, String[]>> getStrong() {
        return strong;
    }
/*/**
　  * @author liuguotao
    * @date 2018/6/5 0005 10:48
    * @param []
    * @return java.util.ArrayList<java.util.Map<java.lang.String,java.lang.String[]>>
    *     获取弱类型
　*/
    public ArrayList<Map<String, String[]>> getWeak() {
        return weak;
    }
/*/**
　  * @author liuguotao
    * @date 2018/6/5 0005 10:48
    * @param [filePath, minSupportCount]
    * @return   初始化工具
　*/
    public AprioriTool(String filePath, int minSupportCount) {
        this.filePath = filePath;//获取测试数据集地址
        this.minSupportCount = minSupportCount;//自定义的最小支持度（主要用于裁剪过滤数据）
        readDataFile();
    }

   /*/**
   　  * @author liuguotao
       * @date 2018/6/5 0005 10:48
       * @param []
       * @return void
       * 读取学习模型并做处理
   　*/
    private void readDataFile() {
        File file = new File(filePath);//文件路径
        ArrayList<String[]> dataArray = new ArrayList<String[]>();//文件中的每项数据集数组（对于数据获取也是可以从各种数据库获得）

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));//字符串包装流用于读取速度
            String str;
            String[] tempArray;
            while ((str = in.readLine()) != null) {//读取文件每一条数据
                tempArray = str.split(" ");//通过空格分割得到数组
                dataArray.add(tempArray);//把数组添加到数据集合（实际项目中数据一般需要清洗组装补缺的）
            }
            in.close();//关闭流
        } catch (IOException e) {
            e.getStackTrace();
        }

        String[] temp = null;
        totalGoodsIDs = new ArrayList<>();//
        for (String[] array : dataArray) {
            temp = new String[array.length - 1];
            System.arraycopy(array, 1, temp, 0, array.length - 1);//截取到每个数组的可用数据并备份
            totalGoodsIDs.add(temp);
        }
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/2 0002 8:47
        * @param [array1, array2]
        * @return boolean
        * 判读字符数组array2是否包含于数组array1中
    　*/
    public boolean iSStrContain(String[] array1, String[] array2) {
        if (array1 == null || array2 == null) {
            return false;
        }

        boolean iSContain = false;
        for (String s : array2) {
            // 新的字母比较时，重新初始化变量
            iSContain = false;
            // 判读array2中每个字符，只要包括在array1中 ，就算包含
            for (String s2 : array1) {
                if (s.equals(s2)) {
                    iSContain = true;
                    break;
                }
            }

            // 如果已经判断出不包含了，则直接中断最外层循环返回false;
            if (!iSContain) {
                break;
            }
        }

        return iSContain;
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:50
        * @param []
        * @return void
        * 开始生成频繁项集链接运算
    　*/
    private void computeLink() {
        // 连接计算的终止数，k项集必须算到k-1子项集为止
        int endNum = 0;
        // 当前已经进行连接运算到几项集,开始时就是1项集
        int currentNum = 1;
        // 商品，1频繁项集映射图
        HashMap<String, FrequentItem> itemMap = new HashMap<>();
        FrequentItem tempItem;//频繁项集的实体
        // 初始列表
        ArrayList<FrequentItem> list = new ArrayList<>();
        // 经过连接运算后产生的结果项集
        resultItem = new ArrayList<>();//用于储存计算出的频繁项列表
        resultItemID = new ArrayList<>();//频繁项ID的集合数组
        // 商品ID的种类
        ArrayList<String> idType = new ArrayList<>();

        /**这个循环主要是从组织好的样本数据中计算出每个商品ID的出现次数*/
        for (String[] a : totalGoodsIDs) {//所有商品ID数组
            for (String s : a) {//变量每个数组中的商品Id
                if (!idType.contains(s)) {//判断商品ID的种类集合中是否包含这个ID s 如果包含则不添加，不包含则添加
                    tempItem = new FrequentItem(new String[] { s }, 1);//放入项集的集合，设置其支持度为1
                    idType.add(s);//加入商品ID种类的列表用于过滤重复的商品ID加入频繁项集
                    resultItemID.add(new String[] { s });//把一个数组加入到项集的集合
                } else {
                    // 支持度计数加1
                    tempItem = itemMap.get(s);//如果在商品类型ID中存在这个频繁项集则直接返回次项集
                    tempItem.setCount(tempItem.getCount() + 1);//他的项集编号+1
                }
                itemMap.put(s, tempItem);//填入频繁项集映射列表
            }
        }
        /**频繁项集映射图END*/

        // 将初始频繁项集转入到列表中，以便继续做连接运算
        for (Map.Entry entry : itemMap.entrySet()) {//的到项集映射的每个频繁实体放入初始项集集合
            list.add((FrequentItem) entry.getValue());
        }
        // 按照商品ID进行排序，否则连接计算结果将会不一致，将会减少
        Collections.sort(list);//这个列表里的实体重写了compareTo方法
        resultItem.addAll(list);//将排好序的频繁项集列表添加到用于计算的频繁项集列表

        String[] array1;
        String[] array2;
        String[] resultArray;
        ArrayList<String> tempIds;
        ArrayList<String[]> resultContainer;
        // 总共要算到endNum项集
        endNum = list.size() - 1;//设置计算到第几个项集（意思是初始项集是3，能组成的频繁项集数是2组）
        /**本循环用于组合频繁项集*/
        /*
        组合规则说明
        例如：[1,2,3]，[1,2,3] 两组数据
            组成初始频繁项集 [{1,}{2,}{3,}]
            第二频繁项集[{1,2}{1,3}{2,3}]
         */
        while (currentNum < endNum) {//从第一个项集开始计算
            resultContainer = new ArrayList<>();
            for (int i = 0; i < list.size() - 1; i++) {//循环初始频繁项集
                tempItem = list.get(i);
                array1 = tempItem.getIdArray();//获取每个项集实体赋值给array1用于取第一个实体频繁项集
                for (int j = i + 1; j < list.size(); j++) {//j的初始是在i的基础上加1所以用于取第二的实体项集
                    tempIds = new ArrayList<>();
                    array2 = list.get(j).getIdArray();//取到第二个实体项集添加到第二个数组
                    for (int k = 0; k < array1.length; k++) {//遍历第一个项集集合
                        // 如果对应位置上的值相等的时候，只取其中一个值，做了一个连接删除操作
                        if (array1[k].equals(array2[k])) {//如果第一个和第二个集合相等则取一个加入项集ID数组
                            tempIds.add(array1[k]);
                        } else {
                            tempIds.add(array1[k]);
                            tempIds.add(array2[k]);
                        }
                    }
                    resultArray = new String[tempIds.size()];//创建长度和项集集合长度相同的数组用来判断项集长度符不符合要求
                    tempIds.toArray(resultArray);

                    boolean isContain = false;
                    // 过滤不符合条件的的ID数组，包括重复的和长度不符合要求的
                    if (resultArray.length == (array1.length + 1)) {
                        isContain = isIDArrayContains(resultContainer,
                                resultArray);//判断项集池中有没有这个项集列表
                        if (!isContain) {
                            resultContainer.add(resultArray);//如果没有就添加
                        }
                    }
                }
            }

            // 做频繁项集的剪枝处理，必须保证新的频繁项集的子项集也必须是频繁项集
            list = cutItem(resultContainer);//根据支持度和新子集验证把新的频繁项集赋值给初始值
            currentNum++;
        }
        /**本循环用于组合频繁项集END*/
        // 输出频繁项集
        for (int k = 1; k <= currentNum; k++) {
            System.out.println("频繁" + k + "项集：");
            for (FrequentItem i : resultItem) {
                if (i.getLength() == k) {
                    System.out.print("{");
                    for (String t : i.getIdArray()) {
                        System.out.print(t + ",");
                    }
                    System.out.print("},");
                }
            }
            System.out.println();
        }
    }

   /*/**
   　  * @author liuguotao
       * @date 2018/6/5 0005 10:51
       * @param [container, array]
       * @return boolean
       * 用于判断数组包含
   　*/
    private boolean isIDArrayContains(ArrayList<String[]> container,
                                      String[] array) {
        boolean isContain = true;
        if (container.size() == 0) {
            isContain = false;
            return isContain;
        }

        for (String[] s : container) {
            // 比较的视乎必须保证长度一样
            if (s.length != array.length) {
                continue;
            }

            isContain = true;
            for (int i = 0; i < s.length; i++) {
                // 只要有一个id不等，就算不相等
                if (s[i] != array[i]) {
                    isContain = false;
                    break;
                }
            }

            // 如果已经判断是包含在容器中时，直接退出
            if (isContain) {
                break;
            }
        }

        return isContain;
    }

  /*/**
  　  * @author liuguotao
      * @date 2018/6/5 0005 10:51
      * @param [resultIds]
      * @return java.util.ArrayList<AssociationAnalysis.datamining_apriori.FrequentItem>
      *     对频繁项集做剪枝步骤，必须保证新的频繁项集的子项集也必须是频繁项集
  　*/
    private ArrayList<FrequentItem> cutItem(ArrayList<String[]> resultIds) {//将构建好的频繁项集传过来做剪枝处理
        String[] temp;
        // 忽略的索引位置，以此构建子集
        int igNoreIndex = 0;
        FrequentItem tempItem;
        // 剪枝生成新的频繁项集
        ArrayList<FrequentItem> newItem = new ArrayList<>();
        // 不符合要求的id
        ArrayList<String[]> deleteIdArray = new ArrayList<>();
        // 子项集是否也为频繁子项集
        boolean isContain = true;
        /**本循环，主要用于验证生成的频繁项集中的每个元素，是否是存在的元素，也就是初始频繁项集是否包含，包含为合法元素，不包含做剪枝处理*/
        for (String[] array : resultIds) {
            // 列举出其中的一个个的子项集，判断存在于频繁项集列表中
            temp = new String[array.length - 1];
            for (igNoreIndex = 0; igNoreIndex < array.length; igNoreIndex++) {//循环每个子项集
                isContain = true;
                for (int j = 0, k = 0; j < array.length; j++) {
                    if (j != igNoreIndex) {//如果相等说明是同一个元素
                        temp[k] = array[j];
                        k++;
                    }
                }

                if (!isIDArrayContains(resultItemID, temp)) {//判断初始频繁项集是否包含temp项集
                    isContain = false;
                    break;
                }
            }

            if (!isContain) {
                deleteIdArray.add(array);
            }
        }
        /**END*/
        // 移除不符合条件的ID组合
        resultIds.removeAll(deleteIdArray);
        /**本循环主要用于计算源数据中包含子项集的个数用来判断支持度是否足够*/
        // 移除支持度计数不够的id集合
        int tempCount = 0;
        for (String[] array : resultIds) {
            tempCount = 0;
            for (String[] array2 : totalGoodsIDs) {
                if (isStrArrayContain(array2, array)) {
                    tempCount++;
                }
            }
        /**END*/
            // 如果支持度计数大于等于最小最小支持度计数则生成新的频繁项集，并加入结果集中
            if (tempCount >= minSupportCount) {
                tempItem = new FrequentItem(array, tempCount);//将每一个子集和这个子集的支持度创建一个频繁项集对象
                newItem.add(tempItem);//添加到新的频繁项集
                resultItemID.add(array);//添加到用于计算的频繁项集
                resultItem.add(tempItem);//添加到所有频繁项集列表
            }
        }

        return newItem;//返回做过剪枝的新项集
    }

   /*/**
   　  * @author liuguotao
       * @date 2018/6/5 0005 10:52
       * @param [array1, array2]
       * @return boolean
       * 数组array2是否包含于array1中，不需要完全一样
   　*/
    private boolean isStrArrayContain(String[] array1, String[] array2) {
        boolean isContain = true;
        for (String s2 : array2) {
            isContain = false;
            for (String s1 : array1) {
                // 只要s2字符存在于array1中，这个字符就算包含在array1中
                if (s2.equals(s1)) {
                    isContain = true;
                    break;
                }
            }

            // 一旦发现不包含的字符，则array2数组不包含于array1中
            if (!isContain) {
                break;
            }
        }

        return isContain;
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:53
        * @param [minConf]
        * @return void
        * 开始计算，并把生成强弱类型关系
    　*/
    public void printAttachRule(double minConf) {
        /** 进行连接和剪枝操作*/
        computeLink();
        /**END*/
        int count1 = 0;
        int count2 = 0;
        ArrayList<String> childGroup1;
        ArrayList<String> childGroup2;
        String[] group1;
        String[] group2;
        // 以最后一个频繁项集做关联规则的输出
        String[] array = resultItem.get(resultItem.size() - 1).getIdArray();
        // 子集总数，计算的时候除去自身和空集
        int totalNum = (int) Math.pow(2, array.length);//2的N次幂
        String[] temp;
        // 二进制数组，用来代表各个子集
        int[] binaryArray;
        //用来存放若类型分类集合
        ArrayList<Map<String,String[]>> weakMaps = new ArrayList<>();
        //用来存放强类型分类集合
        ArrayList<Map<String,String[]>> strongMaps = new ArrayList<>();


        /**根据2进制分类后，计算置信度*/
        for (int i = 1; i < totalNum - 1; i++) {
            binaryArray = new int[array.length];
            numToBinaryArray(binaryArray, i);//用二进制将要关联的项集排列成两中组合来做关联置信度

            childGroup1 = new ArrayList<>();
            childGroup2 = new ArrayList<>();
            count1 = 0;
            count2 = 0;
            // 按照二进制位关系取出子集 主要根据0,1分类
            for (int j = 0; j < binaryArray.length; j++) {
                if (binaryArray[j] == 1) {
                    childGroup1.add(array[j]);
                } else {
                    childGroup2.add(array[j]);
                }
            }

            group1 = new String[childGroup1.size()];
            group2 = new String[childGroup2.size()];

            childGroup1.toArray(group1);
            childGroup2.toArray(group2);
            /**用来计算分类项1在每个数据源下 分类项2发生的次数，并记录出现在数据源中的次数*/
            for (String[] a : totalGoodsIDs) {
                if (isStrArrayContain(a, group1)) {
                    count1++;//判断分组子集集1在源数据中的个数

                    // 在group1的条件下，统计group2的事件发生次数
                    if (isStrArrayContain(a, group2)) {
                        count2++;
                    }
                }
            }
            /**END**/
            // {A}-->{B}的意思为在A的情况下发生B的概率
            System.out.print("{");
            for (String s : group1) {
                System.out.print(s + ", ");
            }
            System.out.print("}-->");
            System.out.print("{");
            for (String s : group2) {
                System.out.print(s + ", ");
            }
            System.out.print(MessageFormat.format(
                    "},confidence(置信度)：{0}/{1}={2}", count2, count1, count2
                            * 1.0 / count1));//出现概率(1/n为散发函数)
            if (count2 * 1.0 / count1 < minConf) {
                // 不符合要求，不是强规则
                System.out.println("由于此规则置信度未达到最小置信度的要求，不是强规则");
                Map<String, String []> stringListHashMap = new HashMap<>();
                stringListHashMap.put("1",group1);
                stringListHashMap.put("2",group2);
                weakMaps.add(stringListHashMap);
                weak=weakMaps;
            } else {
                Map<String, String []> stringListHashMap = new HashMap<>();
                stringListHashMap.put("1",group1);
                stringListHashMap.put("2",group2);
                strongMaps.add(stringListHashMap);
                strong=strongMaps;
                System.out.println("为强规则");
            }
        }
    /**END*/
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 10:52
        * @param [binaryArray, num]
        * @return void
        * 二进制分类
    　*/
    private void numToBinaryArray(int[] binaryArray, int num) {
        int index = 0;
        while (num != 0) {
            binaryArray[index] = num % 2;
            index++;
            num /= 2;
        }
    }


}
