package com.rs.service;

import com.rs.dao.ArticleDAO;
import com.rs.dao.CategoryDAO;
import com.rs.dao.UserDAO;
import com.rs.entity.Article;
import com.rs.entity.Category;
import com.rs.entity.User;
import com.rs.util.other.XCookie;
import com.rs.util.other.XFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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

    public void homepage() throws SQLException {
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

    public void listCrud() throws SQLException {
        User user = (User) request.getSession().getAttribute("currUser");
        if (user.getRole()) {
            articleList = ArticleDAO.getAllNews();
            request.setAttribute("list", articleList);
        } else {
            articleList = ArticleDAO.getAllNewsByAuthor(user.getId());
            request.setAttribute("list", articleList);
        }
        request.setAttribute("path", "/admin/newsList.jsp");
    }

    public void detailPage() throws SQLException {
        String id = request.getPathInfo().substring(1);
        Article article = ArticleDAO.getNewsById(Integer.parseInt(id));
        request.setAttribute("news", article);
        if (!viewdIds.contains(article.getId() + "")) {
            viewdIds += article.getId() + "C";
        }
        XCookie cookie = new XCookie(request, response);
        cookie.create("viewArticle", viewdIds.substring(0, viewdIds.length() - 1), 60 * 60 * 24);
        List<Article> relatedNews = ArticleDAO.getRelatedNews(article.getCategoryId(), article.getId());
        request.setAttribute("relatedNewsList", relatedNews);
        request.setAttribute("view", "/user/newsDetail.jsp");
    }

    public void detailCrud() throws SQLException, ClassNotFoundException {
        Article article = null;
        if (request.getServletPath().contains("edit")) {
            String id = request.getPathInfo().substring(1);
            article = ArticleDAO.getNewsById(Integer.parseInt(id));
        } else if (request.getServletPath().contains("blank")) {
            article = new Article();
            article.setId(ArticleDAO.generateNewId());
        }
        request.setAttribute("news", article);
        List<Category> categories;
        categories = CategoryDAO.getAllCategories();
        request.setAttribute("categories", categories);
        request.setAttribute("path", "/admin/newsDetail.jsp");
    }

    public void articleIUD() throws ServletException, IOException, SQLException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        DateTimeConverter dtc = new DateConverter(new Date());
        dtc.setPattern("MM/dd/yyyy");
        ConvertUtils.register(dtc, Date.class);
        String uri = request.getServletPath();
        Article article;

        if (uri.contains("create")) {
            article = new Article();
            BeanUtils.populate(article, request.getParameterMap());
            Part img = request.getPart("img");
            XFile.upload(request, img);
            article.setImage(img.getSubmittedFileName());
            article.setPostedDate(new Date());
            article.setAuthor(((Article) request.getSession().getAttribute("user")).getId());
            ArticleDAO.addNews(article);
            request.setAttribute("article", article);
            request.setAttribute("action", "edit");
        } else if (uri.contains("update")) {
            BeanUtils.populate(article, request.getParameterMap());
            Part img = request.getPart("img");
            if (img != null && !img.getSubmittedFileName().isBlank()) {
                upload(request, img);
                article.setImage(img.getSubmittedFileName());
            }
            article.setPostedDate(new Date());
            ArticleDAO.updateNews(article);
            request.setAttribute("article", article);
            request.setAttribute("action", "edit");
        } else if (uri.contains("delete")) {
            ArticleDAO.deleteNews(article.getId());
            article = new Article();
            article.setId(ArticleDAO.generateNewId());
            request.setAttribute("article", article);
            request.setAttribute("action", "edit");
        } else if (uri.contains("reset")) {
            article = new Article();
            article.setId(UserDAO.generateNewId());
            request.setAttribute("article", article);
            request.setAttribute("action", "create");
        }
    }

    public void searchEngine() throws SQLException {
        String servlet = request.getServletPath();
        if (servlet.startsWith("/user")) {
            String searchQuery = request.getParameter("search");
            articleList = ArticleDAO.searchNews(searchQuery);
            request.setAttribute("newsList", articleList);
            request.setAttribute("view", "/user/newsList.jsp");
        } else if (servlet.startsWith("/admin")) {
            User user = (User) request.getSession().getAttribute("currUser");
            if (user.getRole()) {
                articleList = ArticleDAO.searchNews(request.getParameter("search"));
                request.setAttribute("list", articleList);
            } else {
                articleList = ArticleDAO.searchNewsByAuthor(user.getId(), request.getParameter("search"));
                request.setAttribute("list", articleList);
            }
            request.setAttribute("path", "/admin/newsList.jsp");
        }

    }


}
