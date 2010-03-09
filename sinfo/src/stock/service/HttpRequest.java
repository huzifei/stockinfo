package stock.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpRequest {
	
	Log log = LogFactory.getLog(this.getClass().getName());
	private HashMap<String,String> cookieHash =  new HashMap<String,String>();
	private String httpMethod, httpUrl;
	private String httpPostData = null; // It is used in post method, wiht get postdata from http process parsing manually
	
	public HttpRequest() {
		super();
	}
	public HttpRequest(String httpUrl, String httpMethod, HashMap<String, String> cookieHash, String httpPostData) {
		super();
		this.cookieHash = cookieHash;
		this.httpMethod = httpMethod;
		this.httpUrl = httpUrl;
		this.httpPostData = httpPostData;
	}
	
	public HashMap<String, String> getCookieHash() {
		return cookieHash;
	}

	public void setCookieHash(HashMap<String, String> cookieHash) {
		this.cookieHash = cookieHash;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getHttpUrl() {
		return httpUrl;
	}

	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	public String getHttpPostData() {
		return httpPostData;
	}

	public void setHttpPostData(String httpPostData) {
		this.httpPostData = httpPostData;
	}

	public String readRemotePage()
	{
		String httpContents = null;
		
	        log.debug("--------Transfer Start------------------------------------------------------------------");
	        log.info("readRemotePage Url："+httpUrl);
	        log.debug("--------------------------------------------------------------------------------------");
	       
	        HttpClient httpClient = new HttpClient();
	        /*
	        httpClient.getHostConfiguration().setProxy("210.77.83.71", 8900);  
	        httpClient.getParams().setAuthenticationPreemptive(true);  
	        httpClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials("stlib","stlib.cn"));  
	       */
	        if(httpMethod.equals("GET")) {
	            GetMethod getHC = new GetMethod(httpUrl);
	            getHC.getParams().setHttpElementCharset("UTF-8");
	            getHC.setFollowRedirects(false);
	           
	            //发送Cookie
	            StringBuilder cookieValue = new StringBuilder();
	            if (cookieHash != null) {
	            	for(String key : cookieHash.keySet()){
	            		cookieValue.append(key);
	            		cookieValue.append("=");
	            		cookieValue.append(cookieHash.get(key));
	            		cookieValue.append("; ");
	            	}
	            	if (cookieValue.length() > 0)
	            		getHC.setRequestHeader("Cookie", cookieValue.toString());
	            }
	            getHC.setRequestHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; User-agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; http://bsalsa.com) ; .NET CLR 2.0.50727)");
	            getHC.setRequestHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
	            getHC.setRequestHeader("Accept-Language", "zh-cn");
	            getHC.setRequestHeader("Connection", "Keep-Alive");
	            
	            int statusCode;
	            try {
	                statusCode = httpClient.executeMethod(getHC);
	                Header[] headers = getHC.getResponseHeaders();
	                for(Header header : headers){
	                	  checkCookie(header);
	                }
	               
	                if(statusCode==HttpStatus.SC_OK) {
	                    String charset = "gb2312"; //"utf-8";
	                    InputStreamReader isr = new InputStreamReader(getHC.getResponseBodyAsStream(), charset); // 设置读取流的编码格式，自定义编码           
	                    // 使用字符读取方式，循环读取源文件内容       
	                    StringBuffer sb = new StringBuffer();
	                    int b ;
	                    while ((b = isr.read()) != -1)//顺序读取文件text里的内容并赋值给整型变量b,直到文件结束为止。
	                    {
	                        if (b < 32 && b!= 10 && b != 13 && b != 9) b = 32;//过滤掉一些换行等符号
	                        //if ( b== 10 || b== 13 || b== 9) b = 32;//过滤掉一些换行等符号
	                        sb.append((char)b);
	                    }
	                    isr.close();
	                    getHC.abort();
	                    // 取得采集的内容
	                    httpContents  = sb.toString();
	                   
	                    // try to write result into disk
		       			debug_write_file("d:\\temp\\debugRequest.html", httpContents);
	                   
	                } else {
	                    if(statusCode == 301 || statusCode == 302) {                       
	                        Header locationHeader = getHC.getResponseHeader("location");
	                        httpUrl = locationHeader.getValue();
	                        httpContents = readRemotePage();//重新请求新网页
	                    }
	                }
	            } catch (HttpException e2) {
	                e2.printStackTrace();
	            } catch (IOException e2) {
	                e2.printStackTrace();
	            }
	        } else {
	        	PostMethod postHC = new PostMethod(httpUrl);
	           
	            //发送Cookie
	            StringBuilder cookieValue = new StringBuilder();
	            String tmpStr = "";
	            if (cookieHash != null) {
	            	for(String key : cookieHash.keySet()){
	            		if (key.equalsIgnoreCase("VIPCSID")) {
	            			tmpStr = cookieHash.get(key);
	            		}
	            		cookieValue.append(key);
	            		cookieValue.append("=");
	            		cookieValue.append(cookieHash.get(key));
	            		cookieValue.append("; ");
	            	}
	            	if (cookieValue.length() > 0) {
	            		postHC.setRequestHeader("Cookie", cookieValue.toString());
	            		// System.out.println("send Cookie = "+cookieValue.toString());
	            	}
	            }
	            postHC.setRequestHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; User-agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; http://bsalsa.com) ; .NET CLR 2.0.50727)");
	            postHC.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	            postHC.setRequestHeader("Accept-Language", "zh-cn");
	            postHC.setRequestHeader("Connection", "Keep-Alive");
	            
	            //post数据到服务器
	           String newpostdata = "";
	            if(httpPostData!=null && !httpPostData.equals("")){
	                HashMap<String,String> argv =  new HashMap<String,String>();
	                String[] arrParams = httpPostData.split("#");
	                for(int i=0;i<arrParams.length;i++){
	                	if(arrParams[i].indexOf("=") > -1)
	                    {
	                        String[] nameValue = arrParams[i].split("=", 2);
	                        if (!newpostdata.equals(""))
	                    		newpostdata += "&";
	                      //  try {
	                        	if(nameValue.length > 1)
	                        		newpostdata += nameValue[0]+"="+nameValue[1];
	                        		//newpostdata += nameValue[0]+"="+java.net.URLEncoder.encode(nameValue[1], "UTF-8");
			                    else
			                    	newpostdata += nameValue[0]+"=";
	                    }
	                }
	            }
	            System.out.println("submit postdata = "+newpostdata);
	            postHC.setRequestBody(newpostdata);
	            int statusCode;
	            try {
	            	//httpClient.getHostConfiguration().setHost("lsg.cnki.net", 80, "http");
	                statusCode = httpClient.executeMethod(postHC);
	                System.out.println("返回结果码： "+statusCode);
	                Header[] headers = postHC.getResponseHeaders();
	                for(Header header : headers){
	                	  checkCookie(header);
	                }
	               
	                if(statusCode==HttpStatus.SC_OK){
	                   
	                    String charset = "utf-8";
	                    InputStreamReader isr = new InputStreamReader(postHC.getResponseBodyAsStream(), charset); // 设置读取流的编码格式，自定义编码           
	                    // 使用字符读取方式，循环读取源文件内容       
	                    StringBuffer sb = new StringBuffer();
	                    int b ;
	                    while ((b = isr.read()) != -1)//顺序读取文件text里的内容并赋值给整型变量b,直到文件结束为止。
	                    {
	                        if (b < 32 && b!= 10 && b != 13 && b != 9) b = 32;//过滤掉一些换行等符号
	                        //if ( b== 10 || b== 13 || b== 9) b = 32;//过滤掉一些换行等符号
	                        sb.append((char)b);
	                    }
	                    isr.close();
	                    postHC.abort();
	                    //取得采集的内容
	                    httpContents  = sb.toString();
		                }
	                else
	                {
	                    if(statusCode == 301 || statusCode == 302){
	                        Header locationHeader = postHC.getResponseHeader("location");
	                        httpContents = readRemotePage();//重新请求新网页
	                    }
	                   
	                }
	               
	               
	            } catch (HttpException e2) {
	                e2.printStackTrace();
	            } catch (IOException e2) {
	                e2.printStackTrace();
	            }
	        }
	        return httpContents;
	    }

    private void checkCookie(NameValuePair entry){
        if(entry.getName().equals("Set-Cookie")){
        	if (this.cookieHash == null)
        		this.cookieHash = new HashMap<String,String>();
            String value = entry.getValue();
            if(value.indexOf(";") >0 )
                value = value.substring(0,value.indexOf(";"));
           
            String[] cookieNameValuePair = value.split("=", 2);
            if(cookieNameValuePair.length > 1) {
                if(cookieNameValuePair[1].equals("deleted")){
                    this.cookieHash.remove(cookieNameValuePair[0]);
                } else {
                	this.cookieHash.put(cookieNameValuePair[0], cookieNameValuePair[1]);
                    //cookie.put(cookieNameValuePair[0], java.net.URLDecoder.decode(cookieNameValuePair[1]));
                }
            } else {
            	this.cookieHash.remove(cookieNameValuePair[0]);
            }
        }
    }
    
	private void debug_write_file(String filename, String result) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(filename, false));
			writer.write(result);  
			writer.close();
		} catch (IOException e) {
			log.error("UspDataQueryAction: write into file error");
			e.printStackTrace();
		}  
		return; 
	}

}
