package third;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;




public class GOPC_Tree_Main {
	
	public static class MP_tree_Mapper_First extends Mapper<LongWritable, Text, IntWritable, Text>{
		private IntWritable one = new IntWritable();
	    private Text word = new Text();
	/**
	 * ����  �к� + �����ݣ����ڵ��ļ���� + ptn��
	 * ���  �����ļ���� + ptn
	 */
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	      StringTokenizer itr = new StringTokenizer(value.toString()," ");
	      one.set(Integer.parseInt(itr.nextToken()));
	      word.set(value.toString().split(" ", 2)[1]);
	      //�����ļ� + ptn
	      context.write(one,word);
	    }
	}
	
	
	public static class MP_tree_Partitioner_First extends Partitioner<IntWritable,Text>{
		/**
		 * ��key/value�ַ�����ͬ��reduce
		 */
		@Override
		public int getPartition(IntWritable key, Text value, int numPartitions) {
			// TODO Auto-generated method stub
			int n = key.get();
			return n-1;
		}
	}
	
	/**
	 * ���� �ļ���� + ptn
	 * 
	 * @author Lichshe
	 *
	 */
	public static class MP_tree_Reducer_First extends Reducer<IntWritable,Text,IntWritable,Text>
	{
		public GOPC_Tree m;
		@Override
		protected void setup(Context context)throws IOException, InterruptedException
		{   m = new GOPC_Tree();
			m.root = new Node("Root");
		}
		/**
		 * ������
		 * ����Key ֻ��һ��ֵ
		 */
	    public void reduce(IntWritable key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
	    	System.out.println(key.get());
	    	context.write(new IntWritable(key.get()), new Text(" "));
	    	long t = System.currentTimeMillis();
	    	System.out.println(t);
	    	PopulateTreeView(values);
	    	long t1 = System.currentTimeMillis();
	    	System.out.println(t1);
	    	System.out.println(t1-t);	    	
	    }
	    
	    public void PopulateTreeView(Iterable<Text> values)
	    {
	    	int count = 0;
	    	String sline = "";
	    	Node nownode = new Node();
	    	for (Text val : values) {
	    		sline = val.toString();
	    		String ss[] = sline.split(" ");
	    		
	        	for(int i=0;i<ss.length-1;i++){
	        		if(i==0){
	        			if(count ==0){
	        				m.root.child_list = new ArrayList<Node>();
	        				Node u = new Node(ss[i],0,m.root);
	        				m.root.child_list.add(u);
	        				count++;
	        			}
	        			else 
	        			{
	        				if(!m.root.child_list.get(m.root.child_list.size()-1).item_name.equals(ss[i])){
	        					Node u = new Node(ss[i],0,m.root);
	        					m.root.child_list.add(u);
	        				}
	        			}
	        			nownode = m.root.child_list.get(m.root.child_list.size()-1);
	        		}
	        		else{
	        			if(nownode.child_list ==null){
	        				nownode.child_list = new ArrayList<Node>();
	        				Node u = new Node(ss[i],0,nownode);
	        				nownode.child_list.add(u);
	        			}
	        			else{
	        				int listLength_1 = nownode.child_list.size();//��ǰ�ڵ㺢�ӽڵ�ĸ���
	        				if(!nownode.child_list.get(listLength_1-1).item_name.equals(ss[i])){
	        					Node u = new Node(ss[i],0,nownode);
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
	    }
	    /**
	     * ����ʶ��
	     */
	    @Override
	    protected void cleanup(Context context)throws IOException, InterruptedException
	    {
	    	long t3 = System.currentTimeMillis();
	    	System.out.println(t3);
	    	int maxnum = Integer.parseInt(context.getConfiguration().get("maxnum"));
	    	String TransFilename = context.getConfiguration().get("TransFilename");
	    	recore(10,maxnum,TransFilename,context);
	    	long t4 = System.currentTimeMillis();
	    	System.out.println(t4);
	    	System.out.println(t4-t3);
	    }

	   

	    public void recore(int k,int maxnum,String filepath,Context context) throws IOException, InterruptedException
	    {
	    	Configuration conf = context.getConfiguration();
			FileSystem hdfs = FileSystem.get(conf);
			Path pt = new Path(filepath);
			FSDataInputStream inn = hdfs.open(pt); 
			BufferedReader in = new BufferedReader(new InputStreamReader(inn,"UTF-8"));
			int count = 1;
			String line = "";
			while((line=in.readLine())!=null)
			{
				ArrayList<String> rowList=new ArrayList<String>();
		 	    for(String s:line.split(" "))
		 	    {
		 	    	rowList.add(s);
		 	    }
	    		m.score=new float[maxnum];//valueֵ�����ʼ������ÿ���û��Ƽ�����Ŀ��score��ÿ�ζ�Ҫ��ʼ�������������Ϊ��֪��Ԫ�ض��ٸ������������ȡ������Ŀ�����ֵ
	    		//����Ϊʲô�����ֵ���  ��֪��ʲôԭ�����Ǹ�ȫ�ֱ���
	    		Stack<Node> s = new Stack<Node>();//ջ  ������������
	    		ArrayList<Node> rootchild = m.root.child_list;//��ȡ���еڶ���ڵ�
	    		int iut = 0;//�Ƽ���item�ı�ų�ʼ��   ��������������Ƽ�����Ŀ   Ҫ���� Ȼ�����÷�
	    		for(Node second :rootchild){//foreach������ÿ������ڵ㣬��ȡ��ǰ�û��������Ƽ�
	    			s.push(second);//ѹջ
	    			while(s.size()!=0){//ջ��Ϊ��  �ͼ�������
	    				Node cur = s.pop();//��ջ
	    				iut = visit(cur,rowList ,iut);//���ʣ���ȡ���Ƽ�����Ŀ���Ͷ�score���м���
	    				if (cur.color < 2 && cur.child_list!=null){//�������С�ڶ��һ����ں��ӽڵ�
	    					//����null˵������ǰptn��ȫƥ��  ���Ƽ�
	    					//ptn��һ����ƥ��  ������ȫƥ��
	    					for(Node curchild:cur.child_list){
	    						curchild.color=cur.color;//���ڵ��visitֵ�����ӽڵ�
	    						s.push(curchild);//��ǰ�ڵ�����к��ӽڵ�ѹջ
	    					}
	    				}
	    				cur.color=0;//�����ʹ����ߴ��ݸ�����visitֵ�Ľڵ��visitֵ����
	    			}
	    		}
	    	    int lenj=m.score.length;
	    	    String sss = "";
	    	    for(int ms=0;ms<lenj-1;ms++){
	    	    	
	    	    	if(m.score[ms]!=0)
	    	    	{
	    	    		sss += String.valueOf(ms)+ ":" + m.score[ms] +":" + " ";	
	    	    	}
	   	    	}
	   	 	    context.write(new IntWritable(count),new Text(sss));
	   	 	    count++;
	   	 	    context.progress();
			}
			in.close();
			inn.close();
	    }

	    public int visit(Node cur,ArrayList<String> tu, int iutold){//ÿ�η��ش��Ƽ�����Ŀ�ı��
	    	if(tu.contains(cur.item_name)&&cur.color==1){//������visit==1˵����֮ǰ����δƥ�����Ŀ��
	    		//��ʱ��cosֵҪ���׸�֮ǰδƥ�����Ŀ��֮ǰ��δƥ�����Ŀ  ����Ҫ�Ƽ�����Ŀ��
	    		//���ƥ��ģ���visit������1 ˵�� ��֮ǰû��δƥ�����Ŀ�Թ��������ס�
	    		m.score[iutold]+=cur.parent_stat;
	    	}
	    	else if(tu.contains(cur.item_name)==false){//�����������˵����ƥ��
	    		cur.color+=1;//visit ��һ
	    		//��ʱҪ�ж��ǵ�һ��δƥ�仹�ǵڶ���
	    		//����ǵ�һ��δƥ��  ˵������Ǵ��Ƽ���  ���ҿ�ʼ���� ���ش��Ƽ���Ŀ�ı��
	    		//����ǵڶ���δƥ��  ˵��ƥ���������
	    		if(cur.color==1){
	    			int iut=Integer.parseInt(cur.item_name);//�ַ���ת����
	    			m.score[iut]+=cur.parent_stat;
	    			return iut;//�µ��Ƽ���item���
	    		}
	    	}
	    	return iutold;//�ɵ��Ƽ���item���
	    }

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
	    		Collections.sort(allChildDeepth,Collections.reverseOrder());
	    		return (Integer)allChildDeepth.get(0) + 1;
	    	}
	    }
	    /**
	     * ������pattern����
	     * @return
	     */
	    public int pattern_count(){
	    	int count = 0;
	    	Stack<Node> s = new Stack<Node>();
			ArrayList<Node>rootchild = m.root.child_list;//���еڶ���ڵ�
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
//	    	if(s ==null)return 0;
//	    	if(s.child_list==null)return 1;
//	    	int sum = 0;
//	    	for(Node n : s.child_list)sum += leaf_node(n);
//	    	return sum;
	    	int count = 0;
	    	Stack<Node> s = new Stack<Node>();
			ArrayList<Node>rootchild = m.root.child_list;//���еڶ���ڵ�
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
//	    	if(s==null)return 0;
//	    	if(s.child_list==null)return 1;
//	    	int sum = 1;
//	    	for(Node n : s.child_list) sum+=total_node(n);
//	    	return sum;
	    	int count = 0;
	    	Stack<Node> s = new Stack<Node>();
			ArrayList<Node>rootchild = m.root.child_list;//���еڶ���ڵ�
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
			if(m.root ==null) return result;
			helper(m.root,m.root.item_name,result);
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
	
	}
	
	/**
	 * ������Ľ���ļ����кϲ�
	 * ���͵�Reduce
	 * @author Lichshe
	 *
	 */
	public static class MP_tree_Mapper_Second extends Mapper<LongWritable,Text,IntWritable,Text>
	{
		public void map(LongWritable key,Text value,Context context) throws IOException, InterruptedException
		{
			String[] tempvalue = value.toString().trim().split(";");
			if(tempvalue.length!=1)
			//key �� �Ƽ����
			{
				
				context.write(new IntWritable(Integer.parseInt(tempvalue[0])),new Text(tempvalue[1]));
			}
		}
	}
	/**
	 * �Խ���ϲ�����
	 * @author Lichshe
	 *
	 */
	public static class MP_tree_Reducer_Second extends Reducer<IntWritable,Text,IntWritable,Text>
	{
		
		public void reduce(IntWritable key,Iterable<Text> value, Context context) throws IOException, InterruptedException
		{
			writerHDFS(key,value,context);
		}
		public void writerHDFS(IntWritable key,Iterable<Text> value,Context context) throws IOException, InterruptedException
		{
			Map<Integer,Float> sortlist = new HashMap<Integer,Float>();
			for(Text tt:value)//���Text
			{
				for(String realtt :tt.toString().split(" "))//�Ȱ��ո�ָ�
				{
					String[] keyvalue = realtt.split(":");//�ٰ����ָ�
					sortlist.put(Integer.parseInt(keyvalue[0]), Float.parseFloat(keyvalue[1]));
				}
			}

			List<Map.Entry<Integer,Float>> infoIds =new ArrayList<Map.Entry<Integer,Float>>(sortlist.entrySet());
	        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	        Collections.sort(infoIds, new Comparator<Map.Entry<Integer,Float>>() {   
	            public int compare(Map.Entry<Integer,Float> o1, Map.Entry<Integer,Float> o2) {      
	            	return ((o2.getValue()>=o1.getValue())?1:-1);
	            }
	        });
   	    	//���ǰʮ��
   	    	String result = "";
   	    	for(int j=0;j<infoIds.size();j++){//ȡ��ǰk������������ļ�
   	 	    	Map.Entry<Integer, Float> mapsort=infoIds.get(j);
   	 	    	int tempkey=mapsort.getKey();
   	 	    	float tempvalue=mapsort.getValue();
   	 	    	result +=String.valueOf(tempkey)+"("+String.valueOf(tempvalue)+")"+" ";	
   	       }
   	 	   context.write(key, new Text(result));
		}
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf,args).getRemainingArgs();
	    //���룬��������ID��trans�ļ���reduce����
	    runMapReduce(args[0],args[1],args[2],args[3],Integer.parseInt(args[4]));
	    //���룬���
//	    runMapReducesecond(args[1],args[5]);
	}
	
	
	   public static void runMapReduce(String input,String output,String maxnum,String TransFilename,int reducenum) throws IOException, ClassNotFoundException, InterruptedException
	  {
		   
		    Configuration conf = new Configuration();
		    conf.set("TransFilename",TransFilename);
		    conf.set("maxnum", maxnum);
		    conf.set("mapred.textoutputformat.separator", ";");
		    Job job = Job.getInstance(conf, "MapReduce01");
		    job.setJarByClass(GOPC_Tree_Main.class);
		    job.setMapperClass(MP_tree_Mapper_First.class);
		    job.setReducerClass(MP_tree_Reducer_First.class);
		    
		    job.setPartitionerClass(MP_tree_Partitioner_First.class);
		    job.setNumReduceTasks(reducenum);
		    
		    job.setOutputKeyClass(IntWritable.class);
		    job.setOutputValueClass(Text.class);
		    
		    FileInputFormat.addInputPath(job, new Path(input));
		    FileOutputFormat.setOutputPath(job,new Path(output));
		    
		    job.waitForCompletion(true);
	  }
	   public static void runMapReducesecond(String input,String output) throws IOException, ClassNotFoundException, InterruptedException
	   {
			   
			    Configuration conf = new Configuration();
			    Job job = Job.getInstance(conf, "MapReduce02");
			    job.setJarByClass(GOPC_Tree_Main.class);
			    job.setMapperClass(MP_tree_Mapper_Second.class);
			    job.setReducerClass(MP_tree_Reducer_Second.class);
			    
			    job.setOutputKeyClass(IntWritable.class);
			    job.setOutputValueClass(Text.class);
			    
			    FileInputFormat.addInputPath(job, new Path(input));
			    FileOutputFormat.setOutputPath(job,new Path(output));
			    
			    job.waitForCompletion(true);
		  }
	 
}
