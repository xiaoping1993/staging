package com.jiping.staging.recommend;

import java.io.IOException;
import java.io.Serializable;

import org.apache.spark.api.java.JavaRDD;import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * 召回模型训练：als
 * @author wangj01052
 *
 */
public class AlsRecall implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static void main(String[] args) throws IOException {
		//初始化运行环境
		SparkSession spark = SparkSession.builder().master("local").appName("DianpingApp").getOrCreate();
		JavaRDD<String> csvFile = spark.read().textFile("file:///C:\\java\\gitcode\\staging\\src\\main\\resources\\behavior.csv").toJavaRDD();
		JavaRDD<Rating> ratingJavaRDD = csvFile.map(new Function<String, Rating>() {
			@Override
			public Rating call(String str) throws Exception {
				// TODO Auto-generated method stub
				return Rating.parseRating(str);
			}
		});
		//变为DataSet<Row> 数据就写入spark计算集合系统中了
		Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD, Rating.class);
		//集合随机拆分8 2 分
		Dataset<Row>[] ratings = rating.randomSplit(new double[] {0.8,0.2});
		Dataset<Row> trainingData = ratings[0];
		Dataset<Row> testData = ratings[1];
		//过拟合：增大数据规模、减少rank，增大正则化的系数
		//欠拟合：增加rank,减少正则化系数
		ALS als = new ALS().setMaxIter(10).setRank(5).setRegParam(0.01).setUserCol("userId").setItemCol("shopId").setRatingCol("rating");
		//模型训练
		ALSModel alsModel = als.fit(trainingData);
		//模型评测
		Dataset<Row> predictions = alsModel.transform(testData);
		//rmse 均方差根误差，预测值与真实值之间误差的平方和除以观测次数，开个根号
		RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("rating").setPredictionCol("prediction");
		double rmse = evaluator.evaluate(predictions);
		alsModel.save("file:///C:\\java\\gitcode\\staging\\src\\main\\resources\\alsmodel");
		
	}
	public static class Rating implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int userId;
		private int shopId;
		private int rating;
		
		public static Rating parseRating(String str) {
			Rating rating = new Rating();
			str = str.replaceAll("\"", "");
			String[] strArr = str.split(",");
			rating.setUserId(Integer.parseInt(strArr[0]));
			rating.setShopId(Integer.parseInt(strArr[1]));
			rating.setRating(Integer.parseInt(strArr[2]));
			return rating;
		}
		/**
		 * @return the userId
		 */
		public int getUserId() {
			return userId;
		}
		/**
		 * @param userId the userId to set
		 */
		public void setUserId(int userId) {
			this.userId = userId;
		}
		/**
		 * @return the shopId
		 */
		public int getShopId() {
			return shopId;
		}
		/**
		 * @param shopId the shopId to set
		 */
		public void setShopId(int shopId) {
			this.shopId = shopId;
		}
		/**
		 * @return the rating
		 */
		public int getRating() {
			return rating;
		}
		/**
		 * @param rating the rating to set
		 */
		public void setRating(int rating) {
			this.rating = rating;
		}
		
	}
}
