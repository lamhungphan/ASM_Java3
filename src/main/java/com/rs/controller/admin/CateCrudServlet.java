package com.rs.controller.admin;

import com.rs.dao.CategoryDAO;
import com.rs.entity.Category;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet implementation class CateCrudServlet
 */
@WebServlet({"/admin/category", "/admin/category/update/*", "/admin/category/delete/*", "/admin/category/new", "/admin/category/insert" })
public class CateCrudServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = request.getServletPath();
		if(path.endsWith("category")) {
			try {
				List<Category> list = CategoryDAO.getAllCategories();
				request.setAttribute("list", list);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				throw new ServletException(e);
			}
		}
		else if(path.endsWith("category/insert")) {
			Category c = new Category();
			c.setName(request.getParameter("newCate"));
            try {
                CategoryDAO.addCategory(c);
				response.sendRedirect(request.getContextPath() + "/admin/category");
				return;
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
		else if(path.contains("update")) {
			String id = request.getPathInfo().substring(1);
			try {
				Category category = CategoryDAO.getCategoryById(Integer.parseInt(id));
				category.setName(request.getParameter("updatedName"));
				CategoryDAO.updateCategory(category);
				response.sendRedirect(request.getContextPath() + "/admin/category");
				return;
			} catch (SQLException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
        }
		else if(path.contains("delete")) {
			try {
				CategoryDAO.deleteCategory(Integer.parseInt(request.getPathInfo().substring(1)));
				response.sendRedirect(request.getContextPath() + "/admin/category");
				return;
			} catch (SQLException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
		request.setAttribute("path", "/admin/views/category.jsp");
		request.getRequestDispatcher("/admin/views/index.jsp").forward(request, response);
	}
}
