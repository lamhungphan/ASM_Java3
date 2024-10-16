package com.rs.controller.admin;


import com.rs.dao.CategoryDAO;
import com.rs.dao.ArticleDAO;
import com.rs.dao.UserDAO;
import com.rs.entity.Article;
import com.rs.entity.Category;
import com.rs.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Servlet implementation class NewsCrudServlet
 */
@WebServlet({"/admin/news","/admin/news/edit/*","/admin/news/blank","/admin/news/create","/admin/news/update","/admin/news/delete","/admin/news/reset","/admin/news/search"})
public class NewsCrudServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Article article;
//	private static Users user;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsCrudServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		List<Article> list;
		String path = request.getServletPath();
		User user = (User) request.getSession().getAttribute("currUser");
		if(path.contains("search") && !request.getParameter("search").isBlank()) {
			if(user.getRole()) {
				try {
					list = ArticleDAO.searchNews(request.getParameter("search"));
					request.setAttribute("list", list);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					list = ArticleDAO.searchNewsByAuthor(user.getId(),request.getParameter("search"));
					request.setAttribute("list", list);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			request.setAttribute("path", "/admin/views/newsList.jsp");

		} else if(path.contains("edit")) {
			String id = request.getPathInfo().substring(1);
			try {
				article = ArticleDAO.getNewsById(Integer.parseInt(id));
			} catch (NumberFormatException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("news", article);
			List<Category> categories;
			try {
				categories = CategoryDAO.getAllCategories();
				request.setAttribute("categories", categories);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("path", "/admin/views/newsDetail.jsp");
		}
		else if(path.contains("blank")) {
			article = new Article();
			try {
				article.setId(ArticleDAO.generateNewId());
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			request.setAttribute("news", article);
			List<Category> categories;
			try {
				categories = CategoryDAO.getAllCategories();
				request.setAttribute("categories", categories);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			request.setAttribute("path", "/admin/views/newsDetail.jsp");
		}
		else if(user.getRole()) {
			try {
				list = ArticleDAO.getAllNews();
				request.setAttribute("list", list);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			request.setAttribute("path", "/admin/views/newsList.jsp");
		}
		else if(!user.getRole()) {
			try {
				list = ArticleDAO.getAllNewsByAuthor(user.getId());
				request.setAttribute("list", list);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			request.setAttribute("path", "/admin/views/newsList.jsp");
		}
		request.getRequestDispatcher("/admin/views/index.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		DateTimeConverter dtc = new DateConverter(new Date());
		dtc.setPattern("MM/dd/yyyy");
		ConvertUtils.register(dtc, Date.class);
		String uri = request.getServletPath();

		if (uri.contains("create")) {
			try {
				BeanUtils.populate(article, request.getParameterMap());
				Part img = request.getPart("img");
				upload(request, img);
				article.setImage(img.getSubmittedFileName());
				article.setPostedDate(new Date());
				article.setAuthor(((Article) request.getSession().getAttribute("user")).getId());
				ArticleDAO.addNews(article);
				request.setAttribute("article", article);
				request.setAttribute("action", "edit");
			} catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.contains("update")) {
			try {
				BeanUtils.populate(article, request.getParameterMap());
				Part img = request.getPart("img");
				if(img!=null && !img.getSubmittedFileName().isBlank()) {
					upload(request, img);
					article.setImage(img.getSubmittedFileName());
				}
				article.setPostedDate(new Date());
				ArticleDAO.updateNews(article);
				request.setAttribute("article", article);
				request.setAttribute("action", "edit");
			} catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.contains("delete")) {
			try {
				ArticleDAO.deleteNews(article.getId());
				article = new Article();
				article.setId(ArticleDAO.generateNewId());
				request.setAttribute("article", article);
				request.setAttribute("action", "edit");
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.contains("reset")) {
			article = new Article();
			try {
				article.setId(UserDAO.generateNewId());
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("article", article);
			request.setAttribute("action", "create");
		}
		request.setAttribute("path", "/admin/views/newsDetail.jsp");
		request.getRequestDispatcher("/admin/views/index.jsp").forward(request, response);
	}
	
	private void upload(HttpServletRequest request, Part img) throws IOException {
		File saveDir = new File(request.getServletContext().getRealPath("/photo"));
		if(!saveDir.exists()) {
			saveDir.mkdirs();
		}
		String path = "/photo/" + img.getSubmittedFileName();
		String fileName = request.getServletContext().getRealPath(path);
		img.write(fileName);
	}
}
