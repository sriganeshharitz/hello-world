package com.uttara.sem.action;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;

import sun.security.jca.GetInstance;

import com.alibaba.fastjson.JSON;
import com.opensymphony.xwork2.ActionContext;
import com.uttara.sem.listeners.JedisPoolInitializer;
import com.uttara.sem.DAO.LoginDAO;
import com.uttara.sem.common.Constants;
import com.uttara.sem.common.MailAction;
import com.uttara.sem.model.pojo.UserLoginBean;
import com.uttara.sem.service.EncryptDecryptService;
import com.uttara.sem.service.LoginService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class LoginAction extends BaseAction implements SessionAware,ServletRequestAware{

	private static final long serialVersionUID = -9064417072413057281L;
	private String classNameToLog = LoginAction.class.getName();
	protected static Logger logger = Logger.getLogger(LoginAction.class);
	private UserLoginBean lb = new UserLoginBean();
	private Map session;
	private HttpServletRequest request;
	LoginDAO ld=new LoginDAO(null);
	MailAction m=new MailAction();
	String mailSubject = "";
	String mailBody = "";
	String mailTo="";
	String ipCc="";
	boolean flag = false;
	String mailFailedEmail_id = null;

	
		
	public String execute() {
		System.out.println("Inside execute............................");		
		JedisPool pool = JedisPoolInitializer.getPool();
		String result=INPUT;
		LoginService service = new LoginService();
		String loginName = lb.getLoginName().trim();
		Pattern pattern = Pattern.compile("[A-Z]");
		boolean isUserCached = true;
		Jedis jedis = null;
		UserLoginBean res = null;
		
		EncryptDecryptService enService=new EncryptDecryptService(lb.getPassword());
		String plainPassword=lb.getPassword();
		lb.setPassword(enService.encrypt(plainPassword));
		
		try {
			jedis = pool.getResource();
			if((res=JSON.parseObject(jedis.get(lb.getLoginName()),UserLoginBean.class))==null){
			 logger.debug("User credentials aren't cached");
			 res = service.authenticate(lb);	
			 isUserCached = false;
			}
			}
			finally {
				if(jedis!=null)
					jedis.close();
			}
		if(isUserCached){
			logger.debug("Found cached user credentials!!");
		}
		
		//UserLoginBean res = service.authenticate(lb); sg
		//Date d=new Date();
		java.text.DateFormat dt=new java.text.SimpleDateFormat("dd-MMM-yyyy");
    	Date d=new Date();
    	System.out.println(dt.format(d));
    	
		boolean candAccntFlag = false; 
		session = ActionContext.getContext().getSession();
		if (res != null){
			if(!isUserCached){
				logger.debug("Caching user details");
				try{
					jedis = pool.getResource();
					jedis.set(res.getLoginName(), JSON.toJSONString(res));
				}
				finally{
					if(jedis!=null)
						jedis.close();
				}
			}
			String fname=res.getFirstName();
			String name=fname+res.getLastName();
			setUser(res);
			String role = res.getRole();
			logger.debug("inside LoginAction execute() role :" +  role);
			session = ActionContext.getContext().getSession();
/*			session.put("userInformation", res);*/			
			
			if (role.equalsIgnoreCase("admin")) {
					logger.debug("Inside role of admin check");
					logger.debug("inside LoginAction execute() admin role :" +  role);
					/* Session ID Regeneration: Try #4 
	        	    ((SessionMap)this.session).invalidate();
	        	    this.session = ActionContext.getContext().getSession();
	        	    End Try #4 */
	        	   
	        	   HttpSession sHttpSession = request.getSession(true); 
	        	   session.put(Constants.SESSION_ATTRIBUTES.AUTHENTICATED.name(), res);		
	        	   session.put("role", role);
	        	  
	        	  // logger.debug("login Id v_shastry@hotmail.com is logging form Ip addr :"+ipAddress);
	        	   result = "admin";
			} 
			
        	else if (role.equalsIgnoreCase("student") && res.getStatus().equals("active")) {
        		Map<Object, Object> userMap = new HashMap<Object, Object>();
				logger.debug("Inside role of student check");
				
				logger.debug("inside LoginAction execute() student role :" +  role);
				
				/* Session ID Regeneration: Try #4
        	    ((SessionMap)this.session).invalidate();
        	    this.session = ActionContext.getContext().getSession();
        	    End Try #4 */
        	    logger.debug("res --------- " + res);
        	   session.put(Constants.SESSION_ATTRIBUTES.AUTHENTICATED.name(), res);
        	   session.put("role", role);
        	   session.put("USER_SLNO", res.getSlno());
        	   session.put("userMap", userMap);
        	   session.put("batchNo", res.getBatchNo());
        	   result = "student";
		} else{
			addActionError(getText("accountinactive.err"));
        	   result = INPUT;
		}
		}
		else {
			addActionError(getText("unameOrPassword.invalid"));
		}
		return result;
	}

	public String flexLoginInfo(UserLoginBean uBean){
		logger.debug("email id: " + uBean.getEmailId());
		
		String result = null;
		
		lb = uBean;
		result = execute();
		
		return result;
	}
	
	
	public void setSession(Map session) {
		this.session = session;
	}
	
	public Map getSession() {
		return session;
	}
	
	public Object getModel() {
		logger.debug("tIndsie LoginAction getModel(): "+lb.getLoginName());
		return lb;
	}
	
	public void  validate() {
		logger.debug("inside LoginAction validate() :" + classNameToLog);
		if (lb!=null)
		{
			if (isInvalid(lb.getLoginName())) {
				logger.debug("login id no entry...");
				addFieldError("loginName", getText("loginName.required"));
			}
	
			if (isInvalid(lb.getPassword())) {
				logger.debug("Password, no entry...");
				addFieldError("password", getText("password.required"));
			}
		}	
	}

	 private boolean isInvalid(String value) {
	        return (value == null || value.length() == 0);
	    }


	
	public void setServletRequest(HttpServletRequest arg0) {
		this.request = arg0;
	}
	
	public HttpServletRequest getServletRequest() {
		return request;
	}
	}
	
