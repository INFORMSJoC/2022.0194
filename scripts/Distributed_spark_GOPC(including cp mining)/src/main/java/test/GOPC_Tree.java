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

import scala.Tuple2;

import java.util.Scanner;
import java.util.Stack;

public class GOPC_Tree {
    public Node root;//root�ڵ�
    public List<List<String>> Tu;//�����������
    public Map<String,Float>score;//�洢�Ƽ���item�ı����ֵ
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
     * @throws Exception
     */
    public List<Tuple2<Integer,List<Tuple2<String,Float>>>> SearchPath() throws Exception{
    	List<Tuple2<Integer,List<Tuple2<String,Float>>>> jieguo= new ArrayList<Tuple2<Integer,List<Tuple2<String,Float>>>>();
    	for(int i=0;i<Tu.size();i++){
    		score=new HashMap<String,Float>();//valueֵ�����ʼ��
    		Stack<Node> s = new Stack<Node>();
    		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
    		String iut ="";//�Ƽ���item�ı�ų�ʼ��
    		for(Node second :rootchild){
    			s.push(second);
    			while(s.size()!=0){
    				Node cur = s.pop();
    				iut = Pattern_Gen(cur, Tu.get(i),iut);
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
   	 	    	if(value!=0.0){
   	 	    	String key=entry.getKey();
   	 	    	Tuple2<String,Float> temp = new Tuple2<String,Float>(key,value);
	 	    	temp_sum.add(temp);
   	 	    	}
    	    	}
    		jieguo.add(new Tuple2<Integer,List<Tuple2<String,Float>>>((i+1),temp_sum));
    	}
    	return jieguo;
	 }
    public String Pattern_Gen(Node cur,List<String> tu, String iutold){
    	if(tu.contains(cur.item_name)&&cur.color==1){
    		score.put(iutold, score.get(iutold) + cur.parent_stat);
    	}
    	else if(tu.contains(cur.item_name)==false){
    		cur.color+=1;
    		if(cur.color==1){
    			String iut=cur.item_name;//�ַ���ת����
//    			if(score.containsKey(iut)) score.put(iut,score.get(iut)+cur.parent_stat);
//    			else score.put(iut,cur.parent_stat);
    			score.put(iut,cur.parent_stat);
    			return iut;//�µ��Ƽ���item���
    		}
    	}
    	return iutold;//�ɵ��Ƽ���item���
    }
    /**
     * ���������
     * @param cur
     * @return
     */
    public int deepth(Node cur){
    	if(cur.child_list == null)
    		return 1;
    	else
    	{
    		List<Integer> allChildDeepth = new ArrayList<Integer>();
    		for(Node t: cur.child_list)
    		{
    			allChildDeepth.add(deepth(t));
    		}
    		Collections.sort(allChildDeepth);
    		return (int)allChildDeepth.get(allChildDeepth.size()-1) + 1;
    	}
    }
    /**
     * ������pattern����
     * @return
     */
    public int pattern_count(){
    	int count = 0;
    	Stack<Node> s = new Stack<Node>();
		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
		for(Node second :rootchild){
			s.push(second);
			while(s.size()!=0){
				Node cur = s.pop();
				if (cur.parent_stat!=0) count++;
				if(cur.child_list!=null)
				{
					for(Node curChild:cur.child_list)
					{
						s.push(curChild);
					}
				}
				}
			}
		return count;
    }
    /**
     * ��Ҷ�ӽڵ���
     * @return
     */
    public  int leaf_node()
    {
//    	if(s ==null)return 0;
//    	if(s.child_list==null)return 1;
//    	int sum = 0;
//    	for(Node n : s.child_list)sum += leaf_node(n);
//    	return sum;
    	int count = 0;
    	Stack<Node> s = new Stack<Node>();
		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
		for(Node second :rootchild){
			s.push(second);
			while(s.size()!=0){
				Node cur = s.pop();
				if (cur.child_list==null) count++;
				else
				{
					for(Node curChild:cur.child_list)
					{
						s.push(curChild);
					}
				}
				}
			}
		return count;
    }
    /**
     * ���ܽڵ���
     * @return
     */
    public  int total_node()
    {
//    	if(s==null)return 0;
//    	if(s.child_list==null)return 1;
//    	int sum = 1;
//    	for(Node n : s.child_list) sum+=total_node(n);
//    	return sum;
    	int count = 0;
    	Stack<Node> s = new Stack<Node>();
		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
		for(Node second :rootchild){
			s.push(second);
			while(s.size()!=0){
				Node cur = s.pop();
				count++;
				if(cur.child_list!=null)
				{
					for(Node curChild:cur.child_list)
					{
						s.push(curChild);
					}
				}
				}
			}
		return count;
    }
    /**
     * ��ӡ��������·��
     * @return
     */
    public List<String> print_tree()
	{
		List<String> result = new ArrayList<String>();
		if(root ==null) return result;
		helper(root,root.item_name,result);
		return result;
	}
	public void helper(Node rootnode, String path,List<String>result){
		if(rootnode ==null)return;
		if(rootnode.child_list==null)
			{
			result.add(path);
			return;
			}
		for(Node n :rootnode.child_list)
		{
			helper(n,path+"->"+n.item_name,result);
		}
	}
}
