package com.jiping.staging.recommend;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.tomcat.util.buf.StringUtils;

import com.jiping.staging.recommend.AlsRecall.Rating;

/**
 * 召回模型als 预测
 * @author wangj01052
 *
 */
public class AlsRecallPredict {
	public static void main(String[] args){
		//初始化spark运行环境
		SparkSession spark = SparkSession.builder().master("local").appName("DianpingApp").getOrCreate();
		String path = "file:///C:\\java\\gitcode\\staging\\src\\main\\resources\\alsmodel";
		ALSModel alsModel = ALSModel.load(path);
		JavaRDD<String> csvFile = spark.read().textFile("file:///C:\\java\\gitcode\\staging\\src\\main\\resources\\behavior.csv").toJavaRDD();
		JavaRDD<Rating> ratingJavaRDD = csvFile.map(new Function<String, Rating>() {
			@Override
			public Rating call(String str) throws Exception {
				// TODO Auto-generated method stub
				return Rating.parseRating(str);
			}
		});
		Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD, Rating.class);
		//给5个user做召回结果的预测
		Dataset<Row> users = rating.select(alsModel.getUserCol()).distinct().limit(5);
		Dataset<Row> usersRecs = alsModel.recommendForItemSubset(users, 20);
		//先分片 
		usersRecs.foreachPartition(new ForeachPartitionFunction<Row>() {

			@Override
			public void call(Iterator<Row> t) throws Exception {
				//建立数据库连接
				Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/dianpingdb?user=root&password=root&useUnicode=true&useSSL=false&characterEncoding=UTF-8");
				PreparedStatement pst=  connection.prepareStatement("insert into recommend(id,recommend) values (?,?)");
				List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
				//再遍历
				t.forEachRemaining(action->{
					//这里才是Row值了
					int userId = action.getInt(0);
					List<GenericRowWithSchema> recommendationList = action.getList(1);
					List<String> shopIdList = new ArrayList<String>();
					recommendationList.forEach(row->{
						Integer shopId = row.getInt(0);
						shopIdList.add(shopId.toString());
					});
					String recommend = StringUtils.join(shopIdList, ',');
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("userId", userId);
					map.put("recommend",recommend);
					data.add(map);
				});
				data.forEach(recommends->{
					int userId = Integer.parseInt(recommends.get("userId").toString()); 
					String recommend = recommends.get("recommend").toString();
					try {
						pst.setInt(1, userId);
						pst.setString(2, recommend);
						pst.addBatch();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				pst.executeBatch();
			}
		});
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
