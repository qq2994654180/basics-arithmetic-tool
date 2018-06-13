package association.datamining_fptree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author liuguotao
 * @date 2018/6/4 00049:58
 * 工具类
 */
public class FPTreeTool {
    // 输入数据文件位置
    private String filePath;
    // 最小支持度阈值
    private int minSupportCount;
    // 所有事物ID记录
    private ArrayList<String[]> totalGoodsID;
    // 各个ID的统计数目映射表项，计数用于排序使用
    private HashMap<String, Integer> itemCountMap;
    //用于查看树结构
    private TreeNode rootNode1;
    //存放最后频繁集
    ArrayList<Map<String, String[]>> schema = new ArrayList<>();//存放频繁集合
    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 11:26
        * @param []
        * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
        *     获取频繁模式
    　*/
    public List<Map<String, String[]>> getSchema() {
        return schema;
    }
/*/**
　  * @author liuguotao
    * @date 2018/6/5 0005 11:27
    * @param [filePath, minSupportCount]
    * @return
    *初始化工具
　*/
    public FPTreeTool(String filePath, int minSupportCount) {
        this.filePath = filePath;
        this.minSupportCount = minSupportCount;
        readDataFile();
    }

   /*/**
   　  * @author liuguotao
       * @date 2018/6/5 0005 11:27
       * @param []
       * @return void
       * 数据读取并计算支持度
   　*/
    private void readDataFile() {
        File file = new File(filePath);
        ArrayList<String[]> dataArray = new ArrayList<String[]>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            String[] tempArray;
            while ((str = in.readLine()) != null) {
                tempArray = str.split(" ");
                dataArray.add(tempArray);
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }

        String[] temp;
        int count = 0;
        itemCountMap = new HashMap<>();
        totalGoodsID = new ArrayList<>();
        for (String[] a : dataArray) {
            temp = new String[a.length - 1];
            System.arraycopy(a, 1, temp, 0, a.length - 1);//过滤掉项集名称
            totalGoodsID.add(temp);
            for (String s : temp) {
                if (!itemCountMap.containsKey(s)) {//判断映射表项集是否包含，如果不包含添加是支持度是1，如果包含支持度+1
                    count = 1;
                } else {
                    count = ((int) itemCountMap.get(s));
                    // 支持度计数加1
                    count++;
                }
                // 更新表项
                itemCountMap.put(s, count);
            }
        }
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 11:28
        * @param [suffixPattern, transctionList]
        * @return void
        * 构造模式树
    　*/
    private void buildFPTree(ArrayList<String> suffixPattern,
                             ArrayList<ArrayList<TreeNode>> transctionList) {
        // 设置一个空根节点
        TreeNode rootNode = new TreeNode(null, 0);
        int count = 0;
        // 节点是否存在
        boolean isExist = false;
        ArrayList<TreeNode> childNodes;
        ArrayList<TreeNode> pathList;
        // 相同类型节点链表，用于构造的新的FP树
        HashMap<String, ArrayList<TreeNode>> linkedNode = new HashMap<>();
        HashMap<String, Integer> countNode = new HashMap<>();//树名称和出现次数
        // 根据事物记录，一步步构建FP树
        for (ArrayList<TreeNode> array : transctionList) {//循环每个数子集
            TreeNode searchedNode;//用于存放查找到的树节点
            pathList = new ArrayList<>();//用于生成一个子集元素的列表
            for (TreeNode node : array) {//循环每个子集中的树
                pathList.add(node);
                nodeCounted(node, countNode);//设置每个树节点的出现次数
                searchedNode = searchNode(rootNode, pathList);//找到的最后一个节点
                childNodes = searchedNode.getChildNodes();//获取到最后一个节点的子节点

                if (childNodes == null) {//如果这个节点下的子节点为null那么直接添加这个节点到子节点
                    childNodes = new ArrayList<>();
                    childNodes.add(node);
                    searchedNode.setChildNodes(childNodes);
                    node.setParentNode(searchedNode);
                    nodeAddToLinkedList(node, linkedNode);
                } else {//如果子列表不为空
                    isExist = false;
                    for (TreeNode node2 : childNodes) {
                        // 如果找到名称相同，则更新支持度计数
                        if (node.getName().equals(node2.getName())) {
                            count = node2.getCount() + node.getCount();
                            node2.setCount(count);
                            // 标识已找到节点位置
                            isExist = true;
                            break;
                        }
                    }

                    if (!isExist) {
                        // 如果没有找到，需添加子节点
                        childNodes.add(node);
                        node.setParentNode(searchedNode);
                        nodeAddToLinkedList(node, linkedNode);
                    }
                }

            }

        }

        // 如果FP树已经是单条路径，则输出此时的频繁模式
        if (isSinglePath(rootNode)) {
            Map<String, String[]> stringObjectMap = printFrequentPattern(suffixPattern, rootNode);
            schema.add(stringObjectMap);
            System.out.println("-------");
        } else {
            ArrayList<ArrayList<TreeNode>> tList;
            ArrayList<String> sPattern;
            if (suffixPattern == null) {
                sPattern = new ArrayList<>();
            } else {
                // 进行一个拷贝，避免互相引用的影响
                sPattern = (ArrayList<String>) suffixPattern.clone();
            }

            // 利用节点链表构造新的事务
            for (Map.Entry entry : countNode.entrySet()) {
                // 添加到后缀模式中
                sPattern.add((String) entry.getKey());
                //获取到了条件模式机，作为新的事务
                tList = getTransactionList((String) entry.getKey(), linkedNode);

                System.out.print("[后缀模式]：{");
                for(String s: sPattern){
                    System.out.print(s + ", ");
                }
                System.out.print("}, 此时的条件模式基：");
                for(ArrayList<TreeNode> tnList: tList){
                    System.out.print("{");
                    for(TreeNode n: tnList){
                        System.out.print(n.getName() + ", ");
                    }
                    System.out.print("}, ");
                }
                System.out.println();
                // 递归构造FP树
                buildFPTree(sPattern, tList);
                // 再次移除此项，构造不同的后缀模式，防止对后面造成干扰
                sPattern.remove((String) entry.getKey());
            }
        }
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 11:28
        * @param [node, linkedList]
        * @return void
        * 添加树节点
    　*/
    private void nodeAddToLinkedList(TreeNode node,
                                     HashMap<String, ArrayList<TreeNode>> linkedList) {
        String name = node.getName();
        ArrayList<TreeNode> list;

        if (linkedList.containsKey(name)) {//判断这个链表图中有没有这个节点
            list = linkedList.get(name);//如果有获取到
            // 将node添加到此队列中
            list.add(node);//并将这个节点添加到这个链表的集合中
        } else {
            list = new ArrayList<>();//如果没有就创建一个新的节点类型
            list.add(node);
            linkedList.put(name, list);
        }
    }

  /*/**
  　  * @author liuguotao
      * @date 2018/6/5 0005 11:29
      * @param [name, linkedList]
      * @return java.util.ArrayList<java.util.ArrayList<AssociationAnalysis.datamining_fptree.TreeNode>>
      *     根据链表构造出新的事务
  　*/
    private ArrayList<ArrayList<TreeNode>> getTransactionList(String name,
                                                              HashMap<String, ArrayList<TreeNode>> linkedList) {
        ArrayList<ArrayList<TreeNode>> tList = new ArrayList<>();
        ArrayList<TreeNode> targetNode = linkedList.get(name);
        ArrayList<TreeNode> singleTansaction;
        TreeNode temp;

        for (TreeNode node : targetNode) {
            singleTansaction = new ArrayList<>();

            temp = node;
            while (temp.getParentNode().getName() != null) {
                temp = temp.getParentNode();
                singleTansaction.add(new TreeNode(temp.getName(), 1));
            }

            // 按照支持度计数得反转一下
            Collections.reverse(singleTansaction);

            for (TreeNode node2 : singleTansaction) {
                // 支持度计数调成与模式后缀一样
                node2.setCount(node.getCount());
            }

            if (singleTansaction.size() > 0) {
                tList.add(singleTansaction);
            }
        }

        return tList;
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 11:29
        * @param [node, nodeCount]
        * @return void
        * 节点计数
    　*/
    private void nodeCounted(TreeNode node, HashMap<String, Integer> nodeCount) {
        int count = 0;
        String name = node.getName();

        if (nodeCount.containsKey(name)) {//判断节点内容集合中有没有这个节点
            count = nodeCount.get(name);//如果有取到后加1
            count++;
        } else {
            count = 1;//没有默认为1
        }

        nodeCount.put(name, count);//添加到内容集合
    }

    /**
     * 显示决策树
     *
     * @param node
     *            待显示的节点
     * @param blankNum
     *            行空格符，用于显示树型结构
     */
    public void showFPTree(TreeNode node, int blankNum) {
        System.out.println();
        for (int i = 0; i < blankNum; i++) {
            System.out.print("\t");
        }
        System.out.print("--");
        System.out.print("--");

        if (node.getChildNodes() == null) {
            System.out.print("[");
            System.out.print("I" + node.getName() + ":" + node.getCount());
            System.out.print("]");
        } else {
            // 递归显示子节点
             System.out.print("【" + node.getName() + "】");
            for (TreeNode childNode : node.getChildNodes()) {
                showFPTree(childNode, 2 * blankNum);
            }
        }

    }

    /**
     * 待插入节点的抵达位置节点，从根节点开始向下寻找待插入节点的位置
     *其实就是为了找到那个节点是list的最后一个节点
     * @param list
     * @return
     */
    private TreeNode searchNode(TreeNode node, ArrayList<TreeNode> list) {
        ArrayList<TreeNode> pathList = new ArrayList<>();
        TreeNode tempNode = null;//用于存放子节点
        TreeNode firstNode = list.get(0);
        boolean isExist = false;
        // 重新转一遍，避免出现同一引用
        for (TreeNode node2 : list) {
            pathList.add(node2);
        }

        // 如果没有孩子节点，则直接返回，在此节点下添加子节点
        if (node.getChildNodes() == null) {
            return node;
        }
/**如果这个节点下有子节点*/
        for (TreeNode n : node.getChildNodes()) {//遍历这个子节点
            if (n.getName().equals(firstNode.getName()) && list.size() == 1) {//判断这个子节点的名称是否和第一个相等并且在只有一个节点的情况下
                tempNode = node;//直接取到这个子节点
                isExist = true;//并且设置这个节点下不为空
                break;
            } else if (n.getName().equals(firstNode.getName())) {//如果有并且有多个的情况下
                // 还没有找到最后的位置，继续找
                pathList.remove(firstNode);//删掉当前这个因为不是最后一个
                tempNode = searchNode(n, pathList);//使用递归一直到返回（注意这里的pathList和传过来的list是一样的）
                return tempNode;
            }
        }

        // 如果没有找到，则新添加到孩子节点中
        if (!isExist) {
            tempNode = node;
        }

        return tempNode;
    }

  /*/**
  　  * @author liuguotao
      * @date 2018/6/5 0005 14:21
      * @param [rootNode]
      * @return boolean
      * 判断目前构造的FP树是否是单条路径的
  　*/
    private boolean isSinglePath(TreeNode rootNode) {
        // 默认是单条路径
        boolean isSinglePath = true;
        ArrayList<TreeNode> childList;
        TreeNode node;
        node = rootNode;

        while (node.getChildNodes() != null) {
            childList = node.getChildNodes();
            if (childList.size() == 1) {
                node = childList.get(0);
            } else {
                isSinglePath = false;
                break;
            }
        }

        return isSinglePath;
    }

    /**
     * 开始构建FP树
     */
    /*/**
    　  * @author liuguotao
        * @date 2018/6/4 0004 10:35
        * @param []
        * @return void
        * 开始的
    　*/
    public void startBuildingTree() {
        ArrayList<TreeNode> singleTransaction;
        ArrayList<ArrayList<TreeNode>> transactionList = new ArrayList<>();
        TreeNode tempNode;
        int count = 0;

        for (String[] idArray : totalGoodsID) {
            singleTransaction = new ArrayList<>();
            for (String id : idArray) {//每一个项子集
                count = itemCountMap.get(id);//取到一个支持度数量
                tempNode = new TreeNode(id, count);//生成数节点（第几条，支持度）
                singleTransaction.add(tempNode);//添加到节点列表
            }

            // 根据支持度数的多少进行排序 例如：[{1,3}{2,5}{3,2}],排好序为[{3,2}{1,3}{2,5}]
            Collections.sort(singleTransaction);
            for (TreeNode node : singleTransaction) {
                // 支持度计数重新归为1
                node.setCount(1);
            }
            transactionList.add(singleTransaction);//添加新设置的树结构[{3,1}{1,1}{2,1}]
        }

        buildFPTree(null, transactionList);
    }

   /*/**
   　  * @author liuguotao
       * @date 2018/6/5 0005 13:33
       * @param [suffixPattern, rootNode]
       * @return void
       * 输出此单条路径下的频繁模式
   　*/
    private Map<String, String[]> printFrequentPattern(ArrayList<String> suffixPattern,
                                                       TreeNode rootNode) {
        ArrayList<String> idArray = new ArrayList<>();


        TreeNode temp;
        temp = rootNode;
        // 用于输出组合模式
        int length = 0;
        int num = 0;
        int[] binaryArray;

        while (temp.getChildNodes() != null) {
            temp = temp.getChildNodes().get(0);

            // 筛选支持度系数大于最小阈值的值
            if (temp.getCount() >= minSupportCount) {
                idArray.add(temp.getName());
            }
        }

        length = idArray.size();
        num = (int) Math.pow(2, length);

        Map<String, String[]> map = new HashMap<>();

        for (int i = 0; i < num; i++) {
            binaryArray = new int[length];
            numToBinaryArray(binaryArray, i);

            // 如果后缀模式只有1个，不能输出自身
            if (suffixPattern.size() == 1 && i == 0) {
                continue;
            }

            System.out.print("频繁模式：{【后缀模式：");
            // 先输出固有的后缀模式

            if (suffixPattern.size() > 1
                    || (suffixPattern.size() == 1 && idArray.size() > 0)) {
                for (String s : suffixPattern) {
                    System.out.print(s + ", ");

                }
                String[] objects = suffixPattern.toArray(new String[suffixPattern.size()]);
                map.put("1",objects);
            }
            System.out.print("】");
            // 输出路径上的组合模式
            int[] oftenSchema;
            oftenSchema=binaryArray;
            for (int j = 0; j < length; j++) {
                if (binaryArray[j] == 1) {

                    System.out.print(idArray.get(j) + ", ");
                }
            }
            String [] arr=new String[oftenSchema.length];
            for (int z=0; z<oftenSchema.length; z++){
                arr[z]=oftenSchema[z]+"";
            }
            map.put("2",arr);
            System.out.println("}");
        }
        return map;
    }

    /*/**
    　  * @author liuguotao
        * @date 2018/6/5 0005 13:34
        * @param [binaryArray, num]
        * @return void
        * 数字转为二进制形式
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
