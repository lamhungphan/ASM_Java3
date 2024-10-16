package com.rs.service;

import com.rs.dao.UserDAO;
import com.rs.entity.User;
import com.rs.util.encrypt.PasswordUtil;
import com.rs.util.other.XCookie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class UserService {
    private HttpServletRequest request;
    private HttpServletResponse response;
    public UserService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public void login() throws ServletException, IOException, SQLException {
        if(request.getMethod().equals("GET")){
            XCookie xCookie = new XCookie(request, response);
            String id = xCookie.getValue("rememberMe");
            if(id!=null && !id.isBlank()) {
                User user = UserDAO.getUserById(Integer.parseInt(id));
                request.getSession().setAttribute("currUser", user);
                response.sendRedirect(request.getContextPath() + "/user/home");
                return;
            }
            request.setAttribute("view", "/user/login.jsp");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
        else if(request.getMethod().equals("POST")){
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
                                XCookie xCookie = new XCookie(request, response);
                                xCookie.create("rememberMe",user.getId()+"",24*60*60);
                            }
                            response.sendRedirect( request.getContextPath() + "/user/home");
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
}
