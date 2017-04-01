package com.catfish.controller;

import com.catfish.info.UserInfo;
import com.catfish.service.ExcelService;
import com.catfish.support.CubeUtil;
import com.catfish.support.CurrentUser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/excel")
public class ExcelController {

    @Autowired
    ExcelService excelService;

    private Logger logger = Logger.getLogger(getClass());

    @RequestMapping("/export2007")
    public String exportExcel2007(HttpServletRequest request, String controllerName, String methodName, String head,
                                  String replaceParams, String fileName, @CurrentUser UserInfo userInfo) {
        try {
            excelService.exportExcel2007(request, controllerName, methodName,
                    head, replaceParams, fileName, userInfo.getMail());
        } catch (Exception e) {
            logger.info("导出失败", e);
        }
        return "";
    }

    @RequestMapping("/export2007test")

    public String exportExcel2007test(HttpServletRequest request, String path) {
        logger.debug(path);
        String json = new CubeUtil().request2HttpClient(request, path);

        return "";
    }


}
