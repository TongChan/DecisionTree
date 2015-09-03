/**
 * Created by Ivy on 15-1-23.
 */

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.math.BigDecimal;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyDecisionTree {
    private static final int DEF_DIV_SCALE = 5;

    public static void main(String[] args) throws Exception {
    // Read the database
        File attrNameFile = new File("loan-attr.txt");
        File fileData = new File("loan-train.txt");
        String[] attrNames = readAttrNames(attrNameFile);
    // Generate decision tree
        Map<Object, List<RowData>> loanTrain = readTxtFile(fileData,attrNames);
        Object DecisionTreeOfLoanDefault = treeGrowth(loanTrain, attrNames);
    // Print decision tree
        outputTree(DecisionTreeOfLoanDefault, 0, null);
    //Same as heart-train and heart-test file
        attrNameFile = new File("heart-attr.txt");
        fileData = new File("heart-train.txt");
        File testFileData = new File("heart-test.txt");
        attrNames = readAttrNames(attrNameFile);
        Map<Object, List<RowData>> heartTrain = readTxtFile(fileData,attrNames);
        Map<Object, List<RowData>> heartTest = readTxtFile(testFileData,attrNames);
        Object DecisionTreeOfHeartTrain = treeGrowth(heartTrain, attrNames);
        Object DecisionTreeOfHeartTest = treeGrowth(heartTest, attrNames);
        outputTree(DecisionTreeOfHeartTrain, 0, null);
        outputTree(DecisionTreeOfHeartTest, 0, null);
    }

    /**
     * Translate Object to Map, return Map
     */
    public static String[] readAttrNames(File attrNameFile)throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(attrNameFile));
        String attrNamesLine;
        int size=0;
        List<String> attrName1 = new ArrayList<String>();
        while((attrNamesLine = in.readLine()) != null){
            List<String> attrName = new ArrayList<String>();
            if(attrNamesLine.split("\t")[0].split(" ")[1].equalsIgnoreCase("continuous")){
                //Mark the continuous attribute
            }
            attrName.add(attrNamesLine.split("\t")[0].split(" ")[0]);
            attrName1.add(attrName.toString());
        }
        in.close();
        size=attrName1.size()-1;
        String[] attributeNames = new String[size];
        for(int i=0;i<size;i++){
            attributeNames[i] = attrName1.get(i);
        }
        return attributeNames;
    }
    public static Map<Object, List<RowData>> readTxtFile(File file,String[] attrNames)throws IOException{
        int size1=0,size2=0;
        Map<Object, List<RowData>> ret = new HashMap<Object, List<RowData>>();
        BufferedReader in = new BufferedReader(new FileReader(file));
        BufferedReader in1 = new BufferedReader(new FileReader(file));
        String line,line1;
        List<List<Object>> Data = new ArrayList<List<Object>>();
        while((line1 = in1.readLine()) != null){
            size1++;
            String[] temp1 = line1.split("\t");
            size2=temp1[0].split(" ").length;
        }
        in1.close();
        String[][] txtData= new String[size1][size2];
        while((line = in.readLine()) != null){
            String[] temp = line.split("\t");
            for(int j=0;j<temp.length;j++) {
                List<Object> rawData = new ArrayList<Object>();
                for(int i =0; i < temp[j].split(" ").length;i++){
                    rawData.add(temp[j].split(" ")[i]);
                }
                Data.add(rawData);
            }
        }
        in.close();
        for(int p = 0;p<size1;p++){
            for (int k = 0;k<size2;k++){
            txtData[p][k] = (String) Data.get(p).get(k);
            }
        }
        for (Object[] row : txtData) {
            RowData RowData = new RowData();
            int i = 0;
            for (int n = row.length - 1; i < n; i++)
                RowData.setAttribute(attrNames[i], row[i]);
            RowData.setCategory(row[i]);
            List<RowData> RowDatas = ret.get(row[i]);
            if (RowDatas == null) {
                RowDatas = new LinkedList<RowData>();
                ret.put(row[i], RowDatas);
            }
            RowDatas.add(RowData);
        }
        return ret;
    }

    static Object treeGrowth(Map<Object, List<RowData>> categoryToRowDatas, String[] attrNames) {
        // if only have one data on the this attribute, classify it directly
        if (categoryToRowDatas.size() == 1)
            return categoryToRowDatas.keySet().iterator().next();

        Object[] rst = findBestSplit(categoryToRowDatas, attrNames);

        // choose the root for decision tree
        Tree tree = new Tree(attrNames[(Integer) rst[0]]);

        // remove the tested attributes
        String[] subA = new String[attrNames.length - 1];
        for (int i = 0, j = 0; i < attrNames.length; i++)
            if (i != (Integer) rst[0])
                subA[j++] = attrNames[i];

        // find the best split for child node
        Map<Object, Map<Object, List<RowData>>> splits = (Map<Object, Map<Object, List<RowData>>>) rst[2];
        for (Entry<Object, Map<Object, List<RowData>>> entry : splits.entrySet()) {
            Object attrValue = entry.getKey();
            Map<Object, List<RowData>> split = entry.getValue();
            Object child = treeGrowthChild(split, subA);
            tree.setChild(attrValue, child);
        }
        return tree;
    }
    //for the nodes, which is not continuous attribute
    static Object treeGrowthChild(Map<Object, List<RowData>> categoryToRowDatas, String[] attrNames) {
        if (categoryToRowDatas.size() == 1)
            return categoryToRowDatas.keySet().iterator().next();

        Object[] rst = findBestSplitChild(categoryToRowDatas, attrNames);
        Tree tree = new Tree(attrNames[(Integer) rst[0]]);
        String[] subA = new String[attrNames.length - 1];
        for (int i = 0, j = 0; i < attrNames.length; i++)
            if (i != (Integer) rst[0])
                subA[j++] = attrNames[i];
        Map<Object, Map<Object, List<RowData>>> splits = (Map<Object, Map<Object, List<RowData>>>) rst[2];
        for (Entry<Object, Map<Object, List<RowData>>> entry : splits.entrySet()) {
            Object attrValue = entry.getKey();
            Map<Object, List<RowData>> split = entry.getValue();
            Object child = treeGrowth(split, subA);
            tree.setChild(attrValue, child);
        }
        return tree;
    }

    static Object[] findBestSplitChild(Map<Object, List<RowData>> categoryToRowDatas, String[] attrNames) {
        int minIndex = -1; // Best split index
        // use BigDecimal type to compute the Gini value correct to 5 decimal places
        BigDecimal miniGini = new BigDecimal(Double.toString(1.0));
        BigDecimal minGiniValue = new BigDecimal(Double.toString(1.0));
        BigDecimal perGiniValue = new BigDecimal(Double.toString(0.0));
        BigDecimal curGiniValue = new BigDecimal(Double.toString(0.0));
        BigDecimal one = new BigDecimal(Double.toString(1.0));
        BigDecimal zero = new BigDecimal(Double.toString(0.0));
        Map<Object, Map<Object, List<RowData>>> minSplits = null; // Best split

        for (int attrIndex = 0; attrIndex < attrNames.length; attrIndex++) {
            double allCount = 0; // The number of total rows
            Map<Object, Map<Object, List<RowData>>> curSplits = new HashMap<Object, Map<Object, List<RowData>>>();
            for (Entry<Object, List<RowData>> entry : categoryToRowDatas.entrySet()) {
                Object category = entry.getKey();
                List<RowData> RowDatas = entry.getValue();
                for (RowData RowData : RowDatas) {
                    Object attrValue = RowData.getAttribute(attrNames[attrIndex]);
                    Map<Object, List<RowData>> split = curSplits.get(attrValue);
                    if (split == null) {
                        split = new HashMap<Object, List<RowData>>();
                        curSplits.put(attrValue, split);
                    }
                    List<RowData> splitRowDatas = split.get(category);
                    if (splitRowDatas == null) {
                        splitRowDatas = new LinkedList<RowData>();
                        split.put(category, splitRowDatas);
                    }
                    splitRowDatas.add(RowData);
                }
                allCount += RowDatas.size();
            }
            BigDecimal tmpGiniSum = new BigDecimal(Double.toString(0.0));
            for(Map.Entry<Object, Map<Object, List<RowData>>> entry1 : curSplits.entrySet()){
                //compute each attribute's Gini value
                Map<Object, List<RowData>> split1 = entry1.getValue();
                double sum = 0.0;
                double valueSize1 =0.0;
                BigDecimal tmpSum = new BigDecimal(Double.toString(0.0));
                List<Double> tmpValue = new ArrayList<Double>();
                for(Map.Entry<Object, List<RowData>> entry2 : split1.entrySet()) {
                    List<RowData> perRowData = entry2.getValue();
                    valueSize1 = (double) perRowData.size();
                    tmpValue.add(valueSize1);
                    sum = valueSize1 + sum;
                }
                for(int i=0; i<tmpValue.size();i++){
                    perGiniValue = div(tmpValue.get(i),sum,DEF_DIV_SCALE);
                    perGiniValue = mul(perGiniValue, perGiniValue);
                    tmpSum = add(perGiniValue, tmpSum);
                    perGiniValue = subBig(one, tmpSum);
                }
                curGiniValue = div(sum,allCount,DEF_DIV_SCALE);
                curGiniValue = mul(curGiniValue,perGiniValue);
                tmpGiniSum = add(tmpGiniSum,curGiniValue);
            }
            //if it's better than the current attribute, replace it
            if(tmpGiniSum.compareTo(minGiniValue) == -1){
                minIndex = attrIndex;
                minGiniValue = tmpGiniSum;
                minSplits = curSplits;
            }
        }
        return new Object[] { minIndex, minGiniValue, minSplits };
    }
    // For continues attribute split
    static Object[] findBestSplit(Map<Object, List<RowData>> categoryToRowDatas, String[] attrNames) {
        int minIndex = -1; // Best split index
        BigDecimal miniGini = new BigDecimal(Double.toString(1.0));
        BigDecimal minGiniValue = new BigDecimal(Double.toString(1.0));
        BigDecimal perGiniValue = new BigDecimal(Double.toString(0.0));
        BigDecimal curGiniValue = new BigDecimal(Double.toString(0.0));
        BigDecimal one = new BigDecimal(Double.toString(1.0));
        BigDecimal zero = new BigDecimal(Double.toString(0.0));
        Map<Object, Map<Object, List<RowData>>> minSplits = null;

        for (int attrIndex = 0; attrIndex < attrNames.length; attrIndex++) {
            double allCount = 0; // The number of total rows
            Map<Object, Map<Object, List<RowData>>> curSplits = new HashMap<Object, Map<Object, List<RowData>>>();
            for (Entry<Object, List<RowData>> entry : categoryToRowDatas.entrySet()) {
                Object category = entry.getKey();
                List<RowData> RowDatas = entry.getValue();
                for (RowData RowData : RowDatas) {
                    Object attrValue = RowData.getAttribute(attrNames[attrIndex]);
                    Map<Object, List<RowData>> split = curSplits.get(attrValue);
                    if (split == null) {
                        split = new HashMap<Object, List<RowData>>();
                        curSplits.put(attrValue, split);
                    }
                    List<RowData> splitRowDatas = split.get(category);
                    if (splitRowDatas == null) {
                        splitRowDatas = new LinkedList<RowData>();
                        split.put(category, splitRowDatas);
                    }
                    splitRowDatas.add(RowData);
                }
                allCount += RowDatas.size();
            }
            BigDecimal tmpGiniSum = new BigDecimal(Double.toString(0.0));
            for(Map.Entry<Object, Map<Object, List<RowData>>> entry1 : curSplits.entrySet()){
                Map<Object, List<RowData>> split1 = entry1.getValue();
                double sum = 0.0;
                double valueSize1 =0.0;
                BigDecimal tmpSum = new BigDecimal(Double.toString(0.0));
                List<Double> tmpValue = new ArrayList<Double>();
                for(Map.Entry<Object, List<RowData>> entry2 : split1.entrySet()) {
                    List<RowData> perRowData = entry2.getValue();
                    valueSize1 = (double) perRowData.size();
                    tmpValue.add(valueSize1);
                    sum = valueSize1 + sum;
                }
                for(int i=0; i<tmpValue.size();i++){
                    perGiniValue = div(tmpValue.get(i),sum,DEF_DIV_SCALE);
                    perGiniValue = mul(perGiniValue, perGiniValue);
                    tmpSum = add(perGiniValue, tmpSum);
                    perGiniValue = subBig(one, tmpSum);
                }
                curGiniValue = div(sum,allCount,DEF_DIV_SCALE);
                curGiniValue = mul(curGiniValue,perGiniValue);
                tmpGiniSum = add(tmpGiniSum,curGiniValue);
            }

            if(tmpGiniSum.compareTo(zero) ==0){
                List<BigDecimal> conValues = new ArrayList<BigDecimal>();
                for(int i=0;i<curSplits.keySet().size();i++){
                    //get the value of continuous attribute
                    BigDecimal eachData = new BigDecimal(curSplits.keySet().toArray()[i].toString());
                    conValues.add(eachData);
                }
                //sort the value
                Collections.sort(conValues);
                for(int j = 0;j<conValues.size();j++){
                    List<BigDecimal> lessValues1 = new ArrayList<BigDecimal>();
                    List<BigDecimal> largerValues1 = new ArrayList<BigDecimal>();
                    for(int p = 0;p<=j;p++){
                    lessValues1.add(conValues.get(p));
                    }
                    for(int k = j+1;k<conValues.size();k++){
                        largerValues1.add(conValues.get(k));
                    }
                    //compute the gini value for every split
                    List<Object> keyList = new ArrayList<Object>();
                    double noCount=0,yesCount=0,sum=0;
                    BigDecimal perGini = new BigDecimal(Double.toString(0.0));
                    BigDecimal perGini1 = new BigDecimal(Double.toString(0.0));
                    BigDecimal perGini2 = new BigDecimal(Double.toString(0.0));
                    BigDecimal tmp1 = new BigDecimal(Double.toString(0.0));
                    BigDecimal tmp2 = new BigDecimal(Double.toString(0.0));

                    for(int p1 = 0;p1<lessValues1.size();p1++) {
                        keyList.add(curSplits.get(lessValues1.get(p1).toString()).keySet());
                    }
                    for(int p1 = 0;p1<lessValues1.size();p1++) {
                        if(keyList.get(p1).toString().equalsIgnoreCase("[no]")){
                            noCount = noCount +1;
                        }
                        else{
                            yesCount = yesCount+1;
                        }
                    }
                    //System.out.println(lessValues1);
                    sum = noCount+yesCount;
                    tmp1 =div(noCount,sum,DEF_DIV_SCALE);
                    tmp2 =div(yesCount,sum,DEF_DIV_SCALE);
                    tmp1 = mul(tmp1,tmp1);
                    tmp2 = mul(tmp2,tmp2);
                    perGini1 = add(tmp1,tmp2);
                    perGini1 = subBig(one,perGini1);
                    List<Object> keyList1 = new ArrayList<Object>();
                    double noCount1=0,yesCount1=0,sum1=0;
                    for(int p1 = 0;p1<largerValues1.size();p1++) {
                        keyList1.add(curSplits.get(largerValues1.get(p1).toString()).keySet());
                    }
                    //System.out.println(keyList1);
                    if (keyList1.isEmpty()){
                        perGini2 = one;
                    }else {
                        for (int p1 = 0; p1 < largerValues1.size(); p1++) {
                            if (keyList1.get(p1).toString().equalsIgnoreCase("[no]")) {
                                noCount1 = noCount1 + 1;
                            } else {
                                yesCount1 = yesCount1 + 1;
                            }
                        }
                        sum1 = noCount1 + yesCount1;
                        tmp1 = div(noCount1, sum1, DEF_DIV_SCALE);
                        tmp1 = mul(tmp1, tmp1);
                        tmp2 = div(yesCount1, sum1, DEF_DIV_SCALE);
                        tmp2 = mul(tmp2, tmp2);
                        perGini2 = add(tmp1, tmp2);
                        perGini2 = subBig(one, perGini2);
                        perGini1 = mul(div(sum, allCount, DEF_DIV_SCALE), perGini1);
                        perGini2 = mul(div(sum1, allCount, DEF_DIV_SCALE), perGini2);
                        perGini = add(perGini1, perGini2);
                        //find the best Gini index
                        if (perGini.compareTo(miniGini) == -1) {
                            miniGini = perGini;
                            tmpGiniSum = miniGini;
                        }
                    }
                }

            }
            //compare to other attribute's gini value, replace them if it is better
            if(tmpGiniSum.compareTo(minGiniValue) == -1){
                minIndex = attrIndex;
                minGiniValue = tmpGiniSum;
                minSplits = curSplits;
            }
        }
        //System.out.print("Finally = " + minIndex+ " " + minGiniValue+" "+ minSplits + "\n");
        return new Object[] { minIndex, minGiniValue, minSplits };
    }
    /**
     * print the tree
     */
    static void outputTree(Object obj, int level, Object from) {
        for (int i = 0; i < level; i++)
            System.out.print("|");
        if (from != null)
            System.out.printf("(%s):", from);
        if (obj instanceof Tree) {
            Tree tree = (Tree) obj;
            String attrName = tree.getAttribute();
            System.out.printf("\n"+"%s = \n", attrName);
            for (Object attrValue : tree.getAttributeValues()) {
                Object child = tree.getChild(attrValue);
                outputTree(child, level + 1, attrName + " = " + attrValue);
            }
        } else {
            System.out.printf("  Class = %s\n", obj);
        }
    }
    /**
     * define the row data
     */
    static class RowData {
        private Map<String, Object> attributes = new HashMap<String, Object>();
        private Object category;
        public Object getAttribute(String name) {
            return attributes.get(name);
        }
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
        public Object getCategory() {
            return category;
        }
        public void setCategory(Object category) {
            this.category = category;
        }
        public String toString() {
            return attributes.toString();
        }
    }

    /**
     * define decision tree
     */
    static class Tree {

        private String attribute;
        private Map<Object, Object> children = new HashMap<Object, Object>();
        public Tree(String attribute) {
            this.attribute = attribute;
        }
        public String getAttribute() {
            return attribute;
        }
        public Object getChild(Object attrValue) {
            return children.get(attrValue);
        }
        public void setChild(Object attrValue, Object child) {
            children.put(attrValue, child);
        }
        public Set<Object> getAttributeValues() {
            return children.keySet();
        }
    }

    // the method to compute BigDecimal type of Gini number
    public static BigDecimal div(double v1,double v2,int scale){
        if(scale<0){
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        BigDecimal scale1 = new BigDecimal(Double.toString(0.0000000001));
        b2 = b2.add(scale1);
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mul(BigDecimal v1,BigDecimal v2){
        return v1.multiply(v2);
    }

    public static BigDecimal subBig(BigDecimal v1,BigDecimal v2){
        return v1.subtract(v2);
    }

    public static BigDecimal add(BigDecimal v1,BigDecimal v2){
        return v1.add(v2);
    }
    public static BigDecimal addDouble(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }
    public static BigDecimal perGiniValue(double v1,double v2){
        double sum = v1+v2;
        BigDecimal one = new BigDecimal(Double.toString(1.0));
        BigDecimal tmp1 =div(v1,sum,DEF_DIV_SCALE);
        tmp1 = mul(tmp1,tmp1);
        BigDecimal tmp2 =div(v2,sum,DEF_DIV_SCALE);
        tmp2 = mul(tmp2,tmp2);
        BigDecimal perGini = add(tmp1,tmp2);
        return subBig(one,perGini);
    }
}