/**
 * 这个类负责处理用户相关的 HTTP 请求并调用 UserService 来执行相应的业务逻辑。
 * 
 * @author 石振山
 * @version 4.3.1
 */
package com.ssvep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssvep.dto.UserDto;
import com.ssvep.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ssvep.util.PermissionVerification;
import io.jsonwebtoken.Claims;
import org.json.JSONObject;

@WebServlet("/users")
public class UserController extends HttpServlet {
    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "*");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization,Origin,X-Requested-With,Content-Type,Accept,"
                + "content-Type,origin,x-requested-with,content-type,accept,authorization,token,id,X-Custom-Header,X-Cookie,Connection,User-Agent,Cookie,*");
        resp.setHeader("Access-Control-Request-Headers",
                "Authorization,Origin, X-Requested-With,content-Type,Accept");
        resp.setHeader("Access-Control-Expose-Headers", "*");

        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String requestBody = jsonBuilder.toString();
        JSONObject json = new JSONObject(requestBody);

        String idParam = json.optString("id", "");
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            ObjectMapper objectMapper = new ObjectMapper();

            if (idParam != null) {
                Long id = Long.valueOf(idParam);
                UserDto userDto = userService.getUserById(id);

                if (userDto != null) {
                    out.write(objectMapper.writeValueAsString(userDto));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"error\":\"User not found\"}");
                }
            } else {
                List<UserDto> users = userService.getAllUsers();
                out.write(objectMapper.writeValueAsString(users));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"An error occurred while processing the request\"}");
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 处理预检请求，返回允许的跨域设置
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept, token, id, X-Custom-Header, X-Cookie, Connection, User-Agent, Cookie");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Expose-Headers", "Authorization, X-Custom-Header");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setStatus(HttpServletResponse.SC_OK);  // 200 OK
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "https://localhost:5173");  // 允许来自指定域的跨域请求
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept, token, id, X-Custom-Header, X-Cookie, Connection, User-Agent, Cookie");
        resp.setHeader("Access-Control-Expose-Headers", "Authorization, X-Custom-Header");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

//        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
//        resp.setHeader("Access-Control-Allow-Methods", "*");
//        resp.setHeader("Access-Control-Max-Age", "3600");
//        resp.setHeader("Access-Control-Allow-Headers", "Authorization,Origin,X-Requested-With,Content-Type,Accept,"
//                + "content-Type,origin,x-requested-with,content-type,accept,authorization,token,id,X-Custom-Header,X-Cookie,Connection,User-Agent,Cookie,*");
//        resp.setHeader("Access-Control-Request-Headers",
//                "Authorization,Origin, X-Requested-With,content-Type,Accept");
//        resp.setHeader("Access-Control-Expose-Headers", "*");
//        resp.setHeader("Access-Control-Allow-Credentials", "true");

        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String requestBody = jsonBuilder.toString();
        JSONObject json = new JSONObject(requestBody);

        // 从 JSON 数据中获取参数
        String action = json.optString("action", "");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if ("register".equalsIgnoreCase(action)) {

            String username = json.optString("username", "");
            String password = json.optString("password", "");
            String name = json.optString("name", "");
            String roleParam = "USER";
            UserDto.Role role = UserDto.Role.valueOf(roleParam.toUpperCase());

            UserDto userDto = new UserDto();
            userDto.setUsername(username);
            userDto.setPassword(password);
            userDto.setName(name);
            userDto.setRole(role);

            try {
                userService.createUser(userDto);
                resp.getWriter().write("{\"status\":\"success\",\"message\":\"用户创建成功\"}");

            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"创建用户失败\"}");
            }

        } else if ("login".equalsIgnoreCase(action)) {
            // 处理登录逻辑
            String username = json.optString("username", "");
            String password = json.optString("password", "");

            try {
                boolean authenticated = userService.authenticate(username, password);

                if (authenticated) {
                    String jwtToken = userService.generateToken(username);

                    // 返回JSON响应，包括JWT令牌
                    resp.getWriter()
                            .write("{\"status\":\"success\",\"message\":\"登录成功\",\"token\":\"" + jwtToken
                                    + "\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"用户名或密码错误\"}");
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"登录失败\"}");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "*");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization,Origin,X-Requested-With,Content-Type,Accept,"
                + "content-Type,origin,x-requested-with,content-type,accept,authorization,token,id,X-Custom-Header,X-Cookie,Connection,User-Agent,Cookie,*");
        resp.setHeader("Access-Control-Request-Headers",
                "Authorization,Origin, X-Requested-With,content-Type,Accept");
        resp.setHeader("Access-Control-Expose-Headers", "*");

        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String requestBody = jsonBuilder.toString();
        JSONObject json = new JSONObject(requestBody);

        String idParam = json.optString("userId", "");
        Long userId = Long.valueOf(idParam);

        String username = json.optString("username", "");
        String password = json.optString("password", "");
        String name = json.optString("name", "");
        String roleParam = json.optString("roleParam", "");
        UserDto.Role role = UserDto.Role.valueOf(roleParam.toUpperCase());

        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setUsername(username);
        userDto.setPassword(password);
        userDto.setName(name);
        userDto.setRole(role);

        if (PermissionVerification.verifiedByID(userId)){
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"您无权执行该操作\"}");
        }
        else {
            try {
                userService.updateUser(userDto);

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");

                resp.getWriter().write("{\"status\":\"success\",\"message\":\"用户更新成功\"}");

            } catch (Exception e) {
                e.printStackTrace();

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");

                resp.getWriter().write("{\"status\":\"error\",\"message\":\"更新用户失败\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "*");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization,Origin,X-Requested-With,Content-Type,Accept,"
                + "content-Type,origin,x-requested-with,content-type,accept,authorization,token,id,X-Custom-Header,X-Cookie,Connection,User-Agent,Cookie,*");
        resp.setHeader("Access-Control-Request-Headers",
                "Authorization,Origin, X-Requested-With,content-Type,Accept");
        resp.setHeader("Access-Control-Expose-Headers", "*");


        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String requestBody = jsonBuilder.toString();
        JSONObject json = new JSONObject(requestBody);

        String idParam = json.optString("userId", "");
        Long userId = Long.valueOf(idParam);

        try {
            userService.deleteUser(userId);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"用户删除成功\"}");

        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"删除用户失败\"}");
        }
    }
}
