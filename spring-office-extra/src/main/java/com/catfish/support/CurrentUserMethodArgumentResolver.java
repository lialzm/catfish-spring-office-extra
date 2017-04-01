package com.catfish.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 获取当前用户的信息参数解析器
 * 
 * @author lcy
 *
 */
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest webRequest, WebDataBinderFactory dataBinderFactory) throws Exception {
		CurrentUser currentUserAnnotation = parameter.getParameterAnnotation(CurrentUser.class);
		HttpServletRequest servletWebRequest=	(HttpServletRequest) webRequest.getNativeRequest();
		HttpSession session = servletWebRequest.getSession();
		return session.getAttribute("userInfo");
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(CurrentUser.class)) {
			return true;
		} else {
			return false;
		}
	}

}
