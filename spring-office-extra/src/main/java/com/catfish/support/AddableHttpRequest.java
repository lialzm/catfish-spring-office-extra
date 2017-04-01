package com.catfish.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 可以修改参数的httpRequest Created by lcy on 16/11/19.
 */
public class AddableHttpRequest extends HttpServletRequestWrapper {

	private HashMap<String,String[]> params = new HashMap();

	public AddableHttpRequest(HttpServletRequest request) {
		super(request);
		params.putAll(request.getParameterMap());
	}

	@Override
	public String getParameter(String name) {
		return  params.get(name)==null||params.get(name).length==0?null:params.get(name)[0];
	}

	@Override
	public String[] getParameterValues(String name) {
		Object object = params.get(name);
		if (object instanceof String[]) {
			return (String[]) object;
		} else {
			if (object == null) {
				return null;
			}
			return new String[] { object.toString() };
		}
	}

	public Object getParamValue(String name) {
		return params.get(name);
	}

	public void addParameter(String name, String value) {
		params.put(name, new String[]{value});
	}

	public void addParameters(Map<String, String[]> map) {
		params.putAll(map);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}

}
