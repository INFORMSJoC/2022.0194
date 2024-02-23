package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class FPGrowth_Sub {
	public static  Map<List<String>,Integer> frequentMap = new HashMap<List<String>,Integer>();
	/**
	 * 单条路径组合
	 * @param residualPath
	 * @param results
	 */
	public static void combine(LinkedList<FPNode> residualPath, List<List<FPNode>> results) {
        if (residualPath.size() > 0) {
            FPNode head = residualPath.poll();
            List<List<FPNode>> newResults = new ArrayList<List<FPNode>>();
            for (List<FPNode> list : results) {
                List<FPNode> listCopy = new ArrayList<FPNode>(list);
                newResults.add(listCopy);
            }
            for (List<FPNode> newPath : newResults) {
                newPath.add(head);
            }
            results.addAll(newResults);
            List<FPNode> list = new ArrayList<FPNode>();
            list.add(head);
            results.add(list);
            combine(residualPath, results);
        }
    }
	/**
	 * 读取文件返回字典
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static Map<LinkedList<String>,Integer> readTransaction(String name,int minsupport) throws IOException{
		List<List<String>> fre = new ArrayList<List<String>>();
		final Map<String,Integer> ss = new HashMap<String,Integer>();
		Map<LinkedList<String>,Integer> trans = new HashMap<LinkedList<String>,Integer>();
		FileReader fr = new FileReader(name);
		BufferedReader br = new BufferedReader(fr);
		String sline = "";
		while ((sline = br.readLine()) != null) {
			String[] filerow = sline.split(" ");
			Set<String> rowline = new HashSet<String>();
			for (int i = 0; i < filerow.length; i++)
			{
				rowline.add(filerow[i]);
			}
           ArrayList<String> al = new ArrayList<String>();
            al.addAll(rowline);
            fre.add(al);
	    	}
		for(List<String> list1 :fre){
			for(String s:list1){
				if(!ss.containsKey(s))ss.put(s, 0);
				ss.put(s, ss.get(s)+1);
			}
		}
		for(List<String> list1 :fre){
			LinkedList<String> temp = new LinkedList<String>();
			for(String s:list1)if(ss.get(s)>=minsupport)temp.add(s);
			Collections.sort(temp,new Comparator<String>(){
				public int compare(String arg0, String arg1) {
					// TODO Auto-generated method stub
					int result = (int) (ss.get(arg1) - ss.get(arg0));
					if(result == 0) return arg0.compareTo(arg1);
					else return result;
				}
			});//把s2中的元素按支持度降序排列
			if(!trans.containsKey(temp))trans.put(temp, 0);
			trans.put(temp, trans.get(temp)+1);
		}
		return trans;
	}
	/**
	 * FPGrowth算法
	 * @param tree
	 * @param postModel
	 */
	public static void FPGrowth(FPTree tree,LinkedList<String> postModel){
		if(!tree.isSinglePath()){
			LinkedList<FPNode> path = new LinkedList<FPNode>();
            FPNode currNode = tree.root;
            while (currNode.child.size()!=0) {
                currNode = currNode.child.get(0);
                path.add(currNode);
            }
           List<List<FPNode>> results = new ArrayList<List<FPNode>>();
           combine(path, results);
           for (List<FPNode> list : results) {
        	   int cnt = 0;
        	   List<String> rule = new ArrayList<String>();
        	   for (FPNode node : list) rule.add(node.name);
        	   if(list.size()!=0)cnt = list.get(list.size()-1).support;//cnt最FPTree叶节点的计数
        	   if (postModel != null)rule.addAll(postModel);
        	   frequentMap.put(rule, cnt);
        	   System.out.println(rule);
        	   System.out.println(cnt);
        	   System.out.println("*********");
        	   }
           return;
           }
		for(int i = tree.header.size()-1;i>=0;i--){
			List<String> rule = new ArrayList<String>();
			String item_name = tree.header.get(i);
			 rule.add(item_name);
			 if (postModel != null) {
	                rule.addAll(postModel);
	            }
			frequentMap.put(rule, tree.frequency.get(item_name));
			System.out.println(rule);
			System.out.println(tree.frequency.get(item_name));
			System.out.println("00000000");
			//tree.Flist.get(item_name)
			LinkedList<String> newPostPattern = new LinkedList<String>();
			newPostPattern.add(item_name);
            if (postModel != null) {
                newPostPattern.addAll(postModel);
            }
            List<List<String>> newCPB = new LinkedList<List<String>>();
			
			Map<LinkedList<String>,Integer> prefix = tree.getPrefixPath(item_name);
			FPTree subtree = new FPTree();
			subtree.minSupport = tree.minSupport;
			subtree.setFrequency(prefix);
			subtree.createHeader();
			subtree.buildTree(prefix);
			if(subtree!=null)FPGrowth(subtree, newPostPattern);
		}
	}
}

