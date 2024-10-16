package com.rs.controller.admin;

import com.rs.dao.NewsLetterDAO;
import com.rs.entity.Newsletter;
import com.rs.service.NewsLetterService;
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
@WebServlet({"/letter/list", "/letter/edit/*", "/letter/update", "/letter/delete", "/letter/subscribe", "/letter/subscribe/confirm"})
public class NewsLetterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
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
        try {
            new NewsLetterService(request,response).runCrud();
			new NewsLetterService(request,response).subscribe();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        request.setAttribute("path", "/admin/letter.jsp");
		request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
	}

}
