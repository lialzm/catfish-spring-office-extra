package com.catfish.support;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by A on 2017/3/30.
 */
public class CubeUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    protected static final String FORM_CHARSET = "UTF-8";

    /**
     * 根据方法名称取得反射方法的参数类型(没有考虑同名重载方法使用时注意)
     *
     * @param classInstance 类实例
     * @param methodName    方法名
     * @return
     * @throws ClassNotFoundException
     */
    public static Class[] getMethodParamTypes(Object classInstance, String methodName) throws ClassNotFoundException {
        Class[] paramTypes = null;
        Method[] methods = classInstance.getClass().getDeclaredMethods();// 全部方法
        for (int i = 0; i < methods.length; i++) {
            if (methodName.equals(methods[i].getName())) {// 和传入方法名匹配
                Class[] params = methods[i].getParameterTypes();
                paramTypes = new Class[params.length];
                for (int j = 0; j < params.length; j++) {
                    paramTypes[j] = Class.forName(params[j].getName());
                }
                break;
            }
        }
        return paramTypes;
    }


    public String request2HttpClient(HttpServletRequest request, String path) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000)
                .build();
        HttpClient httpClient = HttpClientBuilder.create().build();
        String method = request.getMethod();
        String result=null;
        if ("POST".equals(method)) {
            System.out.println(path);
            HttpPost httpPost = new HttpPost(path);
            httpPost.setConfig(requestConfig);
            addHeader(request, httpPost);
            HttpEntity stringEntity = null;
            InputStream inputStream = null;
            if (isFormPost(request)) {
                try {
                    inputStream = getBodyFromServletRequestParameters(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    stringEntity = new StringEntity(IOUtils.toString(inputStream, Charset.defaultCharset()), FORM_CHARSET);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    inputStream = request.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    stringEntity = new StringEntity(IOUtils.toString(inputStream, Charset.defaultCharset()), ContentType.APPLICATION_JSON);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpPost);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result=EntityUtils.toString(httpResponse.getEntity());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug(request.getRemoteHost());
            URI uri = null;
            try {
                URIBuilder uriBuilder = new URIBuilder();
                uri = addParameter(request, uriBuilder)
                        .setPath(path)
                        .build();

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            try {
                HttpGet httpget = new HttpGet(uri);
                httpget.setConfig(requestConfig);
                addHeader(request, httpget);
                HttpResponse httpResponse = httpClient.execute(httpget);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result=EntityUtils.toString(httpResponse.getEntity());
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private HttpRequestBase addHeader(HttpServletRequest request, HttpRequestBase requestBase) {
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            if (key.toLowerCase().equals(HTTP.CONTENT_LEN.toLowerCase())) {
                continue;
            }
            String value = request.getHeader(key);
            requestBase.setHeader(key, value);
        }
        return requestBase;
    }

    private URIBuilder addParameter(HttpServletRequest request, URIBuilder uriBuilder) {
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getParameter(key);
            uriBuilder.addParameter(key, value);
        }
        return uriBuilder;
    }


    private static boolean isFormPost(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
                HttpMethod.POST.matches(request.getMethod()));
    }

    private static InputStream getBodyFromServletRequestParameters(HttpServletRequest request) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        Writer writer = new OutputStreamWriter(bos, FORM_CHARSET);

        Map<String, String[]> form = request.getParameterMap();
        for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
            String name = nameIterator.next();
            List<String> values = Arrays.asList(form.get(name));
            for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                String value = valueIterator.next();
                writer.write(URLEncoder.encode(name, FORM_CHARSET));
                if (value != null) {
                    writer.write('=');
                    writer.write(URLEncoder.encode(value, FORM_CHARSET));
                    if (valueIterator.hasNext()) {
                        writer.write('&');
                    }
                }
            }
            if (nameIterator.hasNext()) {
                writer.append('&');
            }
        }
        writer.flush();

        return new ByteArrayInputStream(bos.toByteArray());
    }

}
