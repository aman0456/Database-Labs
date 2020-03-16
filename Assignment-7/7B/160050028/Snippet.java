
//Set appropriate package name

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.io.*;
import java.util.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.math.*;

/**
 * This class uses Dataset APIs of spark to count number of articles per month
 * The year-month is obtained as a dataset of String
 * */

public class Snippet {

	public static void main(String[] args) throws FileNotFoundException {
		
		//give the path of the main directory where every file is stored here	
		String allFilesPath = "/Users/Aman/Downloads/";
		
		//Input dir - should contain all input json files
		String inputPath=allFilesPath + "newsdata"; //Use absolute paths 
		
		//Ouput dir - this directory will be created by spark. Delete this directory between each run
		String outputPath=allFilesPath + "output";   //Use absolute paths

		//the file for entities
		Scanner file = new Scanner(new File(allFilesPath + "entities.txt"));
		
		//the file for positive words
		Scanner file1 = new Scanner(new File(allFilesPath + "positive-words.txt"));
		
		//the file for negative wordss
		Scanner file2 = new Scanner(new File(allFilesPath + "negative-words.txt"));
		
		
		
		Set<String> entities = new HashSet<>();
		Set<String> posWords = new HashSet<>();
		Set<String> negWords = new HashSet<>();
		while (file.hasNext()) {
		    entities.add(file.next().trim().toLowerCase());
		}
		while (file1.hasNext()) {
		    posWords.add(file1.next().trim().toLowerCase());
		}
		while (file2.hasNext()) {
		    negWords.add(file2.next().trim().toLowerCase());
		}
		//https://stackoverflow.com/questions/29178258/using-hashset-to-store-a-text-file-and-read-from-it
		file.close();
		file1.close();
		file2.close();
		StructType structType = new StructType();
		structType = structType.add("source_name", DataTypes.StringType, false); // false => not nullable
		structType = structType.add("year-month", DataTypes.StringType, false); // false => not nullable
		structType = structType.add("entity", DataTypes.StringType, false); // false => not nullable
		structType = structType.add("sentiment", DataTypes.IntegerType, false); // false => not nullable
		ExpressionEncoder<Row> dateRowEncoder = RowEncoder.apply(structType);
		StructType structType1 = new StructType();
		structType1 = structType1.add("source_name", DataTypes.StringType, false); // false => not nullable
		structType1 = structType1.add("year-month", DataTypes.StringType, false); // false => not nullable
		structType1 = structType1.add("entity", DataTypes.StringType, false); // false => not nullable
		structType1 = structType1.add("sentiment", DataTypes.IntegerType, false); // false => not nullable
		structType1 = structType1.add("count", DataTypes.IntegerType, false); // false => not nullable
		structType1 = structType1.add("sencount", DataTypes.IntegerType, false); // false => not nullable
		ExpressionEncoder<Row> dateRowEncoder1 = RowEncoder.apply(structType1);
		
		SparkSession sparkSession = SparkSession.builder()
				.appName("Month wise news articles")		//Name of application
				.master("local")								//Run the application on local node
				.config("spark.sql.shuffle.partitions","2")		//Number of partitions
				.getOrCreate();
		
		//Read multi-line JSON from input files to dataset
		Dataset<Row> inputDataset=sparkSession.read().option("multiLine", true).json(inputPath);
		
		Dataset<Row> wordSentiments = inputDataset.flatMap(
    			new FlatMapFunction<Row, Row>(){
    				public Iterator<Row> call(Row row) throws Exception {
    					String line = ((String)row.getAs("article_body"));
    					String year_month = ((String)row.getAs("date_published")).substring(0, 7);
    					String source_name = ((String)row.getAs("source_name"));
    					line = line.toLowerCase().replaceAll("[^A-Za-z]", " ");  //Remove all punctuation and convert to lower case
    					line = line.replaceAll("( )+", " ");   //Remove all double spaces
    					line = line.trim(); 
    					List<String> wordList = Arrays.asList(line.split(" ")); //Get words
    					List<Row> wordSentiments = new ArrayList<>();
    					int lsize = wordList.size();
    					for (int i = 0; i < lsize; i++) {
    						String temp = wordList.get(i);
    						if (!entities.contains(temp)) continue;
    						int cnt = 0;
    						for (int j = Math.max(i-5, 0); j <= Math.min(lsize - 1, i + 5); j++) {
    							String temp1 = wordList.get(j);
    							if (posWords.contains(temp1)) {
    								wordSentiments.add(RowFactory.create(source_name, year_month, temp, 1));
    								cnt++;
    							}
    							else if (negWords.contains(temp1)) {
    								wordSentiments.add(RowFactory.create(source_name, year_month, temp, -1));
    								cnt++;
    							}
    						}
    						if (cnt == 0) wordSentiments.add(RowFactory.create(source_name, year_month, temp, 0));
    					}
    					return wordSentiments.iterator();
    				}
    				
    			}, dateRowEncoder );
		Dataset<Row> db = wordSentiments.groupBy("source_name", "year-month", "entity", "sentiment").count().as("count");
		Dataset<Row> db8 = db.groupBy("source_name", "year-month", "entity").agg(functions.sum(db.col("sentiment").multiply(db.col("count"))).as("overall-sentiment"), (functions.count("sentiment").as("overall_support")));
		
		Dataset<Row> db0=db.map(new MapFunction<Row,Row>(){
			public Row call(Row row) throws Exception {
				// The first 7 characters of date_published gives the year-month 
				//String yearMonthPublished=((String)row.getAs("date_published")).substring(0, 7);
				//return yearMonthPublished;
				String sn = ((String)row.getAs("source_name"));
				String sn1 = ((String)row.getAs("year-month"));
				String sn2 = ((String)row.getAs("entity"));
				int sn3 = ((int)row.getAs("sentiment"));
				int sn4 = ((Long) row.getAs("count")).intValue();
				return RowFactory.create(sn, sn1, sn2, sn3, sn4, sn3*sn4);
			}
			
		}, dateRowEncoder1);
		Dataset<Row> db1 = db0.groupBy("source_name", "year-month", "entity").agg(functions.sum("count").as("overall_support"), functions.sum("sencount").as("overall_sentiment"));
		Dataset<Row> db2 = db1.filter(db1.col("overall_support").gt(4));
		Dataset<Row> db3 = db2.select("source_name", "year-month", "entity", "overall_sentiment").orderBy(functions.abs(db2.col("overall_sentiment")).desc());
		//Dataset<Row> db4 = db3.select("source_name", "year-month", "entity", "overall_sentiment");
		db3.toJavaRDD().saveAsTextFile(outputPath);
		
		//Dataset<Row> db2 = db1.;
		
		//Dataset<Row> count = wordSentiments.groupBy("value").count().as("year_month_count");
		
		
		//Outputs the dataset to the standard output
		//count.show();
		
		
		//Ouputs the result to a file
//		count.toJavaRDD().saveAsTextFile(outputPath);	
		
	}
}
