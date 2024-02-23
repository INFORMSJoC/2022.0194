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
	public FPNode root = null;//���ڵ�
	public int minSupport;
	public List<String> header = new ArrayList<String>();//ͷ��
	public Map<String,Integer> frequency = new HashMap<String,Integer>();//������ʾ��Ŀ��֧�ֶ�
	public Map<String,FPNode> firstNode = new HashMap<String,FPNode>();//ָ��ÿһ���ڵ���ڸ���ͷ��ݹ��ھ���
	public Map<String,FPNode> lastHomoNode = new HashMap<String,FPNode>();//ָ�����������һ����ͬ�Ľڵ���ڲ����µ���ͬ�Ľڵ�
	public FPTree(){
		root = new FPNode();
	}
	/**
	 * ���������һ���¼
	 * @param trans ���׼�¼
	 * @param count ���׼�¼������
	 */
	public void addTransaction(LinkedList<String> trans,int count){
		FPNode subRoot = root;
		root.support++;
		for(String s : trans){
			FPNode childs = subRoot.getChildByName(s);
			if(childs==null){
				FPNode temp = new FPNode();
				temp.name = s;//��������
				temp.support += count;//����֧�ֶ�
				temp.parent = subRoot;//���ø��ڵ�
				subRoot.child.add(temp);
				if(!firstNode.containsKey(s))firstNode.put(s, temp);//�޸ĵ�һ���ڵ�
				if(lastHomoNode.containsKey(s))lastHomoNode.get(s).nodeLink = temp;//��������޸�nodelink
				lastHomoNode.put(s, temp);//�޸����һ���ڵ�
				subRoot = temp;
			}
			else {
				childs.support += count;
				subRoot = childs;
			}
		}
	}
	/**
	 * ���ݽڵ�����ƻ�ȡ����ģʽ��
	 * @param name �ڵ������
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
	 * �ж��Ƿ�Ϊ��֧������true��false
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
	 * ����header
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
	 * ����trans����ͳ��֧�ֶ�
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
	 * ������
	 * ����Ҫע��������ǣ����֧�ֶȴ�����С֧�ֶ� ��������ӵ�������
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
