package com.xju.enrollment.common;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ExcelExportUtil {

    public static void export(HttpServletResponse response, String fileName, List<Map<String, Object>> data,
                               String[] headers, String[] keys) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        ExcelWriter writer = ExcelUtil.getWriter(true);
        for (int i = 0; i < headers.length; i++) {
            writer.addHeaderAlias(keys[i], headers[i]);
            writer.setColumnWidth(i, 20);
        }
        writer.write(data, true);
        writer.flush(response.getOutputStream());
        writer.close();
    }
}
