package test;

import java.util.ArrayList;
import java.util.List;

public class FPNode {
	public String name = null;//����
	public int support = 0;//֧�ֶ�
	public FPNode parent = null;//���׽ڵ�
	public List<FPNode> child = new ArrayList<FPNode>();//���ӽڵ�
	public FPNode nodeLink = null;//������һ����ͬ�Ľڵ�
	public FPNode(){//�չ�����
	}
	public FPNode getChildByName(String name){//�������ƻ�ȡ���ӽڵ�
		for(FPNode s : child){
			if(s.name.equals(name)){
				return s;
			}
		}
		return null;
	}
}
