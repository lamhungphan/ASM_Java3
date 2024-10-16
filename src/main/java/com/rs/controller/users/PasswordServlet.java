package com.rs.controller.users;

import com.rs.dao.UserDAO;
import com.rs.entity.User;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.rs.util.encrypt.PasswordUtil;
import com.rs.util.other.XMailer;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet implementation class PasswordServlet
 */
@WebServlet({"/user/changePass", "/user/forgetPass", "/user/forgetPass/confirm"})
public class PasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static String key;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        String path = request.getServletPath();
        if (path.contains("changePass")) {
            request.setAttribute("view", "/user/changePass.jsp");
        } else if (path.contains("forgetPass")) {
            if (request.getSession().getAttribute("isConfirm") == null) {
                request.getSession().setAttribute("isConfirm", false);
            }
            request.setAttribute("view", "/user/forgetPass.jsp");
        }
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        String path = request.getServletPath();
        if (path.endsWith("changePass")) {
            String currPass = request.getParameter("currPass");
            String newPass = request.getParameter("newPass");
            String confirmPass = request.getParameter("confirmPass");
            User currUser = (User) request.getSession().getAttribute("currUser");
            if (!PasswordUtil.checkPassword(currPass, currUser.getPassword())) {
                request.setAttribute("error", "Mật khẩu hiện tại không khớp");
                request.setAttribute("view", "/user/changePass.jsp");
            } else if (!newPass.equals(confirmPass)) {
                request.setAttribute("error", "Mật khẩu xác nhận không khớp");
                request.setAttribute("view", "/user/changePass.jsp");
            } else {
                currUser.setPassword(PasswordUtil.hashPassword(newPass));
                try {
                    UserDAO.updateUser(currUser);
                    request.getSession().setAttribute("currUser", null);
                } catch (ClassNotFoundException | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                request.setAttribute("view", "/user/login.jsp");
            }
        } else if (path.endsWith("forgetPass")) {
            boolean isConfirm = (boolean) request.getSession().getAttribute("isConfirm");
            if (isConfirm) {
                String newPass = request.getParameter("newPassword");
                String confirmPass = request.getParameter("confirmPassword");
                if (newPass.equals(confirmPass)) {
                    try {
                        User temp = UserDAO.getUserById((int) request.getSession().getAttribute("passChangeId"));
                        temp.setPassword(PasswordUtil.hashPassword(newPass));
                        UserDAO.updateUser(temp);
                        request.getSession().setAttribute("currUser", null);
                        response.sendRedirect("/SOF203_Assignment/user/login");
                        return;
                    } catch (SQLException | ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else {
                String email = request.getParameter("email");
                try {
                    User temp = UserDAO.getUserByEmail(email);
                    if (temp != null) {
                        key = generateConfirmKey();
                        XMailer.send(email, "Mã xác nhận", key);
                        request.getSession().setAttribute("confirmKey", key);
                        request.getSession().setAttribute("passChangeId", temp.getId());
                        request.setAttribute("formAction", "/forgetPass/confirm");
                        request.getRequestDispatcher("/user/confirmEmail.jsp").forward(request, response);
                        return;
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(e);
                } catch (MessagingException e) {
                    request.setAttribute("error", "Có lỗi xảy ra");
                    request.setAttribute("view", "/user/register.jsp");
                    throw new RuntimeException(e);
                }
            }
        } else if (path.endsWith("confirm")) {
            request.getSession().setAttribute("isConfirm", true);
            response.sendRedirect("/SOF203_Assignment/user/forgetPass");
            return;
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
