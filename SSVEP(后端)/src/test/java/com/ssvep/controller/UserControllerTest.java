/**
 * 这个文件用来测试 UserController 类是否能正确处理相应请求。
 * 使用了mockito模块模拟 HTTP 请求和响应以及service服务。
 * 
 * @author 石振山
 * @version 3.2.3
 */

package com.ssvep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssvep.dto.UserDto;
import com.ssvep.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserControllerTest {
//    private UserController userController;
//    private UserService userService;
//    private HttpServletRequest request;
//    private HttpServletResponse response;
//    private ByteArrayOutputStream outputStream;
//    private PrintWriter writer;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        userService = Mockito.mock(UserService.class);
//        userController = new UserController();
//
//        // 使用反射设置 UserController 的 userService 字段
//        userController.init();
//        Field userServiceField = UserController.class.getDeclaredField("userService");
//        userServiceField.setAccessible(true);
//        userServiceField.set(userController, userService);
//
//        // 捕获响应
//        outputStream = new ByteArrayOutputStream();
//        writer = new PrintWriter(outputStream);
//        response = Mockito.mock(HttpServletResponse.class);
//        when(response.getWriter()).thenReturn(writer);
//
//        request = Mockito.mock(HttpServletRequest.class);
//    }

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private ByteArrayOutputStream outputStream;
    private PrintWriter writer;

    @BeforeEach
    public void setUp() throws Exception {
        // 初始化 Mockito 注解
        MockitoAnnotations.openMocks(this);

        // 捕获响应
        outputStream = new ByteArrayOutputStream();
        writer = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testDoPost_Success() throws ServletException, IOException, SQLException {
        // 设置请求参数
        when(request.getParameter("username")).thenReturn("testUser");
        when(request.getParameter("password")).thenReturn("testPassword");
        when(request.getParameter("name")).thenReturn("Test User");
        when(request.getParameter("role")).thenReturn("USER");
        when(request.getParameter("action")).thenReturn("register");

        // 设置 UserService 的行为
        doNothing().when(userService).createUser(any(UserDto.class));

        userController.doPost(request, response);

        String expectedResponse = "{\"status\":\"success\",\"message\":\"用户创建成功\"}";
        writer.flush(); 
        assertEquals(expectedResponse, outputStream.toString());
    }

    @Test
    public void testDoPost_Failure() throws ServletException, IOException, SQLException {
        // 设置请求参数
        when(request.getParameter("username")).thenReturn("testUser");
        when(request.getParameter("password")).thenReturn("testPassword");
        when(request.getParameter("name")).thenReturn("Test User");
        when(request.getParameter("role")).thenReturn("USER");
        when(request.getParameter("action")).thenReturn("register");

        doThrow(new RuntimeException("创建用户失败")).when(userService).createUser(any(UserDto.class));

        userController.doPost(request, response);

        String expectedResponse = "{\"status\":\"error\",\"message\":\"创建用户失败\"}";
        writer.flush(); 
        assertEquals(expectedResponse, outputStream.toString());
    }

    @Test
    public void testDoGet_UserFound() throws ServletException, IOException {
        Long userId = 1L;
        UserDto mockUserDto = new UserDto();
        mockUserDto.setUserId(userId);
        mockUserDto.setUsername("testUser");
        mockUserDto.setName("Test User");

        when(request.getParameter("id")).thenReturn(String.valueOf(userId));
        when(userService.getUserById(userId)).thenReturn(mockUserDto);

        userController.doGet(request, response);

        String expectedResponse = new ObjectMapper().writeValueAsString(mockUserDto);
        writer.flush(); 
        assertEquals(expectedResponse, outputStream.toString());
    }

    @Test
    public void testDoGet_UserNotFound() throws ServletException, IOException {
        Long userId = 1L;

        when(request.getParameter("id")).thenReturn(String.valueOf(userId));
        when(userService.getUserById(userId)).thenReturn(null);

        userController.doGet(request, response);

        String expectedResponse = "{\"error\":\"User not found\"}";
        writer.flush(); 
        assertEquals(expectedResponse, outputStream.toString());
    }

    @Test
    public void testDoPut_Success() throws ServletException, IOException,SQLException {
        when(request.getParameter("userId")).thenReturn("1");
        when(request.getParameter("username")).thenReturn("testUser");
        when(request.getParameter("password")).thenReturn("newPassword");
        when(request.getParameter("name")).thenReturn("Updated User");
        when(request.getParameter("role")).thenReturn("USER");

        doNothing().when(userService).updateUser(any(UserDto.class));

        userController.doPut(request, response);

        String expectedResponse = "{\"status\":\"success\",\"message\":\"用户更新成功\"}";
        writer.flush(); 
        assertEquals(expectedResponse, outputStream.toString());
    }

    @Test
    public void testDoPut_Failure() throws ServletException, IOException,SQLException {
        when(request.getParameter("userId")).thenReturn("1");
        when(request.getParameter("username")).thenReturn("testUser");
        when(request.getParameter("password")).thenReturn("newPassword");
        when(request.getParameter("name")).thenReturn("Updated User");
        when(request.getParameter("role")).thenReturn("USER");

        doThrow(new RuntimeException("更新用户失败")).when(userService).updateUser(any(UserDto.class));

        userController.doPut(request, response);

        String expectedResponse = "{\"status\":\"error\",\"message\":\"更新用户失败\"}";
        writer.flush(); 
        assertEquals(expectedResponse, outputStream.toString());
    }
//    @Test
//    public void testDoDelete_Success() throws ServletException, IOException,SQLException {
//        when(request.getParameter("userId")).thenReturn("1");
//
//        doNothing().when(userService).deleteUser(anyLong());
//
//        userController.doDelete(request, response);
//
//        String expectedResponse = "{\"status\":\"success\",\"message\":\"用户删除成功\"}";
//        writer.flush();
//        assertEquals(expectedResponse, outputStream.toString());
//    }
//
//    @Test
//    public void testDoDelete_Failure() throws ServletException, IOException,SQLException {
//        when(request.getParameter("userId")).thenReturn("1");
//
//        doThrow(new RuntimeException("删除用户失败")).when(userService).deleteUser(anyLong());
//
//        userController.doDelete(request, response);
//
//        String expectedResponse = "{\"status\":\"error\",\"message\":\"删除用户失败\"}";
//        writer.flush();
//        assertEquals(expectedResponse, outputStream.toString());
//    }

//    //v2
//    public void testDoDelete_Success() throws ServletException, IOException, SQLException {
//        // 模拟 HttpServletRequest 的 getReader() 方法
//        String jsonBody = "{\"userId\":\"1\"}";
//        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));  // 创建一个模拟的 BufferedReader
//        when(request.getReader()).thenReturn(reader);  // 返回模拟的 reader
//
//        // 模拟删除用户服务
//        doNothing().when(userService).deleteUser(anyLong());
//
//        // 执行 doDelete 方法
//        userController.doDelete(request, response);
//
//        // 校验输出结果
//        String expectedResponse = "{\"status\":\"success\",\"message\":\"用户删除成功\"}";
//        writer.flush();
//        assertEquals(expectedResponse, outputStream.toString());
//    }

    @Test
    public void testDoDelete_Success() throws ServletException, IOException, SQLException {
        // 模拟 HttpServletRequest 的 getReader() 方法
        String jsonBody = "{\"userId\":\"1\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));  // 创建一个模拟的 BufferedReader
        when(request.getReader()).thenReturn(reader);  // 返回模拟的 reader

//        // 创建 JWT 令牌
//        String token = userService.generateToken("testUser");
//
//        // 模拟解析令牌返回的 claims
//        Claims mockClaims = userService.parseToken(token);  // 使用实际的 token 来解析
//        when(userService.parseToken(anyString())).thenReturn(mockClaims);
        Claims mockClaims = Jwts.claims().setSubject("testUser");  // 创建一个模拟的 Claims 对象
        when(userService.parseToken(anyString())).thenReturn(mockClaims);  // 确保返回有效的 Claims


        // 模拟删除用户服务
        doNothing().when(userService).deleteUser(anyLong());

        // 执行 doDelete 方法
        userController.doDelete(request, response);

        // 校验输出结果
        String expectedResponse = "{\"status\":\"success\",\"message\":\"用户删除成功\"}";
        writer.flush();
        assertEquals(expectedResponse, outputStream.toString());
    }

    @Test
    public void testDoDelete_Failure() throws ServletException, IOException, SQLException {
        // 模拟 HttpServletRequest 的 getReader() 方法
        String jsonBody = "{\"userId\":\"1\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));  // 创建一个模拟的 BufferedReader
        when(request.getReader()).thenReturn(reader);  // 返回模拟的 reader

        // 模拟删除用户服务抛出异常
        doThrow(new RuntimeException("删除用户失败")).when(userService).deleteUser(anyLong());

        // 执行 doDelete 方法
        userController.doDelete(request, response);

        // 校验输出结果
        String expectedResponse = "{\"status\":\"error\",\"message\":\"删除用户失败\"}";
        writer.flush();
        assertEquals(expectedResponse, outputStream.toString());
    }

}
