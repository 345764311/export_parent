package com.itheima.web.controller;

import com.itheima.domain.system.User;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BaseController {
	@Autowired
	protected HttpServletRequest request;

	@Autowired
	protected HttpServletResponse response;

	@Autowired
	protected HttpSession session;


	protected String getCompanyId() {
		return getUser().getCompanyId();
	}

	protected String getCompanyName() {
		return getUser().getCompanyName();
	}

	protected User getUser() {
		User user = (User) session.getAttribute("loginUser");
		if (user == null) {
			throw new RuntimeException("session超时");
		}
		return user;
	}
}
