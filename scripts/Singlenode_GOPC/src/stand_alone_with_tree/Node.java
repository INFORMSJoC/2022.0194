package stand_alone_with_tree;

import java.util.ArrayList;



public class Node {

	public String item_name;//推荐item编号
	public float parent_stat;//推荐的值
	public int color;//推荐项的访问次数
	public Node parent;//父节点
	public ArrayList<Node> child_list;
	public int level;
	public Node()
    {
    }
    public Node(String text,Node parent)
    {
        this.item_name = text;
        this.parent = parent;
        this.color = 0;
    }
    public Node(String text)
    {
        this.item_name = text;
        this.color = 0;
        this.level = -1;
    }
    public Node(String a, float b)
    {
        this.item_name = a;
        this.parent_stat = b;
        this.color = 0;
    }
    public Node(String a, float b,Node parent)
    {
        this.item_name = a;
        this.parent_stat = b;
        this.parent = parent;
        this.color = 0;
    }
  
   

}
