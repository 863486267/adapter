package com.saas.adapter.code.controllers.Tianyu;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ly
 *
 */
public class HttpPost{
	
	private HttpURLConnection openConnection ;
	private StringBuilder paremeter = new StringBuilder();
	private String charset = null;
	
	private Proxy proxy = null;
	 
	public Proxy getProxy() {
		return proxy;
	}
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}
	private Map<String, String> cookies = null;
	
	public Map<String, String> getCookies() {
		return cookies;
	}
	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	
	/**
	 *
	 * @throws IOException 如果这个报错就是连接根本没打开 
	 */
	public HttpPost(URL url,String charset) throws IOException{
		if(charset == null){
			charset = "utf-8";
		}else{
			this.charset = charset; 
		}
	   	 if ( proxy != null) {
		      openConnection =  (HttpURLConnection) url.openConnection(proxy);
		 } else {
			  openConnection =  (HttpURLConnection) url.openConnection();
		 }
    	openConnection.setDoOutput(true); 
    	openConnection.setDoInput(true); 
    	openConnection.setConnectTimeout(default_timeout); 
    	openConnection.setReadTimeout(default_timeout); 
	} 
	
	public static final Integer default_timeout = 30*1000;
	
	public void setConnectTimeout(Integer timeout) {
    	openConnection.setConnectTimeout(timeout); 
	}
	public void setReadTimeout(Integer timeout) {
    	openConnection.setReadTimeout(timeout); 
	}
	
	public void setTimeout(Integer timeout) {
    	openConnection.setConnectTimeout(timeout); 
    	openConnection.setReadTimeout(timeout);
	}
	
	  private boolean instanceFollowRedirects = true;

	  private boolean defaultReaParemeter = true;
	  
	public void setDefaultReaParemeter(boolean defaultReaParemeter) {
		    this.defaultReaParemeter = defaultReaParemeter;
	}

	public boolean isInstanceFollowRedirects() {
		    return instanceFollowRedirects;
	}

	public void setInstanceFollowRedirects(boolean instanceFollowRedirects) {
		    this.instanceFollowRedirects = instanceFollowRedirects;
	}
	/**
	 *
	 * @throws IOException 如果这个报错就是连接根本没打开 
	 */
	public HttpPost(URL url) throws IOException{
		this.charset = "utf-8"; 
	   	 if ( proxy != null) {
		      openConnection =  (HttpURLConnection) url.openConnection(proxy);
		 } else {
			  openConnection =  (HttpURLConnection) url.openConnection();
		 }
    	
    	openConnection.setDoOutput(true); 
    	openConnection.setDoInput(true); 
    	openConnection.setConnectTimeout(default_timeout); 
    	openConnection.setReadTimeout(default_timeout); 
	}
	
	public void addParemeter(String name,String value){ 
		if(name == null){
			paremeter.append(value).append("&"); 
		}else{
			paremeter.append(name).append("=").append(URLEncoder.encode(value)).append("&"); 
		}
	} 
	public void addRequestData(String value){ 
		if(value != null){
			paremeter.append(value).append(" "); 
		}
	} 
	public void setRequestProperty(String key,String value){ 
		openConnection.setRequestProperty(key, value);
	} 
	public void addParemeterEncode(String name, String value) {
		    if (value == null) {
		      value = "";
		    }
		    try {
				String encode = URLEncoder.encode(value,charset);
				 paremeter.append(name).append("=").append(encode).append("&");
			} catch (Exception e) {
			}
		    
		   
	}
	/**
	 *  
	 * @return post的返回值
	 * @throws IOException 获取结果报错
	 */
	public String getResult() throws IOException {
		OutputStreamWriter outputStream = null;
		InputStream inputStream = null;
		ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();
		if(paremeter.length() > 1){
			paremeter = paremeter.deleteCharAt(paremeter.length()-1);
		}
		
		StringBuilder sb = new StringBuilder();
		if(cookies != null){
			Set<String> keySet = cookies.keySet();
			for (String string : keySet) {
				String string2 = cookies.get(string);
				
				sb.append(string+"="+string2+"; ");
			}
		}
		
		System.out.println(paremeter);
		
		//System.out.println(paremeter.toString());
		byte[] returnByte = null;
		try {
			
		    openConnection.setRequestProperty("Cookie", sb.toString());
		    openConnection.setInstanceFollowRedirects(instanceFollowRedirects);
			outputStream = new OutputStreamWriter(openConnection.getOutputStream(), charset);   
	    	outputStream.write(paremeter.toString());  
	    	outputStream.flush(); 
	    	outputStream.close(); 
	    	//System.out.print(openConnection.getHeaderField("Set-Cookie"));
	    	
//	    	System.out.println(openConnection.getHeaderFields());
	    	
	    	if(this.cookies != null){
				Map<String, List<String>> headerFields = openConnection.getHeaderFields();
		    	
	    		List<String> list = headerFields.get("Set-Cookie");
	    		if(list != null){
	    			for (String string2 : list) {
	        			String[] substring = string2.substring(0,string2.indexOf(";")).split("=");
	        			cookies.put(substring[0], substring[1]);
	    			}
	    		}
			}
	    	
	    	
	    	inputStream = openConnection.getInputStream();
			int b;
			while (inputStream != null && (b = inputStream.read()) != -1) {
				outByteArray.write(b);
			} 
			returnByte = outByteArray.toByteArray();
		} catch (IOException e) {
			inputStream = ((HttpURLConnection)openConnection).getErrorStream();
			int b;
			while (inputStream != null && (b = inputStream.read()) != -1) {
				outByteArray.write(b);
			} 
			returnByte = outByteArray.toByteArray();
		}finally {
			outByteArray.close();
			if (outputStream != null) {
				outputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		 
	}
		return new String(returnByte,charset);
	}
}
