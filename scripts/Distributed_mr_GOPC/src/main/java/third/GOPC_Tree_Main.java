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
	 * 输入  行号 + 行内容（所在的文件编号 + ptn）
	 * 输出  所在文件编号 + ptn
	 */
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	      StringTokenizer itr = new StringTokenizer(value.toString()," ");
	      one.set(Integer.parseInt(itr.nextToken()));
	      word.set(value.toString().split(" ", 2)[1]);
	      //所在文件 + ptn
	      context.write(one,word);
	    }
	}
	
	
	public static class MP_tree_Partitioner_First extends Partitioner<IntWritable,Text>{
		/**
		 * 将key/value分发到不同的reduce
		 */
		@Override
		public int getPartition(IntWritable key, Text value, int numPartitions) {
			// TODO Auto-generated method stub
			int n = key.get();
			return n-1;
		}
	}
	
	/**
	 * 输入 文件编号 + ptn
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
		 * 构建树
		 * 这里Key 只有一个值
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
	        				int listLength_1 = nownode.child_list.size();//当前节点孩子节点的个数
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
	     * 进行识别
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
	    		m.score=new float[maxnum];//value值数组初始化，向每个用户推荐的项目的score，每次都要初始化清零操作，因为不知道元素多少个，所以这里就取所有项目的最大值
	    		//但是为什么不用字典呢  不知道什么原因，这是个全局变量
	    		Stack<Node> s = new Stack<Node>();//栈  用来保存数据
	    		ArrayList<Node> rootchild = m.root.child_list;//获取所有第二层节点
	    		int iut = 0;//推荐的item的编号初始化   这是用来保存待推荐的项目   要保存 然后计算得分
	    		for(Node second :rootchild){//foreach，遍历每个二层节点，获取当前用户完整的推荐
	    			s.push(second);//压栈
	    			while(s.size()!=0){//栈不为空  就继续进行
	    				Node cur = s.pop();//出栈
	    				iut = visit(cur,rowList ,iut);//访问，获取待推荐的项目，和对score进行计算
	    				if (cur.color < 2 && cur.child_list!=null){//错过次数小于二且还存在孩子节点
	    					//等于null说明，当前ptn完全匹配  无推荐
	    					//ptn有一个不匹配  其他完全匹配
	    					for(Node curchild:cur.child_list){
	    						curchild.color=cur.color;//父节点的visit值给孩子节点
	    						s.push(curchild);//当前节点的所有孩子节点压栈
	    					}
	    				}
	    				cur.color=0;//将访问过或者传递给孩子visit值的节点的visit值归零
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

	    public int visit(Node cur,ArrayList<String> tu, int iutold){//每次返回待推荐的项目的编号
	    	if(tu.contains(cur.item_name)&&cur.color==1){//包含且visit==1说明，之前就有未匹配的项目，
	    		//此时的cos值要贡献给之前未匹配的项目。之前的未匹配的项目  就是要推荐的项目。
	    		//如果匹配的，切visit不等于1 说明 ，之前没有未匹配的项目略过，不贡献。
	    		m.score[iutold]+=cur.parent_stat;
	    	}
	    	else if(tu.contains(cur.item_name)==false){//如果不包括，说明不匹配
	    		cur.color+=1;//visit 加一
	    		//这时要判断是第一次未匹配还是第二次
	    		//如果是第一次未匹配  说明这就是待推荐的  并且开始贡献 返回待推荐项目的编号
	    		//如果是第二次未匹配  说明匹配操作结束
	    		if(cur.color==1){
	    			int iut=Integer.parseInt(cur.item_name);//字符串转整树
	    			m.score[iut]+=cur.parent_stat;
	    			return iut;//新的推荐的item编号
	    		}
	    	}
	    	return iutold;//旧的推荐的item编号
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
	     * 求树的pattern个数
	     * @return
	     */
	    public int pattern_count(){
	    	int count = 0;
	    	Stack<Node> s = new Stack<Node>();
			ArrayList<Node>rootchild = m.root.child_list;//所有第二层节点
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
	     * 求叶子节点数
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
			ArrayList<Node>rootchild = m.root.child_list;//所有第二层节点
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
	     * 求总节点数
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
			ArrayList<Node>rootchild = m.root.child_list;//所有第二层节点
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
	     * 打印树的所有路径
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
	 * 将输出的结果文件进行合并
	 * 发送到Reduce
	 * @author Lichshe
	 *
	 */
	public static class MP_tree_Mapper_Second extends Mapper<LongWritable,Text,IntWritable,Text>
	{
		public void map(LongWritable key,Text value,Context context) throws IOException, InterruptedException
		{
			String[] tempvalue = value.toString().trim().split(";");
			if(tempvalue.length!=1)
			//key 和 推荐结果
			{
				
				context.write(new IntWritable(Integer.parseInt(tempvalue[0])),new Text(tempvalue[1]));
			}
		}
	}
	/**
	 * 对结果合并排序
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
			for(Text tt:value)//多个Text
			{
				for(String realtt :tt.toString().split(" "))//先按空格分割
				{
					String[] keyvalue = realtt.split(":");//再按：分割
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
   	    	//输出前十个
   	    	String result = "";
   	    	for(int j=0;j<infoIds.size();j++){//取出前k个进行输出到文件
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
	    //输入，输出，最大ID，trans文件，reduce个数
	    runMapReduce(args[0],args[1],args[2],args[3],Integer.parseInt(args[4]));
	    //输入，输出
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
