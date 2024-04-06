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
	 * ����·�����
	 * @param residualPath
	 * @param results
	 */
	public static void combine(LinkedList<FPNode> residualPath, List<List<FPNode>> results) {
        if (residualPath.size() > 0) {
            FPNode head = residualPath.poll();//ȡ��֧�ֶ�С�ġ�λ�ڿ�ͷ��Ԫ��
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
	 * FPGrowth�㷨
	 * @param tree
	 * @param postModel
	 */
	public static void FPGrowth(FPTree tree,LinkedList<String> postModel){
		if(!tree.isSinglePath()){
			LinkedList<FPNode> path = new LinkedList<FPNode>();
            FPNode currNode = tree.root;
            while (currNode.child.size()!=0) {
                currNode = currNode.child.get(0);
                path.addFirst(currNode);//�����ӽڵ���뵽·����ǰ��   ��֧�ֶ�����44
            }
           List<List<FPNode>> results = new ArrayList<List<FPNode>>();
           combine(path, results);
           for (List<FPNode> list : results) {
        	   int cnt = 0;
        	   List<String> rule = new ArrayList<String>();
        	   if (postModel != null)rule.addAll(postModel);
        	   for (FPNode node : list) rule.add(node.name);
        	   if(list.size()!=0)cnt = list.get(0).support;//cnt��FPTreeҶ�ڵ�ļ���   ��ͷ��֧�ֶ���С��
        	   frequentMap.put(rule, cnt);
        	   }
           return;
           }
		for(int i = tree.header.size()-1;i>=0;i--){
			List<String> rule = new ArrayList<String>();
			String item_name = tree.header.get(i);//ͷ���Ԫ��  ֧�ֶȴ�
			 if (postModel != null) {
	                rule.addAll(postModel);
	            }
			 rule.add(item_name);//֧�ֶȴ�ķ������
			frequentMap.put(rule, tree.frequency.get(item_name));
			//tree.Flist.get(item_name)
			LinkedList<String> newPostPattern = new LinkedList<String>();
            if (postModel != null) {
                newPostPattern.addAll(postModel);
            }
            newPostPattern.add(item_name);//֧�ֶȴ�ķ��ں���
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

