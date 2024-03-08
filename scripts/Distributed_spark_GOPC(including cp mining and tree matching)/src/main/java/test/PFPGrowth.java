package test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
			System.out.println("input��filename	minisupport	partitionnum	 outputfile testFile recomResult");
			System.exit(0);
		}
		SparkConf sparkConf = new SparkConf()
				.set("spark.reducer.maxSizeInFlight", "128M")
				.set("spark.shuffle.io.retryWait", "5s")
				.set("spark.network.timeout", "900000");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		sc.setLogLevel("ERROR");
		String inputFile = args[0];
		double minSupport = Double.parseDouble(args[1]);
		final int partitionnum = Integer.parseInt(args[2]);
		long now = System.currentTimeMillis();
		JavaRDD<List<String>> lines = sc.textFile(inputFile,partitionnum)//ԭʼ��ÿ������
				.map(new Function<String, List<String>>() {

					public List<String> call(String v1) throws Exception {
						// TODO Auto-generated method stub
						return Arrays.asList(v1.split(" "));
					}
		}).cache();
		final long count = lines.count();
		final int absoluteSupport = (int) Math.ceil(minSupport * count);//֧�ֶ�
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
				return v1+v2;//��ͬkey��value���
			}
		}).filter(new Function<Tuple2<String,Integer>, Boolean>() {
			
			public Boolean call(Tuple2<String, Integer> v1) throws Exception { //���˳�����֧�ֶȵ���Ŀ
				// TODO Auto-generated method stub
				if(v1._2 >= absoluteSupport && !v1._1.equals("") && v1._1 != null)return true;
				else return false; 
				}
			}).cache();
		final Map<String,Integer> wordcount = wordcount_filter.collectAsMap();
		final Broadcast<Map<String, Integer>> FMap = sc.broadcast(wordcount);
		JavaPairRDD<String,Integer> Flist = wordcount_filter
				.sortByKey(new comp(){
				public int compare(String o1, String o2) {//��Flist��������
					// TODO Auto-generated method stub
					int result = FMap.getValue().get(o2) - FMap.getValue().get(o1);
					if(result == 0) return o1.compareTo(o2);
					else return result;
				}
			}).cache();
		JavaPairRDD<String,Long> Flist_index = Flist.keys().zipWithIndex().cache();//Flist��item��item�����
		JavaPairRDD<String,Double>IndexWithSupport = Flist_index.mapToPair(new PairFunction<Tuple2<String,Long>, String,Double>() {//��support��index�ĳ˻�
			public Tuple2<String, Double> call(Tuple2<String, Long> t) throws Exception {
				// TODO Auto-generated method stub
				return new Tuple2<String,Double>(t._1, ( Math.log((double)t._2) / Math.log((double)2))  *wordcount.get(t._1));
			}
		})
		.sortByKey(new comp(){
			public int compare(String o1, String o2) {//��Flist��������
				// TODO Auto-generated method stub
				int result = FMap.getValue().get(o2) - FMap.getValue().get(o1);
				if(result == 0) return o1.compareTo(o2);
				else return result;
			}
		});//����ÿ����Ŀ���µĸ���ֵ
		final Map<String,Integer> item_partition = new HashMap<String,Integer>();//�����洢��Ŀ�ķ���
		double[] partition_supp = new double[partitionnum];//ÿ������֧�ֶȵĺ�
		for(Tuple2<String,Double> tu2:IndexWithSupport.collect()){//����ÿ������
			double min = partition_supp[0];
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
						});//��s2�е�Ԫ�ذ�֧�ֶȽ�������
						int[] p = new int[partitionnum];
						for(int i = s1.size()-1;i>=0;i--){
							int  index = item_partition_br.getValue().get(s1.get(i));
							if(p[index]!=1){
								List<String> tran = new ArrayList<String>();
								tran.addAll(s1.subList(0, i + 1));//��ÿ�����ܵķ�������һ������
								Tuple2<Integer, List<String>> t1 = new Tuple2<Integer, List<String>>(index, tran);
								//�����Ͷ�Ӧ��trans
								tra.add(t1);//����ÿ��trans�ֽ�ɵ�        ��Ӧ�ķ�����trans���������
								p[index] = 1;
							}
						}
						return tra;
					}
				});
		JavaPairRDD<Integer, Iterable<List<String>>> Trans_repartition = Trans_split.groupByKey(partitionnum).cache();//�����ɵ�trans���з���   Ч�ʵ�
		JavaRDD<String> Trans3 = Trans_repartition.flatMap(new FlatMapFunction<Tuple2<Integer,Iterable<List<String>>>, String>() {//�����ͷ������е���Ŀ��
			public Iterable<String> call(Tuple2<Integer, Iterable<List<String>>> s) throws IOException {//����string
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
				long two = System.currentTimeMillis();
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
//				s3.add(String.valueOf(two - one));
				Collections.sort(s3);
				return s3;
			}
		}).cache();//�����ڲ�����
		JavaRDD<String> Trans_sort = Trans3.sortBy(new Function<String, String>() {

			public String call(String v1) throws Exception {
				// TODO Auto-generated method stub
				return v1;
			}
		}, true, partitionnum).cache();//��������
		Trans_sort.repartition(1).saveAsTextFile(args[3]);//���������
		List<String>m2_Tree_pattern = new ArrayList();
		m2_Tree_pattern.addAll(Trans_sort.collect());
		final Broadcast<List<String>> m2_final = sc.broadcast(m2_Tree_pattern);//�㲥���������
		final Broadcast<List<List<String>>> testUserFile =sc.broadcast(sc.textFile(args[4]).map(new Function<String, List<String>>() {

			public ArrayList<String> call(String v1) throws Exception {
				// TODO Auto-generated method stub
				return new ArrayList(Arrays.asList(v1.trim().split(" ")));
			}
		}).collect());
		
		JavaRDD<Tuple2<Integer, List<Tuple2<String, Float>>>> result_temp = Trans3.mapPartitions(new FlatMapFunction<Iterator<String>,Tuple2<Integer,List<Tuple2<String,Float>>> >() {

			public List<Tuple2<Integer, List<Tuple2<String, Float>>>> call(Iterator<String> t) throws Exception {
				// TODO Auto-generated method stub
				List<String> s = new ArrayList<String>();
				while(t.hasNext()){
					s.add(t.next());//��Ԫ�ر�����s��
				}
				GOPC_Tree m = new GOPC_Tree();
				long t1 = System.currentTimeMillis();
				m.PopulateTreeView(s);
				long t2 = System.currentTimeMillis();
				m.Tu = testUserFile.getValue();
				long t3 = System.currentTimeMillis();
				GOPC_Tree m2 = new GOPC_Tree();//�����ڶ�����������ѯ
				m2.PopulateTreeView(m2_final.getValue());
				List<Tuple2<Integer,List<Tuple2<String,Float>>>> jieguo = m.SearchPath(m2.root);
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
				if(v1.isEmpty()&&(!v2.isEmpty())) return v2;
				if(v2.isEmpty()&&(!v1.isEmpty())) return v1;
				if(v1.isEmpty()&&v2.isEmpty()) return new ArrayList<Tuple2<String, Float>>();
				float num1 = v1.get(0)._2;
				float num2 = v2.get(0)._2;
				if(num1>num2)return v1;
				else if(num1<num2) return v2;
				else{
					List<Tuple2<String, Float>> res = new ArrayList<Tuple2<String, Float>>();
					Set  set=new HashSet();
					for(Tuple2<String, Float>s :v1)set.add(s._1);
					for(Tuple2<String, Float>s :v2)set.add(s._1);
					for( Iterator  it = set.iterator();  it.hasNext(); )
			        {             
			            res.add(new Tuple2<String, Float>(it.next().toString(),num1));            
			        } 
					return res;
				}
			}
		});
		JavaPairRDD<Integer, String> final_result = real_result.mapValues(new Function<List<Tuple2<String,Float>>, String>() {

			public String call(List<Tuple2<String, Float>> v1) throws Exception {
				// TODO Auto-generated method stub
				String res = "";
				if(v1.isEmpty())return "0";
				int flag=0;
				for(Tuple2<String,Float>s :v1){
					if(flag==0){
						flag = 1;
						res+=s._1;
					}
					else res+=" "+s._1;
				}
				return res;
			}
		});
//		JavaPairRDD<Integer, String> final_result = real_result.combineByKey(new Function<List<Tuple2<String,Float>>, String>() {
//
//			public String call(List<Tuple2<String, Float>> v1) throws Exception {
//				// TODO Auto-generated method stub
//				if(v1.isEmpty()) return "0";
//				String res = "";
//				int flag=0;
//				for(Tuple2<String,Float>s :v1){
//					if(flag==0){
//						flag = 1;
//						res+=s._1;
//					}
//					else res+=" "+s._1;
//				}
//				return res;
//			}
//		}, new Function2<String,List<Tuple2<String,Float>>, String>() {    
//			
//			public String call(String arg1, List<Tuple2<String, Float>> arg2) throws Exception {
//				// TODO Auto-generated method stub
//				if(arg2.isEmpty()) return arg1;
//				String res = "";
//				for(Tuple2<String,Float>s :arg2){
//					res+=" " + s._1;
//				}
//				String temp = arg1 + res;
//				return temp;
//			}
//		}, new Function2<String, String, String>() {
//
//			public String call(String arg1, String arg2) throws Exception {
//				// TODO Auto-generated method stub
//				return arg1+arg2;
//			}
//		});
		final_result.sortByKey().repartition(1).values().saveAsTextFile(args[5]);
		long then = System.currentTimeMillis();
		System.out.println(then - now);
	}
}
