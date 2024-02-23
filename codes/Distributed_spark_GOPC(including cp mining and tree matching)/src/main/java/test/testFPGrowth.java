package test;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.FPGrowthModel;

public class testFPGrowth {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SparkConf sparkConf = new SparkConf()
				.setAppName("PFPGrowth")
				.set("spark.reducer.maxSizeInFlight", "128M")
				.set("spark.shuffle.io.retryWait", "5s")
				.set("spark.network.timeout", "900000");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		String inputFile = args[0];
		double minSupport = Double.parseDouble(args[1]);
		final int partitionnum = Integer.parseInt(args[2]);
		JavaRDD<List<String>> lines = sc.textFile(inputFile,partitionnum)
				.map(new Function<String, List<String>>() {

					public List<String> call(String v1) throws Exception {
						// TODO Auto-generated method stub
						return Arrays.asList(v1.split(" "));
					}
		}).cache();
		FPGrowth fpg = new FPGrowth()
				.setMinSupport(minSupport)
				.setNumPartitions(partitionnum);
		FPGrowthModel<String> model = fpg.run(lines);
		for (FPGrowth.FreqItemset<String> itemset: model.freqItemsets().toJavaRDD().collect()) {
			System.out.println("[" + itemset.javaItems() + "], " + itemset.freq());
			}
	}

}
