package com.rs.controller.users;

import com.rs.dao.ArticleDAO;
import com.rs.entity.Article;
import com.rs.service.ArticleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.tags.shaded.org.apache.bcel.generic.RETURN;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet implementation class NewsServlet
 */
@WebServlet({"/user/home", "/user/culture", "/user/law", "/user/sports", "/user/travel", "/user/tech",
        "/user/detail/*", "/user/search"})
public class HomepageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ArticleService articleService;

    public HomepageServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        articleService = new ArticleService(request,response);
        try {
            if (uri.contains("home")) {
                articleService.homepage();
            } else if (uri.contains("detail")) {
                articleService.detailPage();
            } else if (uri.contains("search") && !request.getParameter("search").isBlank()) {
                articleService.searchEngine();
            } else if (request.getParameter("search") != null && request.getParameter("search").isBlank() ) {
                articleService.homepage();
            } else {
                articleService.listPage();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
