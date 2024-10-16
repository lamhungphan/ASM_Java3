package com.rs.controller.admin;

import com.rs.dao.UserDAO;
import com.rs.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet({ "/admin/user", "/admin/user/edit/*", "/admin/user/blank", "/admin/user/create", "/admin/user/update",
		"/admin/user/delete", "/admin/user/reset" })
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static User form = null;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String uri = request.getServletPath();
		if (uri.contains("edit")) {
			String id = request.getPathInfo().substring(1);
			try {
				form = UserDAO.getUserById(Integer.parseInt(id));
				request.setAttribute("item", form);
			} catch (NumberFormatException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("action", "edit");
			request.setAttribute("path", "/admin/views/userDetail.jsp");
		} else if (uri.contains("blank")) {
			form = new User();
			try {
				form.setId(UserDAO.generateNewId());
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("item", form);
			request.setAttribute("action", "create");
			request.setAttribute("path", "/admin/views/userDetail.jsp");
		}
		else {
			request.setAttribute("path", "/admin/views/userList.jsp");
			try {
				List<User> list = UserDAO.getAllUsers();
				request.setAttribute("list", list);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		request.getRequestDispatcher("/admin/views/index.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		DateTimeConverter dtc = new DateConverter(new Date());
		dtc.setPattern("MM/dd/yyyy");
		ConvertUtils.register(dtc, Date.class);
		String uri = request.getServletPath();
		if (uri.contains("create")) {
			try {
				BeanUtils.populate(form, request.getParameterMap());
				UserDAO.addUser(form);
				request.setAttribute("action", "edit");
			} catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.contains("update")) {
			try {
				BeanUtils.populate(form, request.getParameterMap());
				UserDAO.updateUser(form);
				request.setAttribute("action", "edit");
			} catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.contains("delete")) {
			try {
				UserDAO.deleteUser(form.getId());
				response.sendRedirect("SOF203_Assignment/admin/user");
				return;
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.contains("reset")) {
			form = new User();
			try {
				form.setId(UserDAO.generateNewId());
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("action", "create");
		}
		request.setAttribute("item", form);
		request.setAttribute("path", "/admin/views/userDetail.jsp");
		request.getRequestDispatcher("/admin/views/index.jsp").forward(request, response);
	}

}
