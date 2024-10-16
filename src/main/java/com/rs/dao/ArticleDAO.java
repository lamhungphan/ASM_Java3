package com.rs.dao;

import com.rs.entity.Article;
import com.rs.util.other.XJdbc;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class ArticleDAO {
//    private Connection connection;
//
//    public NewsDAO(Connection connection) {
//        this.connection = connection;
//    }

	public static void addNews(Article article) throws SQLException, ClassNotFoundException {
		String sql = "INSERT INTO NEWS (Title, Content, Image, PostedDate, Author, ViewCount, CategoryId, Home) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, id);
//            stmt.setString(2, title);
//            stmt.setString(3, content);
//            stmt.setString(4, image);
//            stmt.setDate(5, postedDate);
//            stmt.setString(6, author);
//            stmt.setInt(7, viewCount);
//            stmt.setString(8, categoryId);
//            stmt.setBoolean(9, home);
//            stmt.executeUpdate();
//        }
		XJdbc.IUD(sql, article.toInsertData());
	}

	public static void updateNews(Article article) throws SQLException, ClassNotFoundException {
		String sql = "UPDATE NEWS SET Title = ?, Content = ?, Image = ?, PostedDate = ?, Author = ?, ViewCount = ?, CategoryId = ?, Home = ? WHERE Id = ?";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, title);
//            stmt.setString(2, content);
//            stmt.setString(3, image);
//            stmt.setDate(4, postedDate);
//            stmt.setString(5, author);
//            stmt.setInt(6, viewCount);
//            stmt.setString(7, categoryId);
//            stmt.setBoolean(8, home);
//            stmt.setString(9, id);
//            stmt.executeUpdate();
//        }
		XJdbc.IUD(sql, article.toUpdateData());
	}

	public static void deleteNews(int id) throws SQLException, ClassNotFoundException {
		String sql = "DELETE FROM NEWS WHERE Id = ?";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, id);
//            stmt.executeUpdate();
//        }
		XJdbc.IUD(sql, id);
	}

	public static Article getNewsById(int id) throws SQLException {
		String sql = "SELECT * FROM NEWS WHERE Id=?";
		Article article = XJdbc.getSingleResult(Article.class, sql, id);

//        try (PreparedStatement stmt = connection.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//            while (rs.next()) {
//                newsList.add(rs.getString("Title"));
//            }
//        }
		return article;
	}

	public static List<Article> getAllNews() throws SQLException {
		String sql = "SELECT * FROM NEWS";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql);

//        try (PreparedStatement stmt = connection.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//            while (rs.next()) {
//                newsList.add(rs.getString("Title"));
//            }
//        }
		return articleList;
	}

	public static List<Article> getAllHomeNews() throws SQLException {
		String sql = "SELECT TOP 5 * FROM NEWS WHERE HOME = 1";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql);

//        try (PreparedStatement stmt = connection.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//            while (rs.next()) {
//                newsList.add(rs.getString("Title"));
//            }
//        }
		return articleList;
	}

	public static List<Article> getAllNewsByAuthor(int authorID) throws SQLException {
		String sql = "SELECT * FROM NEWS WHERE Author = ?";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql, authorID);

//        try (PreparedStatement stmt = connection.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//            while (rs.next()) {
//                newsList.add(rs.getString("Title"));
//            }
//        }
		return articleList;
	}

	public static List<Article> searchNews(String keyword) throws SQLException {
		String sql = "SELECT NEWS.*" +
				" FROM NEWS JOIN USERS ON NEWS.Author = USERS.Id JOIN CATEGORIES ON NEWS.CategoryId = CATEGORIES.Id " +
				"WHERE Title like ? or Content like ? or USERS.Fullname like ? or CATEGORIES.Name like ?";
        return XJdbc.getResultList(Article.class, sql, "%" + keyword + "%");
	}
	
	public static List<Article> searchNewsByAuthor(int authorID, String keyword) throws SQLException {
		String sql = "SELECT NEWS.* FROM NEWS JOIN USERS ON NEWS.Author = USERS.Id WHERE USERS.Id = ? AND Title like ?";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql, "%" + keyword + "%");
		sql = "SELECT NEWS.* FROM NEWS JOIN USERS ON NEWS.Author = USERS.Id WHERE USERS.Id = ? AND Content like ?";
		articleList.addAll(XJdbc.getResultList(Article.class, sql, "%" + keyword + "%"));
		sql = "SELECT NEWS.* FROM NEWS JOIN CATEGORIES ON NEWS.CategoryId = CATEGORIES.Id WHERE USERS.Id = ? AND CATEGORIES.Name like ?";
		articleList.addAll(XJdbc.getResultList(Article.class, sql, "%" + keyword + "%"));
		return articleList;
	}

	public static List<Article> searchAll(String keyword) throws SQLException {
	    String sql = "SELECT DISTINCT NEWS.* " +
	                 "FROM NEWS " +
	                 "LEFT JOIN USERS ON NEWS.Author = USERS.Id " +
	                 "LEFT JOIN CATEGORIES ON NEWS.CategoryId = CATEGORIES.Id " +
	                 "WHERE NEWS.Title LIKE ? " +
	                 "OR NEWS.Content LIKE ? " +
	                 "OR USERS.Fullname LIKE ? " +
	                 "OR CATEGORIES.Name LIKE ?";

	    String searchKeyword = "%" + keyword + "%";
	    return XJdbc.getResultList(Article.class, sql, searchKeyword, searchKeyword, searchKeyword, searchKeyword);
	}

	public static List<Article> getNewsByCategory(String categoryName) throws SQLException {
		String sql = "SELECT NEWS.* FROM NEWS JOIN CATEGORIES ON NEWS.CategoryId = CATEGORIES.Id WHERE CATEGORIES.Name = ?";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql, categoryName);
		return articleList;
	}

	public static List<Article> getNewsByDateRange(Date startDate, Date endDate) throws SQLException {
		String sql = "SELECT * FROM NEWS WHERE PostedDate BETWEEN ? AND ?";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql, startDate, endDate);
		return articleList;
	}
	
	public static List<Article> getLatestNews() throws SQLException {
	    String sql = "SELECT TOP 5 * FROM NEWS ORDER BY PostedDate DESC";
	    List<Article> articleList = XJdbc.getResultList(Article.class, sql);
	    return articleList;
	}

	public static List<Article> getTopNewsByViews() throws SQLException {
		String sql = "SELECT TOP 5 * FROM NEWS ORDER BY ViewCount DESC";
		List<Article> articleList = XJdbc.getResultList(Article.class, sql);
		return articleList;
	}

	

	public static List<Article> getRelatedNews(int categoryId, int newsId) throws SQLException {
		String sql = "SELECT TOP 5 * FROM News WHERE categoryId = ? AND id <> ?";
		return XJdbc.getResultList(Article.class, sql, categoryId, newsId);
	}

	public static int generateNewId() throws ClassNotFoundException, SQLException {
		String sql = "select count(*) from NEWS";
		return (int) XJdbc.getValue(sql) + 1;
	}
}
