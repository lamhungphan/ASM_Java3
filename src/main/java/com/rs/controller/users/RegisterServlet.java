package com.rs.controller.users;

import com.rs.dao.UserDAO;
import com.rs.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.rs.util.encrypt.AES;
import com.rs.util.encrypt.PasswordUtil;
import com.rs.util.other.XMailer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet({ "/user/register", "/user/register/confirm" })
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String AES_KEY;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RegisterServlet() {
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
		request.setAttribute("view", "/user/register.jsp");
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Lấy thông tin từ form đăng ký
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		String path = request.getServletPath();
		String confirmKey;
		if(path.contains("confirm")) {
			String confirmKeyInput = request.getParameter("confirm");
			confirmKey = (String) request.getSession().getAttribute("confirmKey");
			if(confirmKeyInput.equals(confirmKey)) {
				// Mã hóa mật khẩu với SHA-256
				String regPassword = (String) request.getSession().getAttribute("regPassword");
				String regEmail;
				try {
					regEmail = AES.decryptPassword((String) request.getSession().getAttribute("regEmail"), AES_KEY);
					// Lưu thông tin người dùng vào cơ sở dữ liệu
					UserDAO.addUser(regEmail, regPassword, false);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException | ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					request.getSession().setAttribute("confirmKey", null);
					request.getSession().setAttribute("regEmail",null);
					request.getSession().setAttribute("regPassword", null);
				}

				

				// Điều hướng đến trang đăng nhập thành công
				response.sendRedirect("/SOF203_Assignment/user/login");
				return;
			}
			else {
				request.setAttribute("errorMess", "Mã xác nhận sai, hãy nhập lại");
				request.getRequestDispatcher("/user/confirmEmail.jsp").forward(request, response);
				return;
			}
		}

		else if (email != null && password != null) {
			try {
				User existingUser = UserDAO.getUserByEmail(email);
				if (existingUser != null) {
					request.setAttribute("errorEmail", "Email đã tồn tại, vui lòng sử dụng email khác");
					request.setAttribute("view", "/user/register.jsp");
				}

				else if (!password.equals(confirmPassword)) {
					request.setAttribute("errorPassword", "Mật khẩu và mật khẩu xác nhận không khớp");
					request.setAttribute("view", "/user/register.jsp");
				} else {
					// Gửi mail cùng mã xác nhận
					confirmKey = generateConfirmKey();
					boolean isSent = XMailer.send(email, "Mã xác nhận", confirmKey);
					if (isSent) {
						AES_KEY = AES.generateSecretKey();
						request.getSession().setAttribute("confirmKey", confirmKey);
						request.getSession().setAttribute("regEmail", AES.encryptPassword(email, AES_KEY));
						request.getSession().setAttribute("regPassword", PasswordUtil.hashPassword(password));
						request.setAttribute("formAction", "/register/confirm");
						request.getRequestDispatcher("/user/confirmEmail.jsp").forward(request, response);
						return;
					} else {
						request.setAttribute("errorPassword", "Mã xác nhận không khớp");
						request.setAttribute("view", "/user/register.jsp");
					}
				}
			} catch (SQLException | ClassNotFoundException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
				request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau");
				request.setAttribute("view", "/user/register.jsp");
			}
		} else {
			request.setAttribute("error", "Vui lòng điền đầy đủ thông tin");
			request.setAttribute("view", "/user/register.jsp");
		}
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	private String generateConfirmKey() {
		String allowed = "qwertyuiopasdfghjklzxcvbnmMNBVCXZASDFGHJKLPOIUYTREWQ0123456789";
		String key = "";
		for (int i = 0; i < 6; i++) {
			key += allowed.charAt((int) (Math.random() * allowed.length()));
		}
		return key;

	}
}
