/**
 * 这个类负责处理用户操作分析报告相关的 HTTP 请求并调用 AnalysisReportService 来执行相应的业务逻辑。
 * 
 * @author 石振山
 * @version 4.5.1
 */
package com.ssvep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssvep.dto.AnalysisReportDto;
import com.ssvep.service.AnalysisReportService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@WebServlet("/analysisreport")
public class AnalysisReportController extends HttpServlet {
    private AnalysisReportService reportService;

    @Override
    public void init() throws ServletException {
        reportService = new AnalysisReportService();
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
            objectMapper.registerModule(new JavaTimeModule());

            if (idParam != null) {
                Long id = Long.valueOf(idParam);
                AnalysisReportDto reportDto = reportService.getReportById(id);

                if (reportDto != null) {
                    out.write(objectMapper.writeValueAsString(reportDto));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"error\":\"Report not found\"}");
                }
            } else {
                List<AnalysisReportDto> reportDto = reportService.getAllReports();
                out.write(objectMapper.writeValueAsString(reportDto));
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

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // 允许来自指定域的跨域请求
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

        String testRecordId = json.optString("test_id", "");
        String reportData = json.optString("reportData", "");
        String createdAt = json.optString("createdAt", "");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> reportMap = objectMapper.readValue(reportData, Map.class);

        AnalysisReportDto reportDto = new AnalysisReportDto();
        reportDto.setTestRecordId(Long.valueOf(testRecordId));
        reportDto.setReportData(reportMap);
        reportDto.setCreatedAt(LocalDateTime.parse(createdAt));

        try {
            reportService.createReport(reportDto);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"分析报告存储成功\"}");
        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"分析报告存储失败\"}");
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
        String testRecordId = json.optString("test_id", "");
        String reportData = json.optString("reportData", "");
        String createdAt = json.optString("createdAt", "");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> reportMap = objectMapper.readValue(reportData, Map.class);

        AnalysisReportDto reportDto = new AnalysisReportDto();
        reportDto.setReportId(Long.valueOf(id));
        reportDto.setTestRecordId(Long.valueOf(testRecordId));
        reportDto.setReportData(reportMap);
        reportDto.setCreatedAt(LocalDateTime.parse(createdAt));

        try {
            reportService.updateReport(reportDto);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"分析报告更新成功\"}");

        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"分析报告更新失败\"}");
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
            reportService.deleteReport(Id);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"分析报告删除成功\"}");

        } catch (Exception e) {
            e.printStackTrace();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write("{\"status\":\"error\",\"message\":\"分析报告删除失败\"}");
        }
    }

}
