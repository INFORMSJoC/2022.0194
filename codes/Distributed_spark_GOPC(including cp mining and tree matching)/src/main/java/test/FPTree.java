package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FPTree {
	public FPNode root = null;//根节点
	public int minSupport;
	public List<String> header = new ArrayList<String>();//头表
	public Map<String,Integer> frequency = new HashMap<String,Integer>();//用来表示项目的支持度
	public Map<String,FPNode> firstNode = new HashMap<String,FPNode>();//指向每一个节点便于根据头表递归挖掘树
	public Map<String,FPNode> lastHomoNode = new HashMap<String,FPNode>();//指向链接中最后一个相同的节点便于插入新的相同的节点
	public FPTree(){
		root = new FPNode();
	}
	/**
	 * 在树上添加一天记录
	 * @param trans 交易记录
	 * @param count 交易记录的条数
	 */
	public void addTransaction(LinkedList<String> trans,int count){
		FPNode subRoot = root;
		root.support++;
		for(String s : trans){
			FPNode childs = subRoot.getChildByName(s);
			if(childs==null){
				FPNode temp = new FPNode();
				temp.name = s;//设置名称
				temp.support += count;//设置支持度
				temp.parent = subRoot;//设置父节点
				subRoot.child.add(temp);
				if(!firstNode.containsKey(s))firstNode.put(s, temp);//修改第一个节点
				if(lastHomoNode.containsKey(s))lastHomoNode.get(s).nodeLink = temp;//如果存在修改nodelink
				lastHomoNode.put(s, temp);//修改最后一个节点
				subRoot = temp;
			}
			else {
				childs.support += count;
				subRoot = childs;
			}
		}
	}
	/**
	 * 根据节点的名称获取条件模式基
	 * @param name 节点的名称
	 * @return
	 */
	public Map<LinkedList<String>,Integer> getPrefixPath(String name){
		Map<LinkedList<String>,Integer> prefixPath = new HashMap<LinkedList<String>,Integer>();
		FPNode subNode = firstNode.get(name);
		while(subNode!=null){
			FPNode parent = subNode.parent;
			LinkedList<String> path = new LinkedList<String>();
			while(parent.name!=null){
				path.addFirst(parent.name);
				parent = parent.parent;
			}
			prefixPath.put(path, subNode.support);
			subNode = subNode.nodeLink;
		}
		return prefixPath;
	}
	/**
	 * 判断是否为单支，返回true或false
	 * @return
	 */
	public boolean isSinglePath(){
		FPNode subRoot = root;
		while(subRoot.child.size() != 0){
			if(subRoot.child.size()>1) return true;
			subRoot = subRoot.child.get(0);
		}
		return false;
	}
	/**
	 * 创建header
	 */
	public void createHeader(){
		for(String s : frequency.keySet()){
			if(frequency.get(s)>=minSupport)header.add(s);
		}
		Collections.sort(header, new Comparator<String>(){
			public int compare(String arg0, String arg1) {
				// TODO Auto-generated method stub
				int result = frequency.get(arg1) - frequency.get(arg0);
				if(result == 0) return arg0.compareTo(arg1);
				else return result;
			}
		});
	}
	/**
	 * 根据trans数据统计支持度
	 * @param trans
	 */
	public void setFrequency(Map<LinkedList<String>,Integer>trans){
		
		for (Entry<LinkedList<String>, Integer> entry : trans.entrySet()) {
			int count = entry.getValue();
			for(String s:entry.getKey()){
				if(!frequency.containsKey(s))frequency.put(s, 0);
				frequency.put(s, frequency.get(s) + count);
			}
		}
	}
	/**
	 * 构建树
	 * 这里要注意的问题是：如果支持度大于最小支持度 则将数据添加到书上面
	 * @param trans
	 */
	public void buildTree(Map<LinkedList<String>,Integer>trans){
		for (Entry<LinkedList<String>, Integer> entry : trans.entrySet()) {
			LinkedList<String> temp = new LinkedList<String>();
			for(String s : entry.getKey())if(frequency.get(s)>=minSupport)temp.add(s);
			if(temp.isEmpty())continue;
			addTransaction(temp, entry.getValue());
		}
	}
}
