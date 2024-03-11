package test;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Scanner;
import java.util.Stack;

import scala.Tuple2;

public class GOPC_Tree {
    public Node root;//root节点
    public List<List<String>> Tu;//保存测试数据
    public Map<String,Float>score;//存储推荐的item的编号与值
    public static float maxs;
    /**
     * 构建树
     * @param file
     * @throws Exception
     */
    public void PopulateTreeView(List<String> s) throws Exception{
    	root = new Node("Root");
        int count = 0;
        Node nownode = new Node();
        for(String sline:s){
        	String ss[] = sline.split(" ");
        	for(int i=0;i<ss.length-1;i++){
        		if(i==0){
        			if(count ==0){
        				root.child_list = new ArrayList<Node>();
        				Node u = new Node(ss[i],0,root);
        				root.child_list.add(u);
        				count++;
        			}
        			else 
        			{
        				if(!root.child_list.get(root.child_list.size()-1).item_name.equals(ss[i])){
        					Node u = new Node(ss[i],0,root);
        					root.child_list.add(u);
        				}
        			}
        			nownode = root.child_list.get(root.child_list.size()-1);
        		}
        		else{
        			if(nownode.child_list ==null){
        				nownode.child_list = new ArrayList<Node>();
        				Node u = new Node(ss[i],0,nownode);
        				nownode.child_list.add(u);
        			}
        			else{
        				int listLength_1 = nownode.child_list.size();//当前节点孩子节点的个数
        				if(!nownode.child_list.get(listLength_1-1).item_name.equals(ss[i])){
        					Node u = new Node(ss[i],0,nownode);
        					nownode.child_list.add(u);
        					}
        			}
        			int listlength = nownode.child_list.size();
        			if(i==ss.length-2){
        				nownode.child_list.get(listlength-1).parent_stat += Float.parseFloat(ss[ss.length-1]);
        				nownode = null;
        				break;
        			}
        			nownode = nownode.child_list.get(listlength-1);
        		}		
        	}
        	if(ss.length==2){//将单项的支持度添加进去
        		nownode.parent_stat += Float.parseFloat(ss[ss.length-1]);
        	}
        }
    }
    /**
     * 读取测试数据
     * @param file
     * @throws Exception
     */
    public void ReadTU(String file) throws Exception{
    	Tu = new ArrayList<List<String>>();
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
    	String sline = "";
    	while ((sline = br.readLine())!= null){
    		String filerow[] = sline.split(" ");
 		   ArrayList<String> rowList=new ArrayList<String>();
 		   for(String s:filerow)
 			   {rowList.add(s);}
 		   Tu.add(rowList);
    	 }	
    }
    /**
     * 搜索路径
     * @param file
     * @param maxnum
     * @param k
     * @return 
     * @throws Exception
     */
    public List<Tuple2<Integer,List<Tuple2<String,Float>>>> SearchPath(Node root_another) throws Exception{
    	List<Tuple2<Integer,List<Tuple2<String,Float>>>> jieguo= new ArrayList<Tuple2<Integer,List<Tuple2<String,Float>>>>();
    	for(int i=0;i<Tu.size();i++){
    		score=new HashMap<String,Float>();//value值数组初始化
    		Stack<Node> s = new Stack<Node>();
    		ArrayList<Node>rootchild = root.child_list;//所有第二层节点
    		String iut ="";//推荐的item的编号初始化
    		maxs=0;//初始化最大为零
    		String results = "";
    		for(Node second :rootchild){
    			s.push(second);
    			while(s.size()!=0){
    				Node cur = s.pop();
    				iut = Pattern_Gen(cur, Tu.get(i),iut,root_another);
    				if (cur.color < 2 && cur.child_list!=null){
    					for(Node curchild:cur.child_list){
    						curchild.color=cur.color;//父节点的color值给孩子节点
    						s.push(curchild);
    					}
    				}
    				cur.color=0;//将访问过或者传递给孩子color值的节点的color值归零
    			}
    		}
    		Iterator<Map.Entry<String, Float>> entries = score.entrySet().iterator();
    		List<Tuple2<String,Float>> temp_sum = new ArrayList<Tuple2<String,Float>>();
    		while (entries.hasNext()) {
    	    	Map.Entry<String, Float> entry = entries.next(); 
   	 	    	float value=entry.getValue();
   	 	    	if(value>=maxs){
   	 	    	String key=entry.getKey();
   	 	    	Tuple2<String,Float> temp = new Tuple2<String,Float>(key,value);
   	 	    	temp_sum.add(temp);
   	 	    	}
    	    	}
    		jieguo.add(new Tuple2<Integer,List<Tuple2<String,Float>>>((i+1),temp_sum));
    	}
    	return jieguo;
	 }
    public String Pattern_Gen(Node cur,List<String> tu, String iutold,Node root_another){
    	if(tu.contains(cur.item_name)&&cur.color==1){
    		float former = Prefix_value(cur, iutold,root_another);
    		float pattern = cur.parent_stat;
    		float confidence = pattern/former;
//    		System.out.println(confidence);
//    		System.out.println(confidence);
    		if(confidence>maxs){
    			score.clear();
    			maxs=confidence;
    			score.put(iutold, confidence);
    		}
    		if(confidence==maxs){
				score.put(iutold,confidence);
			}
    	}
    	else if(tu.contains(cur.item_name)==false){
    		cur.color+=1;
    		if(cur.color==1){//如果是第一次匹配未成功
    			String iut=cur.item_name;//字符串
//    			if(score.containsKey(iut)) score.put(iut,score.get(iut)+cur.parent_stat);
//    			else score.put(iut,cur.parent_stat);
    			if(!cur.parent.item_name.equals("Root")){
    				float former = Prefix_value(cur, iut, root_another);
    				float pattern = cur.parent_stat;
    				float confidence = pattern/former;
    				if(confidence>maxs){
    					score.clear();
    					maxs=confidence;
        				score.put(iut,confidence);
    				}
    				if(confidence==maxs){
    					score.put(iut,confidence);
    				}
    			}
    			return iut;//新的推荐的item编号
    		}
    	}
    	return iutold;//旧的推荐的item编号
    }
    public float Prefix_value(Node cur,String iutold,Node root_another){//去查找前缀路径对应的值
    	List<String> prefix= new ArrayList<String>();
    	float value = 0;
    	Node nowNode = cur;
    	while(!nowNode.item_name.equals(new String("Root"))){//获取前缀路径
    		if(!nowNode.item_name.equals(iutold))prefix.add(nowNode.item_name);
    		nowNode = nowNode.parent;
    	}
    	nowNode = root_another;
    	//从树上搜索值
    	for(int i = prefix.size()-1;i>=0;i--){
//    		for(int j =0;j<nowNode.child_list.size();j++){
//    			if(nowNode.child_list.get(j).item_name.equals(prefix.get(i))){ //如果查找到则继续向下查找
//    				nowNode = nowNode.child_list.get(j);//指针指向孩子节点
//    				break;//退出当前循环
//    			}
//    		}
    		int index = binarySearch(nowNode, prefix.get(i));
    		nowNode = nowNode.child_list.get(index);
    	}
    	value = nowNode.parent_stat;
    	return value;
    }
    public static int binarySearch(Node cur, String aim){   
    	int low = 0;   
        int high = cur.child_list.size()-1;   
        while(low <= high) {   
            int middle = (low + high)/2;
            int value = cur.child_list.get(middle).item_name.compareTo(aim);
            if(value==0) {   
                return middle;   
            }else if(value<0) {   
                low = middle + 1;   
            }else {   
                high = middle - 1;   
            }  
        }  
        return -1;  
   }  
}
