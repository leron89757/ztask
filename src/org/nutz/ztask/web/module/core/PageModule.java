package org.nutz.ztask.web.module.core;

import javax.servlet.http.HttpSession;

import org.nutz.ioc.annotation.InjectName;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.filter.CheckSession;
import org.nutz.web.Webs;
import org.nutz.ztask.api.User;

@Filters(@By(type = CheckSession.class, args = {Webs.ME, "/page/login"}))
@InjectName
@IocBean
@Fail(">>:/e500.html")
public class PageModule {

	/**
	 * 自动决定，重定向到哪个视图
	 * 
	 * @param sess
	 *            会话对象
	 * 
	 * @return 重定向的 URL
	 */
	@Filters
	@At("/")
	@Ok(">>:/${obj}")
	public String autoDispatchRoot(HttpSession sess) {
		User u = (User) sess.getAttribute(Webs.ME);
		if (null == u) {
			return "page/login";
		}
		return "page/stack";
	}

	/**
	 * 登录界面
	 */
	@Filters
	@At("/page/login")
	@Ok("jsp:jsp.login")
	public void showLogin() {}

	/**
	 * 系统界面
	 */
	@At("/page/sys")
	@Ok("jsp:jsp.sys")
	public void showSystemConfiguration() {}

	/**
	 * 堆栈界面
	 */
	@At("/page/stack")
	@Ok("jsp:jsp.stack")
	public void showMain() {}

}
