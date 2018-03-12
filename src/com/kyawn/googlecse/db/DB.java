package com.kyawn.googlecse.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DB {

	private static String driver;
	private static String dburl;
	// static关键字用来声明一个代码块，只执行一次
	static {
		Properties prop = new Properties();

		try {
			InputStream in = DB.class.getClassLoader().getResourceAsStream("dbconfig.properties");
			prop.load(in);
		} catch (Exception e) {
			System.out.println("=========配置文件读取错误=========");
		}

		driver = prop.getProperty("driver");
		dburl = prop.getProperty("dburl");
	}

	public void createTable() {
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(dburl);
			Statement statement = connection.createStatement(); // 创建连接对象，是Java的一个操作数据库的重要接口
			String sql = "create table user(userid varchar(50),url varchar(100))";
			statement.executeUpdate("drop table if exists user");// 判断是否有表tables的存在。有则删除
			statement.executeUpdate(sql); // 创建数据库
			connection.close();// 关闭数据库连接
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int insert(String userid, String url) throws ClassNotFoundException, SQLException {
		int result = 0;
		Class.forName(driver);
		Connection connection = DriverManager.getConnection(dburl);
		try {
			String sql = "INSERT INTO user (userid,url) VALUES(?,?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, userid);
			ps.setString(2, url);
			result = ps.executeUpdate();
			connection.close();

		} catch (Exception e) {
		}
		return result;
	}

	public int update(String userid, String url) throws SQLException, ClassNotFoundException {
		int result = 0;
		Class.forName(driver);
		Connection connection = DriverManager.getConnection(dburl);
		String sql = "UPDATE user SET url = '" + url + "' where userid = '" + userid + "'";
		PreparedStatement ps = connection.prepareStatement(sql);
		result = ps.executeUpdate();
		return result;
	}

	public String queryUrl(String userid) throws SQLException, ClassNotFoundException {
		String url = null;
		Class.forName(driver);
		Connection connection = DriverManager.getConnection(dburl);
		try {
			Statement statement = connection.createStatement();
			ResultSet rSet = statement.executeQuery("select * from user where userid='" + userid + "'");// 搜索数据库，将搜索的放入数据集ResultSet中
			while (rSet.next()) { // 遍历这个数据集
				System.out.println("username: " + rSet.getString(1));// 依次输出 也可以这样写 rSet.getString(“name”)
				System.out.println("url: " + rSet.getString(2));
				url = rSet.getString(2);
			}
			rSet.close();// 关闭数据集
			connection.close();// 关闭数据库连接
		} catch (Exception e) {
		}
		return url;
	}

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		DB db = new DB();
		// db.createTable();
		// db.insert(connection, "xiongkaiya", "https://google.com");
		db.update("xiongkaiya", "https://baidu.com");
	}

}
