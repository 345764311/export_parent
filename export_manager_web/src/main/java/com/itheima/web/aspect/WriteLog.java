package com.itheima.web.aspect;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.domain.system.SysLog;
import com.itheima.service.system.SysLogService;
import com.itheima.web.controller.BaseController;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

/**
 * 记录日志
 */
@Aspect
@Component
public class WriteLog extends BaseController {

	@Reference
	private SysLogService sysLogService;

	/**
	 * 记录日志
	 */
	@Around("execution(* com.itheima.web.controller.*.*.*(..))")
	public Object log(ProceedingJoinPoint pjp) throws Throwable {

		//创建一个日志对象
		SysLog sysLog = new SysLog();
		sysLog.setId(UUID.randomUUID().toString());
		sysLog.setUserName(getUser().getUserName());
		sysLog.setIp(request.getRemoteAddr());
		sysLog.setTime(new Date());

		//这就是切点方法
		//MethodSignature  里面存储的就是当前切点方法的所有信息
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		//方法对象
		Method method = methodSignature.getMethod();
		sysLog.setMethod(method.getName());//方法名

		//如果方法上存在RequestMapping注解,就获取RequestMapping注解中name属性的值
		if (method.isAnnotationPresent(RequestMapping.class)) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			sysLog.setAction(requestMapping.name());
		}
		sysLog.setCompanyId(getCompanyId());
		sysLog.setCompanyName(getCompanyName());

		sysLogService.save(sysLog);

		//调用原方法逻辑
		return pjp.proceed();
	}

}