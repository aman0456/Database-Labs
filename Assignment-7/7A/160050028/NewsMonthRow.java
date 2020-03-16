
//Set appropriate package name

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

/**
 * This class uses Dataset APIs of spark to count number of articles per month
 * The year-month is obtained as a dataset of Row
 * */

public class NewsMonthRow {

	public static void main(String[] args) {
		
		//Input dir - should contain all input json files
		String inputPath="/users/ug16/amanb/Downloads/newsdata"; //Use absolute paths 
		
		//Ouput dir - this directory will be created by spark. Delete this directory between each run
		String outputPath="/users/ug16/amanb/output";   //Use absolute paths
		
		StructType structType = new StructType();
	    structType = structType.add("source_name", DataTypes.StringType, false); // false => not nullable
	    structType = structType.add("body", DataTypes.StringType, false); // false => not nullable
	    //structType = structType.add("year-month", DataTypes.StringType, false); // false => not nullable
	    ExpressionEncoder<Row> dateRowEncoder = RowEncoder.apply(structType);
		
		SparkSession sparkSession = SparkSession.builder()
				.appName("Month wise news articles")		//Name of application
				.master("local")								//Run the application on local node
				.config("spark.sql.shuffle.partitions","2")		//Number of partitions
				.getOrCreate();
		
		//Read multi-line JSON from input files to dataset
		JavaRDD<Row> inputDataset=sparkSession.read().option("multiLine", true).json(inputPath).javaRDD();   
		
		
//		// Apply the map function to extract the year-month
//		JavaRDD<Row> temp=inputDataset.map(new MapFunction<Row,Row>(){
//			public Row call(Row row) throws Exception {
//				// The first 7 characters of date_published gives the year-month 
//				String sourceName=((String)row.getAs("source_name"));
//				String body = ((String) row.getAs("article_body"));
//                // RowFactory.create() takes 1 or more parameters, and creates a row out of them.
//				Row returnRow=RowFactory.create(sourceName, body);
//
//				return returnRow;	  
//			}
//			
//		}, dateRowEncoder).javaRDD();
		
		JavaRDD<String> words = inputDataset.flatMap(
    			new FlatMapFunction<Row, String>(){
    				public Iterator<String> call(Row lines) throws Exception {
    					String snmae = lines.getAs("source_name");
    					String line = lines.getAs("article_body");
    					line = line.toLowerCase().replaceAll("[^A-Za-z]", " ");  //Remove all punctuation and convert to lower case
    					line = line.replaceAll("( )+", " ");   //Remove all double spaces
    					line = line.trim(); 
    					List<String> wordList = Arrays.asList(line.split(" ")); //Get words
    					for (int i = 0; i < wordList.size(); i++) {
    						wordList.set(i, snmae + ", " + wordList.get(i));
    					}
    					return wordList.iterator();
    				}
    				
    			} );
		JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));
	    
	    //Aggregate the count into a JavaPairRdd
	    JavaPairRDD<String,Integer> wordCount = ones.reduceByKey(new Function2<Integer,Integer,Integer>(){
			public Integer call(Integer i1, Integer i2) throws Exception {
				return i1+i2;
			}
	    	
	    });
	    wordCount.saveAsTextFile(outputPath);
		// Group by the desired column(s) and take count. groupBy() takes 1 or more parameters
		//Dataset<Row> count=yearMonthDataset.groupBy("year-month").count().as("year_month_count");  
		
		
		//Outputs the dataset to the standard output
		//count.show();
		
		
		//Ouputs the result to a file
		//count.toJavaRDD().saveAsTextFile(outputPath);	
		
	}
	
}

