package stand_alone_with_tree;


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

public class GOPC_Tree {
    public Node root;//root�ڵ�
    public ArrayList<ArrayList<String>> Tu;//�����������
    public Map<String,Float>score;//�洢�Ƽ���item�ı����ֵ
    public int maxlength;
    /**
     * ������
     * @param file
     * @throws Exception
     */
    public void PopulateTreeView(String file) throws Exception{
    	root = new Node("Root");
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
    	String sline = "";
        int count = 0;
        Node nownode = new Node();
        while ((sline = br.readLine())!= null){
        	String ss[] = sline.split(" ");
        	if(ss.length>maxlength)maxlength=ss.length;
        	for(int i=0;i<ss.length-1;i++){
        		if(i==0){
        			if(count ==0){
        				root.child_list = new ArrayList<Node>();
        				Node u = new Node(ss[i],0,root);
        				u.level=i;
        				root.child_list.add(u);
        				count++;
        			}
        			else 
        			{
        				if(!root.child_list.get(root.child_list.size()-1).item_name.equals(ss[i])){
        					Node u = new Node(ss[i],0,root);
        					u.level=i;
        					root.child_list.add(u);
        				}
        			}
        			nownode = root.child_list.get(root.child_list.size()-1);
        		}
        		else{
        			if(nownode.child_list ==null){
        				nownode.child_list = new ArrayList<Node>();
        				Node u = new Node(ss[i],0,nownode);
        				u.level=i;
        				nownode.child_list.add(u);
        			}
        			else{
        				int listLength_1 = nownode.child_list.size();//��ǰ�ڵ㺢�ӽڵ�ĸ���
        				if(!nownode.child_list.get(listLength_1-1).item_name.equals(ss[i])){
        					Node u = new Node(ss[i],0,nownode);
        					u.level=i;
        					nownode.child_list.add(u);
        					}
        			}
        			int listlength = nownode.child_list.size();
        			if(i==ss.length-2){
        				nownode.child_list.get(listlength-1).parent_stat += Float.parseFloat(ss[ss.length-1]);
        				nownode = null;
        				break;
        			}
        			nownode = nownode.child_list.get(listlength-1);
        		}		
        	}
        }
    br.close();
    fr.close();
    maxlength--;
    }
    /**
     * ��ȡ��������
     * @param file
     * @throws Exception
     */
	public void ReadTU(String file) throws IOException{
    	Tu = new ArrayList<ArrayList<String>>();
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
    	String sline = "";
    	while ((sline = br.readLine())!= null){
    		if(!sline.equals("")){
    		String filerow[] = sline.split(" ");
 		   ArrayList<String> rowList=new ArrayList<String>();
 		   for(String s:filerow)
 			   {rowList.add(s);}
 		   Tu.add(rowList);
    	 }
    	}
    }
    /**
     * ����·��
     * @param file
     * @param maxnum
     * @param k
     * @throws Exception
     */
    public void SearchPath(String file,int windowsize) throws Exception{
    	FileWriter fw=new FileWriter(file);
   	    BufferedWriter bw=new BufferedWriter(fw);	
    	for(int i=0;i<Tu.size();i++){
    		    bw.write("user: ");
       	    	bw.write(String.valueOf(i+1));
       	    	bw.write(" ");
       	    	bw.write("item: ");
    		score=new HashMap<String,Float>();//valueֵ�����ʼ��
    		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
    		String iut ="";//�Ƽ���item�ı�ų�ʼ��
    		ArrayList<String> tu = Tu.get(i);//��ȡ�������û�session
    		ArrayList<String> u_tu;//�����û�����session
    		/**�趨����**/
			if(tu.size()>=windowsize) u_tu= new ArrayList<String>(tu.subList(0, windowsize));
			else u_tu= new ArrayList<String>(tu);
    		for(int k=u_tu.size();k>0;k--){//����˥��
    			if(!score.isEmpty())break;//���Ƽ���  ֹͣ����
    			List<String> sub_string = new ArrayList<String>(u_tu.subList(0, k));//��ǰ��С��session
    			Collections.sort(sub_string);//����
    			String ComStr = sub_string.get(0);//���ʵ���С�Ľڵ�
				for(Node second :rootchild){
	    			int re = ComStr.compareTo(second.item_name);//��ȡ��ǰ�ڵ���Tu�Ĳ��
	    			if(re > 0){//������ʱ���Ƽ����ǵ�һ����Ҫ�жϺ���Ľڵ��Ƿ�͵�ǰ�ڵ��Ƿ�һ��
	    				if(u_tu.contains(second.item_name))continue;
	    				String recomname = second.item_name;//�Ƽ���Ŀ
	    				Node nownode=second;//ָ��ڵ�
	    				Node former = null;//�����ݴ��ҵ��Ľڵ�
	    				for(int t=0;t<sub_string.size();t++){
	    					if(nownode.child_list!=null){//������ӽڵ㲻Ϊ��
	    						for(Node n:nownode.child_list){//������ǰ�ڵ�����к��ӽڵ�
		    						if(n.item_name.equals(sub_string.get(t))){//����ҵ���ֵ������ֹ����
		    							former = n;
		    							break;
		    						}
		    					}
		    					if(former == null)break;//�ж���û���ҵ������û�ҵ������˳���ǰ����������
		    					else nownode = former;//����ҵ���������±Ƚ�
	    					}
	    					else break;
	    				}
	    				/**
	    				 * 1.û�ҵ����ӽڵ�  ���ǳ��Ȳ�һ��
	    				 * 2�����ӽڵ�Ϊ��ʱ����ʱ���ǳ��Ȳ�һ��
	    				 * 3����������   ���ǱȽϽ�����
	    				 * 
	    				 *  �ж�nownode��level��cur_tu��size�ıȽ�
	    				 */
	    				if(nownode.level==sub_string.size()){
	    					score.put(recomname, score.getOrDefault(recomname, nownode.parent_stat));
	    				}
	    			}
	    			else if(re==0){
	    				Stack<Node> s = new Stack<Node>();
	    				s.push(second);
	    				while(s.size()!=0){
	    					Node cur = s.pop();
	    					iut = Pattern_Gen(cur,sub_string,u_tu,iut);
	    					if (cur.color < 2 && cur.child_list!=null){
	    						for(Node curchild:cur.child_list){
	    							curchild.color=cur.color;//���ڵ��colorֵ�����ӽڵ�
	    							s.push(curchild);
	    						}
	    					}
	    					cur.color=0;//�����ʹ����ߴ��ݸ�����colorֵ�Ľڵ��colorֵ����
	    				}
	    			}
	    			else break;
				}
    		}
			List<Map.Entry<String, Float>> infoIds = new ArrayList<Map.Entry<String,Float>>(score.entrySet());
    		Collections.sort(infoIds, new Comparator<Map.Entry<String, Float>>(){

				@Override
				public int compare(Entry<String, Float> arg0, Entry<String, Float> arg1) {
					// TODO Auto-generated method stub
					return arg0.getKey().compareTo(arg1.getKey());
				}
    		});
    		for(int j=0;j<infoIds.size();j++){
    			float value=infoIds.get(j).getValue();
	 	    	if(value!=0.0){
	 	    	String key=infoIds.get(j).getKey();
	 	    		bw.write(String.valueOf(key));
	 	    		bw.write("(");
	 	    		bw.write(String.format("%.3f",value));
	 	    		bw.write(")");
	 	    		bw.write(" ");
	 	    	}
    		}
   	 	   bw.newLine();
    	}
    	bw.close();
   	    fw.close();	
	 }
    public String Pattern_Gen(Node cur,List<String> sub_string, List<String> u_tu,String iutold){
    	boolean s = sub_string.contains(cur.item_name);
    	if(s==true&&cur.color==1){
    		if(cur.level==sub_string.size()&&!u_tu.contains(iutold))score.put(iutold, score.getOrDefault(iutold,(float) 0)+cur.parent_stat);
    	}
    	if(s==false){//�����������ǰ�ڵ�Ļ�
    		cur.color+=1;//�ȶԵ�ǰ�ڵ��colorֵ�����޸�
    		if(cur.color==1){//˵���ҵ����Ƽ�����Ŀ
    			String iut=cur.item_name;
    			if(cur.level==sub_string.size()&&!u_tu.contains(iut)){
    				score.put(iut, score.getOrDefault(iut, (float) 0)+cur.parent_stat);
    			}
    			return iut;//�µ��Ƽ���item���
    		}
    	}
    	return iutold;//�ɵ��Ƽ���item���
    }
    /**
     * ��������ÿ�α���ԭ����ǰ������Ĳ���
     * �����ֵ�{����:����}
     * @param a
     * @return
     */
    public static Map<Integer,List<String>> insertSort(List<String> a) {
        int i, j;
        String insertNote;// Ҫ���������
        Map<Integer,List<String>> bis = new HashMap<Integer,List<String>>();
        bis.put(1,new ArrayList<String>(a.subList(0, 1)));
        for (i = 1; i < a.size(); i++) {// ������ĵڶ���Ԫ�ؿ�ʼѭ���������е�Ԫ�ز���
            insertNote = a.get(i);// ���������еĵ�2��Ԫ��Ϊ��һ��ѭ��Ҫ���������
            j = i - 1;
            while (j >= 0 && insertNote.compareTo(a.get(j))<0) {
                a.set(j+1, a.get(j));// ���Ҫ�����Ԫ��С�ڵ�j��Ԫ��,�ͽ���j��Ԫ������ƶ�
                j--;
            }
            a.set(j+1, insertNote) ;// ֱ��Ҫ�����Ԫ�ز�С�ڵ�j��Ԫ��,��insertNote���뵽������
            bis.put(i+1, new ArrayList<String>(a.subList(0, i+1)));
        }
        return bis;
    }
    /**
     * �ں��ӽڵ��У��ö��ֲ����������������Ľڵ�
     * @param a
     * @param s
     * @return
     */
    public int bisearch(ArrayList<Node> a,String s){
    	int low = 0;
    	int high = a.size()-1;
    	int mid = (low+high)/2;
    	while(low<=high){
    		int result = a.get(mid).item_name.compareTo(s);
    		if(result==0)return mid;
    		else if(result>0)high = mid-1;
    		else low = mid+1;
    	}
    	return -1;
    }
    /**
     * ���������
     * @param cur
     * @return
     */
    public int deepth(Node cur){
    	if(cur.child_list == null)
    		return 1;
    	else
    	{
    		List allChildDeepth = new ArrayList();
    		for(Node t: cur.child_list)
    		{
    			allChildDeepth.add(deepth(t));
    		}
    		Collections.sort(allChildDeepth);
    		return (int)allChildDeepth.get(allChildDeepth.size()-1) + 1;
    	}
    }
    /**
     * ������pattern����
     * @return
     */
    public int pattern_count(){
    	int count = 0;
    	Stack<Node> s = new Stack<Node>();
		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
		for(Node second :rootchild){
			s.push(second);
			while(s.size()!=0){
				Node cur = s.pop();
				if (cur.parent_stat!=0) count++;
				if(cur.child_list!=null)
				{
					for(Node curChild:cur.child_list)
					{
						s.push(curChild);
					}
				}
				}
			}
		return count;
    }
    /**
     * ��Ҷ�ӽڵ���
     * @return
     */
    public  int leaf_node()
    {
//    	if(s ==null)return 0;
//    	if(s.child_list==null)return 1;
//    	int sum = 0;
//    	for(Node n : s.child_list)sum += leaf_node(n);
//    	return sum;
    	int count = 0;
    	Stack<Node> s = new Stack<Node>();
		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
		for(Node second :rootchild){
			s.push(second);
			while(s.size()!=0){
				Node cur = s.pop();
				if (cur.child_list==null) count++;
				else
				{
					for(Node curChild:cur.child_list)
					{
						s.push(curChild);
					}
				}
				}
			}
		return count;
    }
    /**
     * ���ܽڵ���
     * @return
     */
    public  int total_node()
    {
//    	if(s==null)return 0;
//    	if(s.child_list==null)return 1;
//    	int sum = 1;
//    	for(Node n : s.child_list) sum+=total_node(n);
//    	return sum;
    	int count = 0;
    	Stack<Node> s = new Stack<Node>();
		ArrayList<Node>rootchild = root.child_list;//���еڶ���ڵ�
		for(Node second :rootchild){
			s.push(second);
			while(s.size()!=0){
				Node cur = s.pop();
				count++;
				if(cur.child_list!=null)
				{
					for(Node curChild:cur.child_list)
					{
						s.push(curChild);
					}
				}
				}
			}
		return count;
    }
    /**
     * ��ӡ��������·��
     * @return
     */
    public List<String> print_tree()
	{
		List<String> result = new ArrayList<String>();
		if(root ==null) return result;
		helper(root,root.item_name,result);
		return result;
	}
	public void helper(Node rootnode, String path,List<String>result){
		if(rootnode ==null)return;
		if(rootnode.child_list==null)
			{
			result.add(path);
			return;
			}
		for(Node n :rootnode.child_list)
		{
			helper(n,path+"->"+n.item_name,result);
		}
	}
    public static void main(String[] args) throws Exception {
    	 if(args.length!=4){
   		  System.out.println("Usage: java -jar xx.jar patternFile userFile resultFile windowsize");
   		  System.exit(0);
   	  }
//    	Scanner sc = new Scanner(System.in);
   	 Runtime.getRuntime().runFinalization();
   	 Runtime.getRuntime().gc();
   	 long start = Runtime.getRuntime().totalMemory();
   	 long start_free = Runtime.getRuntime().freeMemory();
    	GOPC_Tree m=new GOPC_Tree();
    	long t1=System.currentTimeMillis();
    	m.PopulateTreeView(args[0]);
//		m.PopulateTreeView("E:\\result\\out_acc_18.txt");
//		m.PopulateTreeView("C:\\Users\\Lichshe\\Desktop\\testData.txt");
		long t=System.currentTimeMillis();
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().gc();
		long end = Runtime.getRuntime().totalMemory();
		long end_free = Runtime.getRuntime().freeMemory();
		System.out.println("Memory Comsume:" + (end-end_free-start+start_free)+"Byte");
//		System.out.println("Times:"+(t-t1)+"ms");
//		//�����Ľڵ�����
//		int total = m.total_node();
//		System.out.println("�ڵ����Ϊ��" + total);
//		//������Ҷ�ӽڵ�
//		int leaf = m.leaf_node();
//		System.out.println("Ҷ�ӽڵ����Ϊ��" + leaf);
//		//��֦�ڵ㣬
//		System.out.println("��֧�ڵ����Ϊ��" + (total - leaf));
//		//root������������
//		System.out.println("root����������(��ɭ�����Ǹ����ĸ���)��" + m.root.child_list.size());
//		//�������
//		System.out.println("�������Ϊ��" + (m.deepth(m.root)-1));
//		System.out.println("pattern����Ϊ�� " +  m.pattern_count());
//			System.out.println("please input maxnum and top k:");
//	    	int maxnum=sc.nextInt();
//	    	int k=sc.nextInt();
			long t2=System.currentTimeMillis();
			m.ReadTU(args[1]);
//			m.ReadTU("E:\\result\\accidents.dat");
//			m.ReadTU("C:\\Users\\Lichshe\\Desktop\\tu.txt");
			long t3=System.currentTimeMillis();
			m.SearchPath(args[2],Integer.parseInt(args[3]));
//			m.SearchPath("E:\\result\\tutuR.txt",5);
//			m.SearchPath("C:\\Users\\Lichshe\\Desktop\\rr.txt",5);
			long t4=System.currentTimeMillis();
//			System.out.println("Finish ");
//			System.out.println("Times:"+(t3-t2)+"ms");
//			System.out.println("Times:"+(t4-t3)+"ms");
	   	    System.out.println("Build Tree Times:"+(t-t1)+"ms");
			//�����Ľڵ�����
			int total = m.total_node();
			System.out.println("Node Count��" + total);
			//������Ҷ�ӽڵ�
			int leaf = m.leaf_node();
			System.out.println("Leaf Node Count��" + leaf);
			//��֦�ڵ㣬
			System.out.println("Branch Node Count��" + (total - leaf));
			//root������������
			System.out.println("Root's Children Count��" + m.root.child_list.size());
			//�������
			System.out.println("Deepth Of Tree��" + (m.deepth(m.root)-1));
			System.out.println("Pattern Count�� " +  m.pattern_count());
			System.out.println("Finish ");
			System.out.println("Read TU Times:"+(t3-t2)+"ms");
			System.out.println("Recore Times:"+(t4-t3)+"ms");
	}
}
