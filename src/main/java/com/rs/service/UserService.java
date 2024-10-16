package com.rs.service;

import com.rs.dao.UserDAO;
import com.rs.entity.User;
import com.rs.util.encrypt.PasswordUtil;
import com.rs.util.other.XCookie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserService {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private List<User> list;

    public UserService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        list = new ArrayList<>();
    }

    public void login() throws ServletException, IOException, SQLException {
        if (request.getMethod().equals("GET")) {
            XCookie xCookie = new XCookie(request, response);
            String id = xCookie.getValue("rememberMe");
            if (id != null && !id.isBlank()) {
                User user = UserDAO.getUserById(Integer.parseInt(id));
                request.getSession().setAttribute("currUser", user);
                response.sendRedirect(request.getContextPath() + "/user/home");
                return;
            }
            request.setAttribute("view", "/user/login.jsp");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        } else if (request.getMethod().equals("POST")) {
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
                                xCookie.create("rememberMe", user.getId() + "", 24 * 60 * 60);
                            }
                            response.sendRedirect(request.getContextPath() + "/user/home");
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

    public void list() throws SQLException {
        list = UserDAO.getAllUsers();
        request.setAttribute("list", list);
        request.setAttribute("path", "/admin/userList.jsp");
    }

    public void detail() throws SQLException, ClassNotFoundException {
        String uri = request.getServletPath();
        User user;
        if (uri.contains("edit")) {
            String id = request.getPathInfo().substring(1);
            user = UserDAO.getUserById(Integer.parseInt(id));
            request.setAttribute("item", user);
            request.setAttribute("action", "edit");

        } else if (uri.contains("blank")) {
            user = new User();
            user.setId(UserDAO.generateNewId());
            user.setRole(true);
            request.setAttribute("item", user);
            request.setAttribute("action", "create");
        }
        request.setAttribute("path", "/admin/userDetail.jsp");
    }

    public void runCrud() throws SQLException, ClassNotFoundException, IOException, InvocationTargetException, IllegalAccessException {
        DateTimeConverter dtc = new DateConverter(new Date());
        dtc.setPattern("MM/dd/yyyy");
        ConvertUtils.register(dtc, Date.class);
        User user = null;
        String uri = request.getServletPath();
        if (uri.contains("create")) {
            creatUser(user);
        } else if (uri.contains("update")) {
            updateUser(user);
        } else if (uri.contains("delete")) {
            deleteUser(user);
        } else if (uri.contains("reset")) {
            resetUser(user);
        }
        request.setAttribute("item", user);
    }

    private void creatUser(User user) throws InvocationTargetException, IllegalAccessException, SQLException, ClassNotFoundException {
        user = new User();
        BeanUtils.populate(user, request.getParameterMap());
        UserDAO.addUser(user);
        request.setAttribute("action", "edit");
    }

    private void updateUser(User user) throws SQLException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        user = UserDAO.getUserById(Integer.parseInt(request.getParameter("repId").substring(2)));
        BeanUtils.populate(user, request.getParameterMap());
        UserDAO.updateUser(user);
        request.setAttribute("action", "edit");
    }

    private void deleteUser(User user) throws SQLException, ClassNotFoundException {
        UserDAO.deleteUser(Integer.parseInt(request.getParameter("repId").substring(2)));
        user = new User();
        user.setId(UserDAO.generateNewId());
        user.setRole(false);
        request.setAttribute("action", "create");
    }

    private void resetUser(User user) throws SQLException, ClassNotFoundException {
        user = new User();
        user.setRole(false);
        user.setId(UserDAO.generateNewId());
        request.setAttribute("action", "create");
    }
}
