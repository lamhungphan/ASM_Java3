package com.rs.service;

import com.rs.dao.ArticleDAO;
import com.rs.entity.Article;
import com.rs.util.other.XCookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleService {

    private List<Article> articleList = null;
    private List<Article> homePageList = null;
    private List<Article> latestList = null;
    private List<Article> mostViewdList = null;
    private List<Article> viewdList = null;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private static String viewdIds = "";

    public ArticleService(HttpServletResponse response, HttpServletRequest request) {
        this.response = response;
        this.request = request;
        this.articleList = new ArrayList<>();
        this.homePageList = new ArrayList<>();
        this.latestList = new ArrayList<>();
        this.mostViewdList = new ArrayList<>();
        this.viewdList = new ArrayList<>();
    }

    public void homepage() {
        try {
            articleList = ArticleDAO.getAllHomeNews();
            homePageList = ArticleDAO.getAllHomeNews();
            latestList = ArticleDAO.getLatestNews();
            mostViewdList = ArticleDAO.getTopNewsByViews();

            viewdList = new ArrayList<>();
            String[] viewedIds = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("viewedArticles")) {
                        viewedIds = cookies[i].getValue().split("C");
                        break;
                    }
                }
            }

            if (viewedIds != null) {
                for (String id : viewedIds) {
                    Article article = ArticleDAO.getNewsById(Integer.parseInt(id));
                    viewdList.add(article);
                    if (viewdList.size() == 6) {
                        break;
                    }
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("newsList", articleList);
        request.setAttribute("homePageList", homePageList);
        request.setAttribute("latestList", latestList);
        request.setAttribute("mostViewdList", mostViewdList);
        request.setAttribute("view", "/user/home.jsp");
        request.setAttribute("viewdList", viewdList);
    }

    public void listPage() {
        String path = request.getServletPath();
        String category = "";
        switch (path) {
            case "culture":
                category = "Văn hoá";
                break;
            case "tech":
                category = "Công nghệ";
                break;
            case "law":
                category = "Pháp luật";
                break;
            case "sports":
                category = "Thể thao";
                break;
            case "travel":
                category = "Du lịch";
                break;
        }

        try {
            articleList = ArticleDAO.getNewsByCategory(category);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("newsList", articleList);
        request.setAttribute("view", "/user/newsList.jsp");
    }

    public void detailPage() {
        String id = request.getPathInfo().substring(1);

        try {
            Article article = ArticleDAO.getNewsById(Integer.parseInt(id));
            request.setAttribute("news", article);
            if (!viewdIds.contains(article.getId() + "")) {
                viewdIds += article.getId() + "C";
            }

            XCookie cookie = new XCookie(request, response);
            cookie.create("viewArticle", viewdIds.substring(0, viewdIds.length() - 1), 60 * 60 * 24);

            List<Article> relatedNews = ArticleDAO.getRelatedNews(article.getCategoryId(), article.getId());
            request.setAttribute("relatedNewsList", relatedNews);
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("view", "/user/newsDetail.jsp");
    }

    public void searchEngine() {
        String searchQuery = request.getParameter("search");
        try {
            articleList = ArticleDAO.searchNews(searchQuery);
            request.setAttribute("newsList", articleList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("view", "/user/newsList.jsp");
    }


}
