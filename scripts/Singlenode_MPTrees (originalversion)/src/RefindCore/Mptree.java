package RefindCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;

/**
 * Mptree
 * @author Lichshe
 *
 */
public class Mptree {

	public ArrayList<Node> child;//store all nodes except for root node
    public Node root;//root node
    public ArrayList<ArrayList<String>> Tu;//Chinese tweets hashtags
    public float[]score;//store id and cosine value of recommended item
    /**
     * populateTreeView
     * @param file
     * @throws Exception
     */
    public void PopulateTreeView(String file) throws Exception{
    	root = new Node("Root");
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
    	String sline = "";
    	child = new ArrayList<Node>();
        int count = 0;
        while ((sline = br.readLine())!= null){
        	String ss[] = sline.split(" ");
        	for(int i=0;i<ss.length-1;i++){
            /*build all nodes in the second level*****************************/
        		if(i==0){
        			if(count==0){//input first node except for root node     			
        				child.add(new Node(ss[i],0,root));
        			    root.ChildNodes=new ArrayList<Node>();
        				root.ChildNodes.add(child.get(count));
        				count++;
        			}
        			for (int j = 0; j < count; j++){
                         if ((child.get(j).Name.equals(ss[i])) && (child.get(j).ParentNode == root)){//check the existence of nodes on the second level
                             break;
                         }
                         if (j == count - 1){//add a new node when same node is not detected after traversal
                             child.add(new Node(ss[i],0,root));
                             root.ChildNodes.add(child.get(count));
                             count++;
                         }
                     }
        		}
        	/*finish node construction on the second level*******************************/
        		else{
        			 int j = count - 1;//pointer to traverse the array
                     int q = i - 1;//the element before current element
                     int current = 0;//current node
                     while (j >= 0 && (child.get(j).Name.equals(ss[i])==false)){//find the nodes with the same value as current element from the array
                         j--;
                         current = j;
                     }
                     while (j >= 0 && q >= 0){
                         if (child.get(j).ParentNode.Name.equals(ss[q])==false)//check whether the name of father node is the same as current element
                         break;
                         if (q == 0 && child.get(j).ParentNode.ParentNode != root)//check until the first node of current level to see whether the grandpa node is root node
                             break;
                         //j=Array.IndexOf(child, child[j].ParentNode);//return index of father node in the array
                         j = child.indexOf(child.get(j).ParentNode);
                         q--;
                     }
                     if (q < 0 && i != ss.length - 2) {//existing node
                     }
                     else if (q < 0 && i == ss.length - 2){//existing leaf node, change its value                  
                         child.get(current).Value = Float.parseFloat(ss[i+1]);//the last value of each line is cosine
                     }
                     else if (q >= 0){
                         int front = Integer.parseInt(ss[i-1]);
                         int father = count - 1;
                         while (father >= 0){//find the location to add a father node
                            if (child.get(father).Name.equals(ss[i - 1]))
                            break;
                             father--;
                         }
                         if (father >= 0 && i != ss.length-2){//add non-leaf node     
                             child.add(new Node(ss[i], 0, child.get(father)));
                             if(child.get(father).ChildNodes==null)
                            	 child.get(father).ChildNodes=new ArrayList<Node>();
                             child.get(father).ChildNodes.add(child.get(count));
                             count++;
                         }
                         else if (father >= 0 && i == ss.length - 2){//add leaf node
                             child.add(new Node(ss[i],Float.parseFloat(ss[i+1]),child.get(father)));
                             if(child.get(father).ChildNodes==null)
                            	 child.get(father).ChildNodes=new ArrayList<Node>();
                             child.get(father).ChildNodes.add(child.get(count));
                             count++;
                         }
             		}
        		}
        		
        	}
        }
    br.close();
    fr.close();
    }
    /**
     * ReadTU
     * @param file
     * @throws Exception
     */
    public void ReadTU(String file) throws Exception{//read data, e.g., hashtags
    	Tu = new ArrayList<ArrayList<String>>();
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
    	String sline = "";
    	while ((sline = br.readLine())!= null){
    		String filerow[] = sline.split(" ");
 		   ArrayList<String> rowList=new ArrayList<String>();
 		   for(String s:filerow)
 			   {rowList.add(s);}
 		   Tu.add(rowList);
    	 }	
    }
    /**
     * Recore
     * @param file
     * @param maxnum
     * @param k
     * @throws Exception
     */
    public void Recore(String file,int maxnum,int k) throws Exception{
    	FileWriter fw=new FileWriter(file);
   	    BufferedWriter bw=new BufferedWriter(fw);	
    	for(int i=0;i<Tu.size();i++){
    		    bw.write("user: ");
       	    	bw.write(String.valueOf(i+1));
       	    	bw.write(" ");
       	    	bw.write("item: ");
    		score=new float[maxnum];//initialize array of values
    		Stack<Node> s = new Stack<Node>();
    		ArrayList<Node>rootchild = root.ChildNodes;//all nodes in the second level
    		int iut = 0;//initialize ids of recommended item
    		for(Node second :rootchild){
    			s.push(second);
    			while(s.size()!=0){
    				Node cur = s.pop();
    				iut = visit(cur, Tu.get(i),iut);
    				if (cur.Visit < 2 && cur.ChildNodes!=null){
    					for(Node curchild:cur.ChildNodes){
    						curchild.Visit=cur.Visit;//transfer the value of visit from father node to child node
    						s.push(curchild);
    					}
    				}
    				cur.Visit=0;//reset value of visit to 0 after traversal or transfer the value to child node
    			}
    		}
    	    int lenj=score.length;
    	    Map map=new HashMap<Integer,Float>();
    	    for(int ms=0;ms<lenj-1;ms++){
   	    		map.put(ms,score[ms]);
   	    	}
    	    
    	    List<Map.Entry<Integer,Float>> infoIds =new ArrayList<Map.Entry<Integer,Float>>(map.entrySet());
   	    	Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Float>>(){//ranking
   	    		public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {      
   	    	         return o2.getValue().compareTo(o1.getValue());        
   	    	}
   	    	});
   	 	    for(int j=0;j<k;j++){
   	 	    	Map.Entry<Integer, Float> mapsort=infoIds.get(j);
   	 	    	int key=mapsort.getKey();
   	 	    	float value=mapsort.getValue();
   	 	    		bw.write(String.valueOf(key));
   	 	    		bw.write("(");
   	 	    		bw.write(String.format("%.3f",value));
   	 	    		bw.write(")");
   	 	    		bw.write(" ");	
   	       }
   	 	   bw.newLine();
    	}
    	bw.close();
   	    fw.close();	
	 }
    /**
     * vist
     * @param cur
     * @param tu
     * @param iutold
     * @return
     */
    public int visit(Node cur,ArrayList<String> tu, int iutold){
    	if(tu.contains(cur.Name)&&cur.Visit==1){
    		score[iutold]+=cur.Value;
    	}
    	else if(tu.contains(cur.Name)==false){
    		cur.Visit+=1;
    		if(cur.Visit==1){
    			int iut=Integer.parseInt(cur.Name);//convert characters to whole tree
    			score[iut]+=cur.Value;
    			return iut;//new id of recommended item
    		}
    	}
    	return iutold;//previous id of recommended item
    }
    public int deepth(Node cur){
    	if(cur.ChildNodes == null)
    		return 1;
    	else
    	{
    		List allChildDeepth = new ArrayList();
    		for(Node t: cur.ChildNodes)
    		{
    			allChildDeepth.add(deepth(t));
    		}
    		Collections.sort(allChildDeepth,Collections.reverseOrder());
    		return (Integer)allChildDeepth.get(0) + 1;
    	}
    }
   /**
    * Ö÷º¯Êý 
    * @param args
    * @throws Exception
    */
    public static void main(String[] args) throws Exception {
    	Scanner sc = new Scanner(System.in);
    	long t1=System.currentTimeMillis();
    	Mptree m=new Mptree();
		m.PopulateTreeView("C:\\Users\\Lichshe\\Desktop\\wyq\\ptn0C0.2S0.5.txt");
		long t=System.currentTimeMillis();
		System.out.println("Tims:"+(t-t1)/1000+"s");
		//find the number of nodes in the tree
		int total = m.child.size();
		System.out.println("Number of node£º" + total);
		//number of leaves
		int leaf = 0;
		for(Node no: m.child)
		{
			if(no.ChildNodes == null)
				leaf++;
		}
		System.out.println("Number of leaf node£º" + leaf);
		//branching node£¬
		System.out.println("Number of branches£º" + (total - leaf));
		//number of subtrees of root node
		System.out.println("Number of sub trees£º" + m.root.ChildNodes.size());
		//depth of tree
		System.out.println("Depth of the tree£º" + (m.deepth(m.root)-1));
		while(true)
		{
			System.out.println("please input maxnum and top k:");
	    	int maxnum=sc.nextInt();
	    	int k=sc.nextInt();
			System.out.println("please input file name");
			String train = sc.next();
			String result = sc.next();
			long t2=System.currentTimeMillis();
			m.ReadTU("C:\\Users\\Lichshe\\Desktop\\wyq\\" + train + ".txt");
			long t3=System.currentTimeMillis();
			m.Recore("C:\\Users\\Lichshe\\Desktop\\wyq\\" + result + ".txt",maxnum,k);
			long t4=System.currentTimeMillis();
			System.out.println("Finish ");
			System.out.println("Tims:"+(t3-t2)/1000+"s");
			System.out.println("Tims:"+(t4-t3)/1000+"s");
		}
	}

}
