package com.catfish.service.impl;

import com.catfish.service.ExcelHandleInterface;
import com.catfish.service.ExcelService;
import com.catfish.service.SheetService;
import com.catfish.support.AddableHttpRequest;
import com.catfish.support.CubeUtil;
import com.catfish.support.MailUtil;
import com.catfish.support.SpringContextHolder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;


@Service
public class ExcelServiceImpl implements ExcelService {

    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    MailUtil mailUtil;

    @Autowired
    SheetService sheetService;

    @Autowired
    SpringContextHolder contextHolder;

//    @Autowired
//    RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private int pageSize = 1000;// 查询时每页大小

    int rowAccessWindowSize = 1000; // 内存中保留的行数，超出后会写到磁盘
    int perSheetRows = 100000; // 每个sheet 10w条

    /**
     * 导出文件的最大大小 超过这个大小会压缩
     */
    private final int MAX_EXPORT_FILE_SIZE = 10 * 1024 * 1024; // 10MB

//    @Value("#{SERVER_IP}")
    String SERVER_IP;

    @Async
    @Override
    public String improtExcel(String filename, InputStream inputStream, ExcelHandleInterface handleInterface,
                              String message, String mailAddress) {
        String img_host_img_path =SERVER_IP;
        logger.info(filename);
        String[] array = filename.split("\\.");
        // 校验格式
        if (!"xls".equals(array[array.length - 1].toString()) && !"xlsx".equals(array[array.length - 1].toString())) {
            // resultJsonObject.accumulate("status", "3");
            // resultJsonObject.accumulate("message", "文件类型错误，请上传excel文件！");
            // return resultJsonObject.toString();
            return "文件类型错误，请上传excel文件！";
        } else {
            String name = "1111" + "." + array[array.length - 1];
            String path = img_host_img_path + File.separator + "cardHuaTian" + File.separator + name;
            logger.info("path:" + path);
            FileInputStream fileInputStream = null;
            File file = new File(path);
            // 上传
            try {
                // 保存文件
                FileUtils.copyInputStreamToFile(inputStream, file);
                fileInputStream = new FileInputStream(file);
                String suffix = path.substring(path.lastIndexOf(".")); // 文件后辍.
                Workbook workbook = null;
                if (".xls".equals(suffix)) {
                    workbook = new HSSFWorkbook(fileInputStream); // Excel
                } else if (".xlsx".equals(suffix)) {
                    workbook = new XSSFWorkbook(fileInputStream); // Excel
                }
                logger.info("文件上传成功");
                Sheet sheet = workbook.getSheetAt(0);
                long beginTime = System.currentTimeMillis();
                try {
                    sheetService.sheetHandle(handleInterface, sheet);
                } catch (Exception e) {
                    String msg = e.getMessage();
                    logger.info(msg);
                    logger.error("处理失败", e);
                    mailUtil.sendMail("任务失败通知", msg, mailAddress);
                    return "";
                }
                long endTime = System.currentTimeMillis();
                long time = (endTime - beginTime) / 1000;
                logger.info("上传处理耗时:" + time);
                logger.info("文件处理成功");
                mailUtil.sendMail("任务成功通知", message +
                        ",总条数" + sheet.getLastRowNum() + ",耗时" + time + "秒", mailAddress);
                return "处理成功";
            } catch (IOException e) {
                logger.error("上传文件发生异常：" + e);
                mailUtil.sendMail("任务失败通知", "上传文件发生异常", mailAddress);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
                FileUtils.deleteQuietly(file);
            }
        }
        return "其他错误";
    }

    private Object getController(String controllerName) {
        return contextHolder.getBean(controllerName);
    }

    private Class[] getMethodParamTypes(Object object, String methodName) {
        Class clazz = object.getClass();
        int clazzIndex = clazz.getName().indexOf("$");
        String className = clazz.getName().substring(0, clazzIndex);
        // 通过类名获取类用于获取方法参数名
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        int index = 0;
        // 获取全部方法名
        Method[] methods = c.getDeclaredMethods();
        // 获取指定方法位置
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (methodName.equals(method.getName())) {
                index = i;
            }
        }
        // 获取方法参数类型
        Class[] methodParamTypes = null;
        try {
            methodParamTypes = CubeUtil.getMethodParamTypes(object, methodName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return methodParamTypes;
    }

    /**
     * 获取spring代理的方法
     *
     * @param controllerName
     * @param methodName
     * @return
     */
    private Method getSpringMethod(String controllerName, String methodName) {
        // 反射获取controller
        // 获取spring实例化的类
        Object object = getController(controllerName);
        Class clazz = object.getClass();
        Class[] methodParamTypes = getMethodParamTypes(object, methodName);
        // 获取方法
        Method m1 = null;
        try {
            m1 = clazz.getDeclaredMethod(methodName, methodParamTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return m1;
    }

    private Method getMethod(String controllerName, String methodName) {
        Object object = getController(controllerName);
        Class clazz = object.getClass();
        int clazzIndex = clazz.getName().indexOf("$");
        String className = clazz.getName().substring(0, clazzIndex);
        // 通过类名获取类
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Class[] parameterTypes = getMethodParamTypes(object, methodName);
        try {
            return c.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    /**
     * 调用controller方法
     */
    private JSONObject invokeController(String controllerName, String methodName,
                                        HttpServletRequest request, Integer start, Integer limit) {



        Object object = getController(controllerName);
        Class[] methodParamTypes = getMethodParamTypes(object, methodName);
        Method m1 = getSpringMethod(controllerName, methodName);
        Method m2 = getMethod(controllerName, methodName);
        AddableHttpRequest addableHttpRequest = (AddableHttpRequest) request;

        addableHttpRequest.addParameter("start", String.valueOf(start));
        addableHttpRequest.addParameter("limit", String.valueOf(limit));

        Object[] objects = new Object[methodParamTypes.length];
        // 将参数放入方法,模仿springmvc对参数进行处理
        for (int i = 0; i < methodParamTypes.length; i++) {
            objects[i] = resolveArgument(addableHttpRequest, m2, i);
        }
        Object result = "";
        try {
            result = m1.invoke(object, objects);
            System.out.println("result==" + result);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        JSONObject output;
        output = JSONObject.fromObject(result);
        return output;
    }

    private void excelHeadHandle(JSONObject jsonHead, SXSSFSheet sheet1) {
        SXSSFRow row = sheet1.createRow(0);
        JSONArray names = jsonHead.names();
        int headerSize = names.size();
        // 插入头部
        for (int i = 0; i < headerSize; i++) {
            SXSSFCell cell = row.createCell(i);
            row.setHeightInPoints(20);
            String headName = names.optString(i);
            if (jsonHead.has(headName)) {
                headName = jsonHead.getString(names.optString(i));
            }
            cell.setCellValue(headName);
        }
    }

    @Async
    @Override
    public void exportExcel2007(HttpServletRequest request, String controllerName, String methodName, String head,
                                String replaceParams, String fileName, String mailAddress) throws Exception {
        // 开始页数
        int start = 0;
        JSONObject output = invokeController(controllerName, methodName, request, start, pageSize);
        logger.info(output);
        // 获取总条数
        Integer count = 0;
        if (output.has("count")) {
            count = output.getInt("count");
        }
        logger.info(count);
        String filePath = SERVER_IP + File.separator + fileName;
        File file = new File(filePath);
        SXSSFWorkbook wb = new SXSSFWorkbook(rowAccessWindowSize);
        wb.setCompressTempFiles(true);// 生成的临时文件将进行gzip压缩
        try {
            head = URLDecoder.decode(head, "UTF-8");
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        JSONObject jsonHead = JSONObject.fromObject(head);
        JSONArray names = jsonHead.names();
        int headerSize = names.size();
        try {
            replaceParams = URLDecoder.decode(replaceParams, "UTF-8");
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        JSONObject jsonReplaceParams = JSONObject.fromObject(
                replaceParams);
        BufferedOutputStream out = null;
        // 记录数据总行数
        Integer allRowNum = 0;
        JSONArray jsonArray = output.getJSONArray("data");
        while (true) {
            // 记录单sheet行数
            int sheetRowNum = 0;
            SXSSFSheet sheet1 = wb.createSheet();
            // 插入头部
            excelHeadHandle(jsonHead, sheet1);
            sheetRowNum++;
            logger.info("length==" + jsonArray.size());
            // 插入详细数据
            while (sheetRowNum < perSheetRows && allRowNum < count) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    SXSSFRow contentRow = sheet1.createRow(sheetRowNum);
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    for (int j = 0; j < headerSize; j++) {
                        SXSSFCell contentCell = contentRow.createCell(j);
                        String key = names.optString(j);
                        String value = jsonObject.optString(key);
                        // 替换值
                        if (jsonReplaceParams.has(key)) {
                            JSONObject jsonObject2 = jsonReplaceParams.getJSONObject(key);
                            if (jsonObject2.has(value)) {
                                value = jsonObject2.getString(value);
                            } else {
                                value = jsonObject2.optString("@other");
                            }
                        }
                        contentCell.setCellValue(value);
                    }
                    sheetRowNum++;
                    allRowNum++;
                }
                logger.info("while==" + sheetRowNum);
                // json处理完毕,重新请求数据
                logger.info("start==" + start);
                start++;
                // 重新请求
                output = invokeController(controllerName, methodName, request, start * pageSize, pageSize);
                jsonArray = output.getJSONArray("data");
            }
            logger.info("allRowNum==" + allRowNum + "," + count);
            if (allRowNum >= count) {
                break;
            }
        }
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            wb.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //清除临时文件
            wb.dispose();
        }
        IOUtils.closeQuietly(out);
        // TODO 参数返回处理器

        // 压缩

        // 成功发送邮件
        mailUtil.sendMail("导出成功通知",
                "下载地址:" + SERVER_IP + "/CubeImages/file/" + fileName, mailAddress);
    }


    private Object resolveArgument(HttpServletRequest request, Method m1, Integer index) {
        // 匹配参数转换器
       /* MethodParameter parameter = new MethodParameter(m1, index);
        parameter.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
        WebDataBinderFactory binderFactory = new DefaultDataBinderFactory(
                requestMappingHandlerAdapter.getWebBindingInitializer());
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        //旧版本
        *//*try {
			Object object = requestMappingHandlerAdapter.getArgumentResolvers().resolveArgument(parameter, mavContainer,
					new ServletWebRequest(request), binderFactory);
			return object;
		} catch (Exception e) {
			logger.error("resolveArgument", e);
			// 不处理
		}*//*

        List<HandlerMethodArgumentResolver> list = requestMappingHandlerAdapter.getArgumentResolvers();
        for (HandlerMethodArgumentResolver argumentResolver : list
                ) {
            if (argumentResolver.supportsParameter(parameter)) {
                try {
                    return argumentResolver.resolveArgument(parameter, mavContainer, new ServletWebRequest(request), binderFactory);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/
        return null;
    }

}
