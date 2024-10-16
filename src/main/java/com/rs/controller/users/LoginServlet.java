package com.rs.controller.users;

import com.rs.dao.UserDAO;
import com.rs.entity.User;
import com.rs.util.other.XCookie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.rs.util.encrypt.PasswordUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet({"/user/login", "/user/logout"})
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = request.getServletPath();
		if(path.contains("login")) {
			XCookie xCookie = new XCookie(request, response);
			String id = xCookie.getValue("rememberMe");
			if(id!=null && !id.isBlank()) {
				try {
					User user = UserDAO.getUserById(Integer.parseInt(id));
					request.getSession().setAttribute("currUser", user);
					response.sendRedirect(request.getContextPath() + "/user/home");
					return;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}

		request.setAttribute("view", "/user/login.jsp");
		request.getRequestDispatcher("/index.jsp").forward(request, response);
		}
		else if(path.contains("logout")) {
				request.getSession().setAttribute("currUser", null);
				XCookie xCookie = new XCookie(request, response);
				xCookie.delete("rememberMe");
				response.sendRedirect(request.getContextPath() + "/user/home");
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
//		ObjectUtils.isEmpty(email);
		// commons.lang3
		// length > 0, trim = isBlank
		if (email != null && password != null) {
			try {
				User user = UserDAO.getUserByEmail(email);
				if (user != null) {
					if (PasswordUtil.checkPassword(password, user.getPassword())) {
						request.getSession().setAttribute("currUser", user);
						String rememberMe = request.getParameter("rememberMe");
						if (rememberMe != null) {
							Cookie cookie = new Cookie("rememberMe", user.getId()+"");
							cookie.setPath("/SOF203_Assignment");
							cookie.setMaxAge(24*60*60);
							response.addCookie(cookie);
						}
						response.sendRedirect("/SOF203_Assignment/user/home");
						return;
					} else {
						request.setAttribute("error", "Mật khẩu không đúng");
						request.setAttribute("view", "/user/login.jsp");
					}
				} else {
					request.setAttribute("error", "Email không tồn tại");
					request.setAttribute("view", "/user/login.jsp");
				}
			} catch (SQLException | ClassNotFoundException ex) {
				request.setAttribute("error", "Có lỗi xảy ra");
				request.setAttribute("view", "/user/login.jsp");
			}
		} else {
			request.setAttribute("error", "Vui lòng điền đầy đủ thông tin");
			request.setAttribute("view", "/user/login.jsp");
		}
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
}
