package RefindCore;

import java.util.ArrayList;



public class Node {

	public String Name;//id of recommended item
	public float Value;//cos values of recommended item
	public int Visit;//number of visit of recommended item
	public Node ParentNode;//father node
	public ArrayList<Node> ChildNodes;
	/**
	 * build empty node
	 */
	public Node()
    {
    }
	/**
	 * build id of initialized item and father node
	 * @param text
	 * @param parent
	 */
    public Node(String text,Node parent)
    {
        this.Name = text;
        this.ParentNode = parent;
        this.Visit = 0;
    }
    /**
     * build node for initialized item with 0 visit
     * @param text
     */
    public Node(String text)
    {
        this.Name = text;
        this.Visit = 0;
       
    }
    /**
     * build node for initialized item with cosine value b and 0 visit
     * @param a
     * @param b
     */
    public Node(String a, float b)
    {
        this.Name = a;
        this.Value = b;
        this.Visit = 0;
    }
    /**
     * build node for initialized item with cosine value b, 0 visits and father node as parent
     * @param a
     * @param b
     * @param parent
     */
    public Node(String a, float b,Node parent)
    {
        this.Name = a;
        this.Value = b;
        this.ParentNode = parent;
        this.Visit = 0;
    }
  
   

}
