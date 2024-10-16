package com.rs.service;

import com.rs.dao.ArticleDAO;
import com.rs.entity.Article;
import com.rs.util.other.Academics;
import com.rs.util.other.Arguments;
import com.rs.util.other.XCookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ArticleService {

    private List<Article> articleList = null;
    private List<Article> homePageList = null;
    private List<Article> latestList = null;
    private List<Article> mostViewdList = null;
    private List<Article> viewdList = null;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private static String viewdIds = "";

    public ArticleService( HttpServletRequest request, HttpServletResponse response) {
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
        AtomicReference<String> category = new AtomicReference<>("");
        Academics.switchCaseFirst(true, null,
                new Arguments(path.contains("culture"), () -> category.set("Văn hoá")),
                new Arguments(path.contains("tech"), () -> category.set("Công nghệ")),
                new Arguments(path.contains("law"), () -> category.set("Pháp luật")),
                new Arguments(path.contains("sports"), () -> category.set("Thể thao")),
                new Arguments(path.contains("travel"), () -> category.set("Du lịch")));
        try {
            articleList = ArticleDAO.getNewsByCategory(category.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("articleList", articleList);
        request.setAttribute("category", category.get());
        request.setAttribute("view", "/user/newsList.jsp");
    }

    public void detailPage() {
        String id = request.getPathInfo().substring(1);
        try {
            Article article = ArticleDAO.getNewsById(Integer.parseInt(id));
            request.setAttribute("article", article);
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
        String searchQuery = request.getParameter("search").trim();
        if (searchQuery.isBlank()) {
            return;
        }
        try {
            articleList = ArticleDAO.searchNews(searchQuery);
            request.setAttribute("articleList", articleList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("view", "/user/newsList.jsp");
    }
}
