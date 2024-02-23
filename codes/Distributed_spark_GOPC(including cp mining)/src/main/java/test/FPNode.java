package test;

import java.util.ArrayList;
import java.util.List;

public class FPNode {
	public String name = null;//名称
	public int support = 0;//支持度
	public FPNode parent = null;//父亲节点
	public List<FPNode> child = new ArrayList<FPNode>();//孩子节点
	public FPNode nodeLink = null;//链接下一个相同的节点
	public FPNode(){//空构造器
	}
	public FPNode getChildByName(String name){//根据名称获取孩子节点
		for(FPNode s : child){
			if(s.name.equals(name)){
				return s;
			}
		}
		return null;
	}
}
