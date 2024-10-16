package com.rs.dao;

import com.rs.entity.Newsletter;
import com.rs.util.other.XJdbc;

import java.sql.SQLException;
import java.util.List;

public class NewsLetterDAO {
//    private Connection connection;
//
//    public NewsLettersDAO(Connection connection) {
//        this.connection = connection;
//    }

    public static void addNewsletter(Newsletter letter) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO NEWSLETTERS (Email, Enabled) VALUES (?, ?)";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, email);
//            stmt.setBoolean(2, enabled);
//            stmt.executeUpdate();
//        }
        XJdbc.IUD(sql, letter.toInsertData());
    }

    public static void updateNewsletter(Newsletter letter) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE NEWSLETTERS SET Enabled = ? WHERE Email = ?";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setBoolean(1, enabled);
//            stmt.setString(2, email);
//            stmt.executeUpdate();
//        }
        XJdbc.IUD(sql, letter.toUpdateData());
    }

    public static void deleteNewsletter(String email) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM NEWSLETTERS WHERE Email = ?";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, email);
//            stmt.executeUpdate();
//        }
        XJdbc.IUD(sql, email);
    }

    public static boolean isNewsletterEnabled(String email) throws SQLException, ClassNotFoundException {
        String sql = "SELECT Enabled FROM NEWSLETTERS WHERE Email = ?";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, email);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getBoolean("Enabled");
//                }
//            }
//        }
        boolean result = XJdbc.getValue(sql, email)!=null;
        return result;
    }
    
    public static List<Newsletter> getAllNewsletter(){
    	String sql = "SELECT * FROM NEWSLETTERS order by Enabled desc";
    	List<Newsletter> list = XJdbc.getResultList(Newsletter.class, sql);
    	return list;
    }
    
    public static List<String> getEnabledEmailList(){
    	String sql = "SELECT Email FROM NEWSLETTERS WHERE Enabled = 1";
    	List<String> list = XJdbc.getResultList(String.class, sql);
    	return list;
    }
    
    public static Boolean checkEnabled(String email){
    	String sql = "SELECT Enabled FROM NEWSLETTERS where Email = ?";
    	Newsletter letter = XJdbc.getSingleResult(Newsletter.class, sql, email);
    	if(letter==null) {
    		return null;
    	}
    	else if (letter.isEnabled()) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
}
