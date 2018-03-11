package com.kyawn.googlecse.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

	public void createTable() {
		try {
			Class.forName("org.sqlite.JDBC");// 加载驱动,连接sqlite的jdbc
			Connection connection = DriverManager.getConnection("jdbc:sqlite:db/userurl.db");// 连接数据库zhou.db,不存在则创建
			Statement statement = connection.createStatement(); // 创建连接对象，是Java的一个操作数据库的重要接口
			String sql = "create table user(userid varchar(50),url varchar(100))";
			statement.executeUpdate("drop table if exists user");// 判断是否有表tables的存在。有则删除
			statement.executeUpdate(sql); // 创建数据库
			connection.close();// 关闭数据库连接
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int insert(Connection connection, String userid, String url) {
		int result = 0;
		try {
			String sql = "INSERT INTO user (userid,url) VALUES(?,?)";
			System.out.println("sql ps zhiqian:" + sql);
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, userid);
			ps.setString(2, url);
			System.out.println("exe zhiqian");
			result = ps.executeUpdate();
			System.out.println("exe zhihou");
			connection.close();

		} catch (Exception e) {
		}
		return result;
	}

	public boolean update(Connection connection, String userid, String url) throws SQLException {
		boolean result = false;
		String updateSql = "UPDATE user SET url = ? where userid = ?";
		PreparedStatement ps = connection.prepareStatement(updateSql);
		ps.setString(1, url);
		ps.setString(2, userid);
		result = ps.execute();
		System.out.println("update");
		return result;
	}

	public String queryUrl(Connection connection, String userid) {
		String url = null;
		try {
			Statement statement = connection.createStatement(); // 创建连接对象，是Java的一个操作数据库的重要接口
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

//	public static void main(String[] args) throws SQLException, ClassNotFoundException {
//		Class.forName("org.sqlite.JDBC");
//		Connection	 connection = DriverManager.getConnection("jdbc:sqlite:F:\\sanese-master\\db\\userurl.db");//要写绝对路径
//		DB db = new DB();
//		db.createTable();
//	}
	// public static void main(String[] args) {
	// // DB.queryUrl("ofRokwb6b7ohoSYVXqG29espp9no");
	// DB db = new DB();
	// Connection connection = null;
	// try {
	// Class.forName("org.sqlite.JDBC");
	// connection = DriverManager.getConnection("jdbc:sqlite:db/userurl.db");//
	// 连接数据库zhou.db,不存在则创建
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// } catch (SQLException e) {
	// }
	//
	// db.insert(connection, "kaiyaxiong123", "1312");
	// }
}
