package com.rs.controller.admin;

import com.rs.entity.Newsletter;
import com.rs.service.NewsLetterService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet implementation class NewsLetterServlet
 */
@WebServlet({"/admin/letter", "/admin/letter/edit/*", "/admin/letter/update", "/admin/letter/delete", "/user/subscribe", "/user/subscribe/confirm"})
public class NewsLetterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final NewsLetterService newsletterService = new NewsLetterService();
	private Newsletter form;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getServletPath();

		if (path.contains("edit")) {
			int index = Integer.parseInt(request.getPathInfo().substring(1));
			form = newsletterService.getNewsletterByIndex(index);
			request.setAttribute("item", form);

		} else if (path.contains("update")) {
			form.setEmail(request.getParameter("email"));
			form.setEnabled(request.getParameter("enabled") != null);
			try {
				newsletterService.updateNewsletter(form);
				request.setAttribute("item", form);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

		} else if (path.contains("delete")) {
			try {
				newsletterService.deleteNewsletter(form.getEmail());
				form = new Newsletter();
				request.setAttribute("item", form);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

		} else if (path.contains("subscribe")) {
			String email = request.getParameter("email");
			boolean isSubscribed = newsletterService.isEmailSubscribed(email);

			if (isSubscribed) {
				response.sendRedirect("/S203_ASM_Final/user/home");
				return;
			}

			String key = newsletterService.generateConfirmKey();
			boolean isSent = newsletterService.sendConfirmationEmail(email, key);

			if (isSent) {
				request.getSession().setAttribute("confirmKey", key);
				request.getSession().setAttribute("newsLetter", email);
				request.setAttribute("formAction", "/subscribe/confirm");
				request.getRequestDispatcher("/user/confirmEmail.jsp").forward(request, response);
			} else {
				request.setAttribute("message", "Có lỗi xảy ra");
				request.getRequestDispatcher(request.getContextPath() + "/user/home").forward(request, response);
			}

		} else if (path.contains("confirm")) {
			String email = (String) request.getSession().getAttribute("newsLetter");
			try {
				newsletterService.addOrUpdateNewsletter(email);
				response.sendRedirect("/S203_ASM_Final/user/home");
			} catch (SQLException | ClassNotFoundException e) {
				throw new ServletException(e);
			}
		}

		request.setAttribute("path", "/admin/letter.jsp");
		request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
	}
}
