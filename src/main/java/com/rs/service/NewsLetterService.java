package com.rs.service;

import com.rs.dao.NewsLetterDAO;
import com.rs.entity.Newsletter;
import com.rs.util.other.XMailer;

import java.sql.SQLException;
import java.util.List;

public class NewsLetterService {

    public List<Newsletter> getAllNewsletters() {
        return NewsLetterDAO.getAllNewsletter();
    }

    public Newsletter getNewsletterByIndex(int index) throws IndexOutOfBoundsException {
        List<Newsletter> list = getAllNewsletters();
        return list.get(index - 1);
    }

    public void updateNewsletter(Newsletter newsletter) throws ClassNotFoundException, SQLException {
        NewsLetterDAO.updateNewsletter(newsletter);
    }

    public void deleteNewsletter(String email) throws ClassNotFoundException, SQLException {
        NewsLetterDAO.deleteNewsletter(email);
    }

    public boolean isEmailSubscribed(String email) {
        return Boolean.TRUE.equals(NewsLetterDAO.checkEnabled(email));
    }

    public void addOrUpdateNewsletter(String email) throws SQLException, ClassNotFoundException {
        Boolean check = NewsLetterDAO.checkEnabled(email);
        if (check == null) {
            NewsLetterDAO.addNewsletter(new Newsletter(email, true));
        } else if (!check) {
            NewsLetterDAO.updateNewsletter(new Newsletter(email, true));
        }
    }

    public String generateConfirmKey() {
        String allowed = "qwertyuiopasdfghjklzxcvbnmMNBVCXZASDFGHJKLPOIUYTREWQ0123456789";
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append(allowed.charAt((int) (Math.random() * allowed.length())));
        }
        return key.toString();
    }

    public boolean sendConfirmationEmail(String email, String key) {
        return XMailer.send(email, "Mã xác nhận", key);
    }
}
