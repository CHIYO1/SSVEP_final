/**
 * 这个类负责处理检测记录相关的 HTTP 请求并调用 TestRecordService 来执行相应的业务逻辑。
 * 
 * @author 石振山
 * @version 5.3.1
 */
package com.ssvep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssvep.dto.TestRecordDto;
import com.ssvep.service.TestRecordService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/testrecords")
public class TestRecordController extends HttpServlet{
    private TestRecordService recordService;

    @Override
    public void init() throws ServletException {
        recordService = new TestRecordService();
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
        String userParam = json.optString("user_id", "");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            if (idParam != null) {
                Long id = Long.valueOf(idParam);
                TestRecordDto recordDto = recordService.getRecordById(id);

                if (recordDto != null) {
                    out.write(objectMapper.writeValueAsString(recordDto));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"error\":\"Record not found\"}");
                }
            } else if (userParam != null) {
                Long user_id = Long.valueOf(userParam);
                List<TestRecordDto> recordDto = recordService.getRecordsByUser(user_id);
                out.write(objectMapper.writeValueAsString(recordDto));
            } else {
                List<TestRecordDto> recordDto = recordService.getAllRecords();
                out.write(objectMapper.writeValueAsString(recordDto));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"An error occurred while processing the request\"}");
            }
        }
    }

    //new adding
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

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "https://localhost:5173");  // 允许来自指定域的跨域请求
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept, token, id, X-Custom-Header, X-Cookie, Connection, User-Agent, Cookie");
        resp.setHeader("Access-Control-Expose-Headers", "Authorization, X-Custom-Header");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

//        resp.setHeader("Access-Control-Allow-Origin", "*");
//        resp.setHeader("Access-Control-Allow-Methods", "*");
//        resp.setHeader("Access-Control-Max-Age", "3600");
//        resp.setHeader("Access-Control-Allow-Headers", "Authorization,Origin,X-Requested-With,Content-Type,Accept,"
//                + "content-Type,origin,x-requested-with,content-type,accept,authorization,token,id,X-Custom-Header,X-Cookie,Connection,User-Agent,Cookie,*");
//        resp.setHeader("Access-Control-Request-Headers",
//                "Authorization,Origin, X-Requested-With,content-Type,Accept");
//        resp.setHeader("Access-Control-Expose-Headers", "*");

        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String requestBody = jsonBuilder.toString();
        JSONObject json = new JSONObject(requestBody);

        String userId = json.optString("user_id", "");
        String testType = json.optString("test_type", "");
        String testDate = json.optString("test_date", "");
        String testResults = json.optString("test_results", "");
        String relatedInfo = json.optString("related_info", "");
        String stimulusVideoId = json.optString("video_id", "");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> ResultsMap = objectMapper.readValue(testResults, Map.class);

        TestRecordDto recordDto = new TestRecordDto();
        recordDto.setUserId(Long.valueOf(userId));
        recordDto.setTestType(testType);
        recordDto.setTestDate(LocalDate.parse(testDate));
        recordDto.setTestResults(ResultsMap);
        recordDto.setRelatedInfo(relatedInfo);
        recordDto.setStimulusVideoId(Long.valueOf(stimulusVideoId));

        try {
            recordService.createRecord(recordDto);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"检测记录存储成功\"}");
        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"检测记录存储失败\"}");
        }
    }

    @SuppressWarnings("unchecked")
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

        String id = json.optString("id", "");
        String userId = json.optString("user_id", "");
        String testType = json.optString("test_type", "");
        String testDate = json.optString("test_date", "");
        String testResults = json.optString("test_results", "");
        String relatedInfo = json.optString("related_info", "");
        String stimulusVideoId = json.optString("video_id", "");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> ResultsMap = objectMapper.readValue(testResults, Map.class);

        TestRecordDto recordDto = new TestRecordDto();
        recordDto.setRecordId(Long.valueOf(id));
        recordDto.setUserId(Long.valueOf(userId));
        recordDto.setTestType(testType);
        recordDto.setTestDate(LocalDate.parse(testDate));
        recordDto.setTestResults(ResultsMap);
        recordDto.setRelatedInfo(relatedInfo);
        recordDto.setStimulusVideoId(Long.valueOf(stimulusVideoId));

        try {
            recordService.updateRecord(recordDto);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"检测记录更新成功\"}");

        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"检测记录更新失败\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        String IdParam = json.optString("id", "");
        Long Id = Long.valueOf(IdParam);

        try {
            recordService.deleteRecord(Id);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"检测记录删除成功\"}");

        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"检测记录删除失败\"}");
        }
    }

}
