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
    public Node root;//root�ڵ�
    public List<List<String>> Tu;//�����������
    public Map<String,Float>score;//�洢�Ƽ���item�ı����ֵ
    public static float maxs;
    /**
     * ������
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
        				int listLength_1 = nownode.child_list.size();//��ǰ�ڵ㺢�ӽڵ�ĸ���
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
        	if(ss.length==2){//�������֧�ֶ���ӽ�ȥ
        		nownode.parent_stat += Float.parseFloat(ss[ss.length-1]);
        	}
        }
    }
    /**
     * ��ȡ��������
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
     * ����·��
     * @param file
     * @param maxnum
     * @param k
     * @return 
     * @throws Exception
     */
    public List<Tuple2<Integer,List<Tuple2<String,Float>>>> SearchPath(Node root_another) throws Exception{
    	List<Tuple2<Integer,List<Tuple2<String,Float>>>> jieguo= new ArrayList<Tuple2<Integer,List<Tuple2<String,Float>>>>();
    	for(int i=0;i<Tu.size();i++){
    		score=new HashMap<String,Float>();//valueֵ�����ʼ��
    		Stack<Node> s = new Stack<Node>();
    		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
    		String iut ="";//�Ƽ���item�ı�ų�ʼ��
    		maxs=0;//��ʼ�����Ϊ��
    		String results = "";
    		for(Node second :rootchild){
    			s.push(second);
    			while(s.size()!=0){
    				Node cur = s.pop();
    				iut = Pattern_Gen(cur, Tu.get(i),iut,root_another);
    				if (cur.color < 2 && cur.child_list!=null){
    					for(Node curchild:cur.child_list){
    						curchild.color=cur.color;//���ڵ��colorֵ�����ӽڵ�
    						s.push(curchild);
    					}
    				}
    				cur.color=0;//�����ʹ����ߴ��ݸ�����colorֵ�Ľڵ��colorֵ����
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
    		if(cur.color==1){//����ǵ�һ��ƥ��δ�ɹ�
    			String iut=cur.item_name;//�ַ���
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
    			return iut;//�µ��Ƽ���item���
    		}
    	}
    	return iutold;//�ɵ��Ƽ���item���
    }
    public float Prefix_value(Node cur,String iutold,Node root_another){//ȥ����ǰ׺·����Ӧ��ֵ
    	List<String> prefix= new ArrayList<String>();
    	float value = 0;
    	Node nowNode = cur;
    	while(!nowNode.item_name.equals(new String("Root"))){//��ȡǰ׺·��
    		if(!nowNode.item_name.equals(iutold))prefix.add(nowNode.item_name);
    		nowNode = nowNode.parent;
    	}
    	nowNode = root_another;
    	//����������ֵ
    	for(int i = prefix.size()-1;i>=0;i--){
//    		for(int j =0;j<nowNode.child_list.size();j++){
//    			if(nowNode.child_list.get(j).item_name.equals(prefix.get(i))){ //������ҵ���������²���
//    				nowNode = nowNode.child_list.get(j);//ָ��ָ���ӽڵ�
//    				break;//�˳���ǰѭ��
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
