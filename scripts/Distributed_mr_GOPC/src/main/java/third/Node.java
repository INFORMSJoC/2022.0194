package third;

import java.util.ArrayList;



public class Node {

	public String item_name;//�Ƽ�item���
	public float parent_stat;//�Ƽ���cosֵ
	public int color;//�Ƽ���ķ��ʴ���
	public Node parent;//���ڵ�
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
