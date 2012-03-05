package org.nutz.mongo.session;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.types.Code;
import org.bson.types.ObjectId;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mongo.MongoDao;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.SessionProvider;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * MongoSession会话管理器,负责查询/生成MongoSession
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class MongoSessionManager implements SessionProvider {

	private static final Log log = Logs.get();
	private ManagerContext context;
	private Thread cleaner;

	public MongoSessionManager(final MongoDao dao) {
		this(dao, "http.session");
	}

	public MongoSessionManager(final MongoDao dao, final String colName) {
		context = new ManagerContext();
		context.setMongoDao(dao);
		context.setSessions(dao.getDB().getCollection(colName));
		context.setProvider(new SessionValueAdpter());
		DBCollection systemJs = dao.getDB().getCollection("system.js");
		systemJs.remove(new BasicDBObject("_id", "mongoSessionClean"));
		systemJs.insert(new BasicDBObject("_id", "mongoSessionClean").append("value", new Code(Files.read(Files.findFile("org/nutz/mongo/session/mongoSessionClean.js")))));
		cleaner = new Thread("MongoSessionCleaner") {
			public void run() {
				while (true) {
					try {
						context.getMongoDao().getDB().eval("mongoSessionClean()");
					} catch (Throwable e) {
						if (log.isWarnEnabled())
							log.warn("Clean case some error", e);
					}
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						break;
					} 
				}
			}
		};
		cleaner.start();
	}

	public void setProvider(SessionValueAdpter provider) {
		context.setProvider(provider);
	}

	public MongoSession getSession(String key) {
		DBObject dbo = context.getSessions().findOne(new BasicDBObject("_id", new ObjectId(
				key)), new BasicDBObject("_id", 1));
		if (dbo == null || dbo.get("_id") == null)
			return null;
		return new MongoSession(context, (ObjectId) dbo.get("_id"));
	}

	public MongoSession getSession(HttpServletRequest req) {
		return getSession(req, true);
	}

	public MongoSession getSession(HttpServletRequest req, boolean createNew) {
		String key = null;
		if (req.getCookies() != null)
			for (Cookie cookie : req.getCookies()) {
				if ("MongoSessionKey".equalsIgnoreCase(cookie.getName()))
					key = cookie.getValue();
			}
		MongoSession session = null;
		if (!Strings.isBlank(key)) {
			session = getSession(key);
			if (session != null
					&& req.getRemoteAddr().equals(
							session.getValue("remoteAddr"))
					&& req.getHeader("User-Agent").equals(
							session.getValue("userAgent"))) {
				session.touch();
				return session;
			}
		}
		if (!createNew)
			return null;
		Map<String, String> info = new HashMap<String, String>();
		info.put("remoteAddr", req.getRemoteAddr());
		info.put("userAgent", req.getHeader("User-Agent"));
		session = MongoSession.create(context, info);
		session.setNewCreate(true);
		return session;
	}

	public MongoHttpSession getHttpSession(HttpServletRequest req,
			ServletContext servletContext, boolean createNew) {
		MongoSession session = getSession(req, createNew);
		if (session != null) {
			MongoHttpSession httpSession = new MongoHttpSession(context,new ObjectId(session.getId()));
			if (servletContext == null)
				servletContext = req.getSession().getServletContext();
			httpSession.setServletContext(servletContext);
			httpSession.setNewCreate(session.isNew());
			return httpSession;
		}
		return null;
	}

	public HttpSession getHttpSession(HttpServletRequest req) {
		return getHttpSession(req, true);
	}

	public HttpSession getHttpSession(HttpServletRequest req, boolean createNew) {
		MongoHttpSession session = getHttpSession(req,Mvcs.getServletContext(), createNew);
		if (session != null && session.isNew()) {
			boolean flag = true;
			if (((HttpServletRequest) req).getCookies() != null)
				for (Cookie cookie : ((HttpServletRequest) req).getCookies()) {
					if ("MongoSessionKey".equalsIgnoreCase(cookie.getName())) {
						if (session.getId().equals(cookie.getValue())) {
							flag = false;
							break;
						}
					}
				}
			if (flag) {
				Cookie cookie = new Cookie("MongoSessionKey", session.getId());
				cookie.setMaxAge(30 * 60);
				Mvcs.getResp().addCookie(cookie);
			}
		}
		return session;
	}
}
