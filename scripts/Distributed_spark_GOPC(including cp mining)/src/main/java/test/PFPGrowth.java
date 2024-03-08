package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

public class PFPGrowth {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length<6){
			System.out.println("input：filename	minisupport	partitionnum	 outputfile testFile recomResult");
			System.exit(0);
		}
		SparkConf sparkConf = new SparkConf()
				.set("spark.reducer.maxSizeInFlight", "128M")
				.set("spark.shuffle.io.retryWait", "5s")
				.set("spark.network.timeout", "900000");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		String inputFile = args[0];
		double minSupport = Double.parseDouble(args[1]);
		final int partitionnum = Integer.parseInt(args[2]);
		long now = System.currentTimeMillis();
		JavaRDD<List<String>> lines = sc.textFile(inputFile,partitionnum)
				.map(new Function<String, List<String>>() {

					public List<String> call(String v1) throws Exception {
						// TODO Auto-generated method stub
						return Arrays.asList(v1.split(" "));
					}
		}).cache();
		final long count = lines.count();
		final int absoluteSupport = (int) Math.ceil(minSupport * count);
		JavaPairRDD<String,Integer> wordcount_filter = lines
				.flatMapToPair(new PairFlatMapFunction<List<String>, String, Integer>() {
					public Iterable<Tuple2<String, Integer>> call(List<String> t) throws Exception {
						// TODO Auto-generated method stub
						ArrayList<Tuple2<String, Integer>> temp = new ArrayList<Tuple2<String,Integer>>();
						for(String s : t){
							temp.add(new Tuple2<String,Integer>(s,1));
						}
						return temp;
					}
				}).reduceByKey(new Function2<Integer, Integer, Integer>() {
			
			public Integer call(Integer v1, Integer v2) throws Exception {
				// TODO Auto-generated method stub
				return v1+v2;//相同key的value相加
			}
		}).filter(new Function<Tuple2<String,Integer>, Boolean>() {
			
			public Boolean call(Tuple2<String, Integer> v1) throws Exception { //过滤出大于支持度的项目
				// TODO Auto-generated method stub
				if(v1._2 >= absoluteSupport && !v1._1.equals("") && v1._1 != null)return true;
				else return false; 
				}
			}).cache();
		long Flist_size = wordcount_filter.count();
		Map<String,Integer> wordcount = wordcount_filter.collectAsMap();
		final Broadcast<Map<String, Integer>> FMap = sc.broadcast(wordcount);
		JavaPairRDD<String,Integer> Flist = wordcount_filter
				.sortByKey(new comp(){
				public int compare(String o1, String o2) {//对Flist进行排序
					// TODO Auto-generated method stub
					int result = FMap.getValue().get(o2) - FMap.getValue().get(o1);
					if(result == 0) return o1.compareTo(o2);
					else return result;
				}
			}).cache();
		JavaPairRDD<String,Long> Flist_index = Flist.keys().zipWithIndex().cache();//Flist的item及item的序号
		Map<String,Long> Flist_index_map = Flist_index.collectAsMap();//把Flist转换为Map
		final Broadcast<Map<String,Long>> Flist_index_map_br = sc.broadcast(Flist_index_map);//把Flist中的项目和对应的序号广播出去
		final long patition_Flist_size = Flist_size / partitionnum + 1;//长度等于除以分区数  加一
		final Map<String,Integer> item_partition = new HashMap<String,Integer>();//用来存储项目的分区
		long[] partition_supp = new long[partitionnum];//每个分区支持度的和
		for(Tuple2<String,Long> tu2:Flist_index.collect()){//遍历每个分区
			long min = partition_supp[0];
			int index = 0;
			for (int i = 1; i < partitionnum; i++ )
			{
				if (partition_supp[i] < min)
				{
					min = partition_supp[i];
					index = i;
					}
				}
			partition_supp[index] += wordcount.get(tu2._1);
			item_partition.put(tu2._1, index);
		}
		final Broadcast<Map<String,Integer>> item_partition_br = sc.broadcast(item_partition);
		for(int q =0;q<partitionnum;q++)System.out.println(partition_supp[q]);
		final Broadcast<Map<Long,Iterable<String>>> G_list = sc.broadcast(Flist_index.mapToPair(new PairFunction<Tuple2<String,Long>, Long, String>() {

			public Tuple2<Long, String> call(Tuple2<String, Long> t) throws Exception {
				// TODO Auto-generated method stub
				
				return new Tuple2<Long,String>((long)item_partition.get(t._1),t._1);
			}
		}).groupByKey().collectAsMap());
		JavaPairRDD<Integer, List<String>> Trans_split = lines
				.flatMapToPair(new PairFlatMapFunction<List<String>, Integer,List<String>>() {
					public Iterable<Tuple2<Integer, List<String>>> call(List<String> t) throws Exception {
						// TODO Auto-generated method stub
						List<Tuple2<Integer, List<String>>> tra = new ArrayList<Tuple2<Integer, List<String>>>();
						List<String> s1 = new ArrayList<String>();
						for(String s : t){
							Integer index = FMap.getValue().get(s);
							if(index!=null){
								s1.add(s);
							}
						}
						Collections.sort(s1,new Comparator<String>(){

							public int compare(String arg0, String arg1) {
								// TODO Auto-generated method stub
								int result = FMap.getValue().get(arg1) - FMap.getValue().get(arg0);
								if(result == 0) return arg0.compareTo(arg1);
								else return result;
							}
						});//把s2中的元素按支持度降序排列
						int[] p = new int[partitionnum];
						for(int i = s1.size()-1;i>=0;i--){
							int  index = item_partition_br.getValue().get(s1.get(i));
							if(p[index]!=1){
								List<String> tran = new ArrayList<String>();
								tran.addAll(s1.subList(0, i + 1));//在每个可能的分区保存一个副本
								Tuple2<Integer, List<String>> t1 = new Tuple2<Integer, List<String>>(index, tran);
								//分区和对应的trans
								tra.add(t1);//保存每个trans分解成的        对应的分区和trans副本的情况
								p[index] = 1;
							}
						}
						return tra;
					}
				}).cache();
		JavaPairRDD<Integer, Iterable<List<String>>> Trans_repartition = Trans_split.groupByKey(partitionnum).cache();//对生成的trans进行分区   效率低
		JavaRDD<String> Trans3 = Trans_repartition.flatMap(new FlatMapFunction<Tuple2<Integer,Iterable<List<String>>>, String>() {//分区和分区所有的项目集
			public Iterable<String> call(Tuple2<Integer, Iterable<List<String>>> s) throws IOException {//返回string
				long one = System.currentTimeMillis();
				Iterator<List<String>> s1 = s._2.iterator();
				FPTree tree = new FPTree();
				tree.minSupport = (int) absoluteSupport;
				Map<String,Integer> frequency = new HashMap<String,Integer>();
				while(s1.hasNext()){
					LinkedList<String>list1 = new LinkedList<String>(s1.next());
					for(String ss:list1){
						if(!frequency.containsKey(ss))frequency.put(ss, 0);
						frequency.put(ss, frequency.get(ss) + 1);
					}
					tree.addTransaction(list1, 1);
				}
				tree.frequency = frequency;
				Iterator<String>s2 =G_list.value().get(s._1).iterator();
				List<String> Header = new ArrayList<String>();
				while(s2.hasNext()){
					Header.add(s2.next());
				}
				tree.header = Header;
				FPGrowth_Sub sub = new FPGrowth_Sub();
				sub.FPGrowth(tree, null);
				List<String> s3 = new ArrayList<String>(); 
				Map<List<String>,Integer> frequentMap = sub.frequentMap;
				for (Entry<List<String>, Integer> entry : frequentMap.entrySet()) {
					Integer patterncount = entry.getValue();
					String pattern ="";
					for(String ps : entry.getKey()){
						pattern += ps + " ";
					}
					pattern += "" + (double)patterncount/count;
					s3.add(pattern);
				}
				long two = System.currentTimeMillis();
				System.out.println(two - one);
				return s3;
			}
		}).cache();
		Trans3.saveAsTextFile(args[3]);
		final Broadcast<List<List<String>>> testUserFile =sc.broadcast(sc.textFile(args[4]).map(new Function<String, List<String>>() {

			public ArrayList<String> call(String v1) throws Exception {
				// TODO Auto-generated method stub
				return new ArrayList(Arrays.asList(v1.trim().split(" ")));
			}
		}).collect());
		JavaRDD<Tuple2<Integer, List<Tuple2<String, Float>>>> result_temp = Trans3.mapPartitions(new FlatMapFunction<Iterator<String>, Tuple2<Integer,List<Tuple2<String,Float>>>>() {

			public Iterable<Tuple2<Integer, List<Tuple2<String, Float>>>> call(Iterator<String> t) throws Exception {
				// TODO Auto-generated method stub
				List<String> s = new ArrayList<String>();
				while(t.hasNext()){
					s.add(t.next());//将元素保存在s中
				}
				GOPC_Tree m = new GOPC_Tree();
				long t1 = System.currentTimeMillis();
				m.PopulateTreeView(s);
				long t2 = System.currentTimeMillis();
				m.Tu = testUserFile.getValue();
				long t3 = System.currentTimeMillis();
				List<Tuple2<Integer,List<Tuple2<String,Float>>>> jieguo = m.SearchPath();
				long t4 = System.currentTimeMillis();
				System.out.println("BuildTree:" + String.valueOf(t2-t1));
				System.out.println("Match:"+ String.valueOf(t4-t3));
				return jieguo;
			}
		}).cache();
		JavaPairRDD<Integer, List<Tuple2<String, Float>>> result_2 = result_temp.mapToPair(new PairFunction<Tuple2<Integer,List<Tuple2<String,Float>>>, Integer, List<Tuple2<String,Float>>>() {

			public Tuple2<Integer, List<Tuple2<String, Float>>> call(Tuple2<Integer, List<Tuple2<String, Float>>> t)
					throws Exception {
				// TODO Auto-generated method stub
				return t;
			}
		});
		 JavaPairRDD<Integer, List<Tuple2<String, Float>>> real_result = result_2.reduceByKey(new Function2<List<Tuple2<String,Float>>, List<Tuple2<String,Float>>, List<Tuple2<String,Float>>>() {
			
			public List<Tuple2<String, Float>> call(List<Tuple2<String, Float>> v1, List<Tuple2<String, Float>> v2)
					throws Exception {
				// TODO Auto-generated method stub
				Map<String,Float> m_temp = new HashMap<String,Float>();
				for(Tuple2<String, Float>g :v1){
					m_temp.put(g._1, g._2);
				}
				for(Tuple2<String, Float>g :v2){
					m_temp.put(g._1, g._2+m_temp.getOrDefault(g._1,(float)0));
				}
				List<Tuple2<String, Float>> res = new ArrayList<Tuple2<String, Float>>();
				Iterator<Map.Entry<String, Float>> entries = m_temp.entrySet().iterator();
				while (entries.hasNext()) {
				    Map.Entry<String, Float> entry = entries.next();
				    res.add(new Tuple2<String,Float>(entry.getKey(),entry.getValue()));
				}
				return res;
			}
		});
		 JavaPairRDD<Integer, String> final_result = real_result.mapValues(new Function<List<Tuple2<String,Float>>, String>() {

				public String call(List<Tuple2<String, Float>> v1) throws Exception {
					// TODO Auto-generated method stub
					String res = "";
					int flag=0;
					for(Tuple2<String,Float>s :v1){
						if(flag==0){
							flag = 1;
							res+=s._1 +"("+String.format("%.3f", s._2)+")";
						}
						else res+=" "+s._1 + "("+String.format("%.3f", s._2)+")";
					}
					return res;
				}
			});
		 final_result.sortByKey().repartition(1).values().saveAsTextFile(args[5]);
		long then = System.currentTimeMillis();
		System.out.println(then - now);
	}
}
