package com.saas.adapter.code.controllers.Tianyu;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 默认timeout时间是30秒
 * @author lymava
 *
 */
public class HttpGet{
	
	private String urlStr ;
	private StringBuilder paremeter;
	private String charset = null;
	
	private HttpURLConnection openConnection ;
	
	private boolean defaultReaParemeter = true;
	
	public void setDefaultReaParemeter(boolean defaultReaParemeter) {
		this.defaultReaParemeter = defaultReaParemeter;
	}
	
	public void setRequestProperty(String name,String value) throws IOException{ 
		if(!this.isConnected){
			this.connect();
		}
		openConnection.setRequestProperty(name,value);
	} 
	
	private boolean instanceFollowRedirects = true;
	
	public boolean isInstanceFollowRedirects() {
		return instanceFollowRedirects;
	}
	public void setInstanceFollowRedirects(boolean instanceFollowRedirects) {
		this.instanceFollowRedirects = instanceFollowRedirects;
	}
	
	private Map<String, String> cookies = null;
	
	public Map<String, String> getCookies() {
		return cookies;
	}
	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	
	private boolean isConnected = false;
	
	public static final Integer timeout_default = 30*1000;
	
	private int readTimeout;
	private int connectTimeout;
	
	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void connect() throws IOException{
		
		if(paremeter != null && paremeter.length()>1){
			paremeter = paremeter.deleteCharAt(paremeter.length()-1);
			this.urlStr = this.urlStr +"?"+paremeter;
		}
		
		URL url = new URL(this.urlStr);
		openConnection =  (HttpURLConnection) url.openConnection();
		openConnection.setInstanceFollowRedirects(instanceFollowRedirects);
	   
		isConnected = true;
		
		openConnection.setReadTimeout(readTimeout);
		openConnection.setConnectTimeout(connectTimeout);
	}
	
	private Map<String, List<String>> headerFieldsReturn = null;
	  
	  public Map<String, List<String>> getHeaderFieldsReturn() {
		return headerFieldsReturn;
		}
		
		public void setHeaderFieldsReturn(Map<String, List<String>> headerFieldsReturn) {
			this.headerFieldsReturn = headerFieldsReturn;
		}
		/**
		 * 
		 * @param urlStr url地址
		 * @throws IOException 如果这个报错就是连接根本没打开 
		 */
		public HttpGet(String urlStr) throws IOException{
			charset = "utf-8";
			
			this.urlStr = urlStr;
			paremeter = new StringBuilder();
			
			readTimeout = timeout_default;
			connectTimeout =  timeout_default;
		}
	/**
	 * 
	 * @param urlStr url地址
	 * @throws IOException 如果这个报错就是连接根本没打开 
	 */
	public HttpGet(String urlStr,String charset) throws IOException{
		if(charset == null){
			charset = "utf-8";
		}else{
			this.charset = charset; 
		}
		this.urlStr = urlStr;
		paremeter = new StringBuilder();
		
		readTimeout = timeout_default;
		connectTimeout =  timeout_default;
	}
	
	public String getResult() throws IOException{
		
		if(!this.isConnected){
			this.connect();
		}
		
		InputStream inputStream = null;
		ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();
		
		if(defaultReaParemeter){
			openConnection.setRequestProperty("Accept","text/plain, */*");
			openConnection.setRequestProperty("Accept-Encoding","deflate");
			openConnection.setRequestProperty("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			openConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			openConnection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)");
		}
		
		StringBuilder sb_cookies = new StringBuilder();
	      if (cookies != null) {
	        Set<String> keySet = cookies.keySet();
	        for (String string : keySet) {
	          String string2 = cookies.get(string);
	          if (string2 != null) {
	            sb_cookies.append(string + "=" + string2 + "; ");
	          }
	        }
	      } 
	      openConnection.setRequestProperty("Cookie", sb_cookies.toString());
		byte[] returnByte = null;
		try{
			  inputStream = openConnection.getInputStream();
			
			  headerFieldsReturn = openConnection.getHeaderFields();
		      
		      List<String> list = headerFieldsReturn.get("Set-Cookie");
		      if (list != null && cookies != null) {
		        for (String string2 : list) {
		          String[] substring = string2.substring(0, string2.indexOf(";")).split("=");
		          if (substring.length == 1) {
		            cookies.put(substring[0], "");
		          } else {
		            cookies.put(substring[0], substring[1]);
		          }
		        }
		      }
			
			int b;
			while (inputStream != null && (b = inputStream.read()) != -1) {
				outByteArray.write(b);
			} 
			returnByte = outByteArray.toByteArray();
			
			openConnection.disconnect();
			inputStream.close();
		}catch (Exception e) {
			InputStream errorStream = openConnection.getErrorStream();
			
			int b;
			while (errorStream != null && (b = errorStream.read()) != -1) {
				outByteArray.write(b);
			} 
			returnByte = outByteArray.toByteArray();
		}
		return new String(returnByte,charset);
	}
	public void addParemeterNotEncode(String name,String value){ 
		paremeter.append(name).append("=").append(value).append("&");
	} 
	public void addParemeterEncode(String name,String value){ 
		paremeter.append(URLEncoder.encode(name)).append("=").append(URLEncoder.encode(value)).append("&");
	}
	public void addParemeter(String name,String value){ 
		this.addParemeterEncode(name, value);
	} 
}
