package com.rs.controller.admin;

import com.rs.dao.NewsLetterDAO;
import com.rs.entity.Newsletter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.rs.util.other.XMailer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet implementation class NewsLetterServlet
 */
@WebServlet({"/admin/letter", "/admin/letter/edit/*", "/admin/letter/update", "/admin/letter/delete", "/user/subscribe", "/user/subscribe/confirm"})
public class NewsLetterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	List<Newsletter> list;
	private Newsletter form;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsLetterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		list = NewsLetterDAO.getAllNewsletter();
		String path = request.getServletPath();
		if(path.contains("edit")) {
			int index = Integer.parseInt(request.getPathInfo().substring(1));
			form = list.get(index-1);
			request.setAttribute("item", form);
		}
		else if(path.contains("update")) {
			form.setEmail(request.getParameter("email"));
			form.setEnabled(request.getParameter("enabled")!=null);
			try {
				NewsLetterDAO.updateNewsletter(form);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("item", form);
		}
		else if(path.contains("delete")) {
			try {
				NewsLetterDAO.deleteNewsletter(form.getEmail());
				form = new Newsletter();
				request.setAttribute("item", form);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (path.contains("subscribe")) {
			String email = request.getParameter("email");
			Boolean check = NewsLetterDAO.checkEnabled(email);
			if(Boolean.TRUE.equals(check)) {
				response.sendRedirect("/SOF203_Assignment/user/home");
				return;
			}
			String key = generateConfirmKey();
			boolean isSent = XMailer.send(email, "Mã xác nhận", key);
			if (isSent) {
				request.getSession().setAttribute("confirmKey", key);
				request.getSession().setAttribute("newsLetter", email);
				request.setAttribute("formAction", "/subscribe/confirm");
				request.getRequestDispatcher("/user/confirmEmail.jsp").forward(request, response);
				return;
			}
			else{
				request.setAttribute("message", "Có lỗi xảy ra");
				request.getRequestDispatcher(request.getContextPath()+"/user/home").forward(request, response);
				return;
			}
		} else if (path.contains("confirm")) {
			String email = (String) request.getSession().getAttribute("newsLetter");
			Boolean check = NewsLetterDAO.checkEnabled(email);
			if(check==null) {
				try {
					NewsLetterDAO.addNewsletter(new Newsletter(email, true));
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					throw new ServletException(e);
				}
			}
			else if(!check) {
				try {
					NewsLetterDAO.updateNewsletter(new Newsletter(email, true));
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					throw new ServletException(e);
				}
			}
			response.sendRedirect("/SOF203_ASM/user/home");
			return;
		}
		request.setAttribute("path", "/admin/letter.jsp");
		request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
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
