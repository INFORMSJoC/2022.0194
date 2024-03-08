package third;

import java.util.ArrayList;



public class Node {

	public String item_name;//推荐item编号
	public float parent_stat;//推荐的cos值
	public int color;//推荐项的访问次数
	public Node parent;//父节点
	public ArrayList<Node> child_list;
	public Node()
    {
    }
    public Node(String text)
    {
        this.item_name = text;
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
