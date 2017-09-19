package com.eastrobot.robotdev.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils{

    private static final Logger logger = LoggerFactory.getLogger( HttpUtils.class );

    /**
     * 最大连接数
     */
    public final static int MAX_TOTAL_CONNECTIONS = 1000;

    /**
     * 每个路由最大连接数,对于同一个目标机器的最大并发连接只有默认只有2个 哪怕你设置连接池的最大连接数为200，但是实际上还是只有2个连接在工作，
     * 其他剩余的198个连接都在等待，都是为别的目标机器服务的（目标服务器通常指同一台服务器或者同一个域名）
     */
    public final static int MAX_ROUTE_CONNECTIONS = 100;// 100

    /**
     * 连接超时时间 10s
     */
    public final static int CONNECT_TIMEOUT = 10 * 1000;

    /**
     * 读取超时时间 10s
     */
    public final static int READ_TIMEOUT = 15 * 1000;

    public final static int HTTP_PORT = 80;

    public final static int HTTPS_PORT = 443;

    // private static final String USER_AGENT =
    // "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)";//IE6
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:13.0) Gecko/20100101 Firefox/13.0.1";

    private static final String CHARSET = "UTF-8";

    public static HttpClient httpClient;

    private static ScheduledExecutorService executor;

    static{

        HttpParams params = new BasicHttpParams();
        // HTTP 协议的版本,1.1/1.0/0.9
        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );
        // 字符集
        HttpProtocolParams.setContentCharset( params, CHARSET );
        // 伪装的浏览器类型
        HttpProtocolParams.setUserAgent( params, USER_AGENT );
        HttpProtocolParams.setUseExpectContinue( params, true );

        // 设置连接超时时间
        params.setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT );
        params.setParameter( CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT );
        // 从连接池中取连接的超时时间
        // ConnManagerParams.setTimeout(params, WAIT_TIMEOUT);

        // 设置访问协议
        SchemeRegistry schreg = new SchemeRegistry();
        schreg.register( new Scheme( "http", HTTP_PORT, PlainSocketFactory.getSocketFactory() ) );
        schreg.register( new Scheme( "https", HTTPS_PORT, SSLSocketFactory.getSocketFactory() ) );

        // 多连接的线程安全的管理器
        PoolingClientConnectionManager pccm = new PoolingClientConnectionManager( schreg );
        pccm.setDefaultMaxPerRoute( MAX_ROUTE_CONNECTIONS ); // 每个目标主机的最大并行链接数
        pccm.setMaxTotal( MAX_TOTAL_CONNECTIONS ); // 客户端总并行链接最大数

        httpClient = new DefaultHttpClient( pccm, params );

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate( new Runnable(){
            @Override
            public void run(){// 每分钟执行，处理各类任务
                httpClient.getConnectionManager().closeExpiredConnections();// 关闭连接失效的链接
                httpClient.getConnectionManager().closeIdleConnections( 30, TimeUnit.SECONDS );// 关闭30s空闲的链接
            }

        }, 5, 5, TimeUnit.SECONDS );
    }

    /**
     * @Description <b>(get方法发起请求)</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return byte[]
     * @since 2016年2月2日
     */
    public static byte[] doGet( String url, List<NameValuePair> params, String charset ){
        // 产生最终地址
        String realUrl = getQueryUrl( url, params, charset );
        HttpGet get = new HttpGet( realUrl );
        try{
            HttpResponse response = httpClient.execute( get );
            if( HttpStatus.SC_OK == response.getStatusLine().getStatusCode() ){
                if( response.getEntity() != null ){
                    return EntityUtils.toByteArray( response.getEntity() );
                }
            }else{
                get.abort();
            }
        }catch( Exception e ){
            e.printStackTrace();
            get.abort();
        }finally{
            get.releaseConnection();
        }
        return null;
    }

    /**
     * @Description <b>(get方法发起请求)</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return byte[]
     * @since 2016年2月2日
     */
    public static byte[] doGet( String url, Map<String, String> paramsMap, String charset ){
        List<NameValuePair> params = map2Nvp( paramsMap );
        return doGet( url, params, charset );
    }

    /**
     * @Description <b>(get方法发起请求)</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return byte[]
     * @since 2016年2月2日
     */
    public static String getContent( String url, List<NameValuePair> params, String charset ){
        // 产生最终地址
        String realUrl = getQueryUrl( url, params, charset );
        HttpGet get = new HttpGet( realUrl );
        try{
            HttpResponse response = httpClient.execute( get );
            if( HttpStatus.SC_OK == response.getStatusLine().getStatusCode() ){
                if( response.getEntity() != null ){
                    return EntityUtils.toString( response.getEntity(), charset );
                }
            }else{
                get.abort();
            }
        }catch( Exception e ){
            e.printStackTrace();
            get.abort();
        }finally{
            get.releaseConnection();
        }
        return null;
    }

    /**
     * @Description <b>( 传入map参数，请求url，获取结果 )</b></br>
     * @param url
     *            地址
     * @param paramsMap
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String getContent( String url, Map<String, String> paramsMap, String charset ){
        List<NameValuePair> params = map2Nvp( paramsMap );
        return getContent( url, params, charset );
    }

    /**
     * @Description <b>( 传入List<NameValuePair>参数，请求url，获取结果 )</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return byte[]
     * @since 2016年2月2日
     */
    public static byte[] doPost( String url, List<NameValuePair> params, String charset ){
        HttpPost post = new HttpPost( url );
        try{
            post.setEntity( new UrlEncodedFormEntity( params, charset ) );
            HttpResponse response = httpClient.execute( post );
            if( HttpStatus.SC_OK == response.getStatusLine().getStatusCode() ){
                if( response.getEntity() != null ){
                    return EntityUtils.toByteArray( response.getEntity() );
                }
            }else{
                post.abort();
            }
        }catch( Exception e ){
            e.printStackTrace();
            post.abort();
        }finally{
            post.releaseConnection();
        }
        return null;
    }

    /**
     * @Description <b>( 传入map参数，请求url，获取结果 )</b></br>
     * @param url
     *            地址
     * @param paramsMap
     *            参数
     * @return byte[]
     * @since 2016年1月13日
     */
    public static byte[] doPost( String url, Map<String, String> paramsMap, String charset ){
        List<NameValuePair> list = map2Nvp( paramsMap );
        return doPost( url, list, charset );
    }

    /**
     * @Description <b>(传入List<NameValuePair>参数，请求url，获取结果 )</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String getPostContent( String url, List<NameValuePair> params, String charset ){
        logger.info( "url={}", url );
        logger.info( "params={}", params );
        HttpPost post = new HttpPost( url );
        try{
            post.setEntity( new UrlEncodedFormEntity( params, charset ) );
            HttpResponse response = httpClient.execute( post );
            if( HttpStatus.SC_OK == response.getStatusLine().getStatusCode() ){
                if( response.getEntity() != null ){
                    return EntityUtils.toString( response.getEntity(), charset );
                }
            }else{
                post.abort();
            }
        }catch( Exception e ){
            e.printStackTrace();
            post.abort();
        }finally{
            post.releaseConnection();
        }
        return null;
    }

    /**
     * @Description <b>( 传入map参数，请求url，获取结果 )</b></br>
     * @param url
     *            地址
     * @param paramsMap
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String getPostContent( String url, Map<String, String> paramsMap, String charset ){
        logger.info( "params={}", paramsMap );
        List<NameValuePair> params = map2Nvp( paramsMap );
        return getPostContent( url, params, charset );
    }

    /**
     * @Description
     * <b>(保存文件)</b></br>
     * @param url
     * @param destFileName
     * @return 
     * String 
     * @since 2016-6-15    
    */
    public static String getFile( String url, String destFileName ){
        HttpGet httpget = null;
        InputStream in = null;
        FileOutputStream fout = null;
        try{
            httpget = new HttpGet( url );
            HttpResponse response = httpClient.execute( httpget );
            HttpEntity entity = response.getEntity();
            in = entity.getContent();
            File file = new File( destFileName );
            fout = new FileOutputStream( file );
            int l = -1;
            byte[] tmp = new byte[1024];
            while( (l = in.read( tmp )) != -1 ){
                fout.write( tmp, 0, l );
                // 注意这里如果用OutputStream.write(buff)的话，图片会失真，大家可以试试
            }
            fout.flush();
            fout.close();
        }catch( Exception e ){
            e.printStackTrace();
        }finally{
            httpget.releaseConnection();
            try{
                if( in != null ){
                    in.close();
                }
            }catch(Exception e){}
            try{
                if( fout != null ){
                    fout.close();
                }
            }catch(Exception e){}
        }
        return null;
    }

    /**
     * @Description <b>( 使用请求体，如json等，获取结果 )</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String sendRequestBody( String url, String requestBody, String charset ){
        logger.info( "requestBody={}", requestBody );
        HttpPost post = new HttpPost( url );
        try{
            StringEntity entity = new StringEntity( requestBody, charset );
            entity.setContentEncoding( charset );
            // entity.setContentType("application/json");
            post.setEntity( entity );
            HttpResponse response = httpClient.execute( post );
            if( HttpStatus.SC_OK == response.getStatusLine().getStatusCode() ){
                if( response.getEntity() != null ){
                    return EntityUtils.toString( response.getEntity(), charset );
                }
            }else{
                post.abort();
            }
        }catch( Exception e ){
            e.printStackTrace();
            post.abort();
        }finally{
            post.releaseConnection();
        }
        return null;
    }

    /**
     * @Description <b>( Map<String, String> 转换成 List<NameValuePair> )</b></br>
     * @param paramsMap
     *            参数Map
     * @return List<NameValuePair>
     * @since 2016年1月13日
     */
    public static List<NameValuePair> map2Nvp( Map<String, String> paramsMap ){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Set<Map.Entry<String, String>> entrySet = paramsMap.entrySet();
        for( Map.Entry<String, String> e : entrySet ){
            String name = e.getKey();
            String value = e.getValue();
            NameValuePair nameValuePair = new BasicNameValuePair( name, value );
            params.add( nameValuePair );
        }
        return params;
    }

    /**
     * @Description <b>(Map<String,String> 拼接成url查询字符串)</b></br>
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String getQueryUrl( Map<String, String> params, String charset ){
        return URLEncodedUtils.format( map2Nvp( params ), charset );
    }

    /**
     * @Description <b>(组装带queryString的地址)</b></br>
     * @param url
     *            地址
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String getQueryUrl( String url, List<NameValuePair> params, String charset ){
        String paramsStr = null;
        String realUrl = null;
        if( params != null ){
            paramsStr = URLEncodedUtils.format( params, charset );
            StringBuffer sb = new StringBuffer();
            sb.append( url ).append( "?" ).append( paramsStr );
            realUrl = sb.toString();
        }else{
            realUrl = url;
        }
        return realUrl;
    }

    /**
     * @Description <b>(返回url参数字符串)</b></br>
     * @param params
     *            参数
     * @param charset
     *            编码
     * @return String
     * @since 2016年2月2日
     */
    public static String getQueryUrl( List<NameValuePair> params, String charset ){
        return URLEncodedUtils.format( params, charset );
    }

    /**
     * @Description <b>( 默认httpClient )</b></br>
     * @return HttpClient
     * @since 2016年2月2日
     */
    public static HttpClient getHttpClient(){
        return httpClient;
    }

    public static void main( String[] args ){
        long b = System.currentTimeMillis();
        // String url =
        // "http://localhost:83/juhe/onebox/weather/query?cityname=%E5%B9%BF%E5%B7%9E&key=345ae784b92fe8e97e0ad036f5b0b350";
        String url = "http://localhost:83/juhe/onebox/weather/query?cityname=%E5%B9%BF%E5%B7%9E&key=345ae784b92fe8e97e0ad036f5b0b350#";
        // String url =
        // "http://localhost:83/search?ssid=0&from=2001a&pu=usm%400%2Csz%401320_480&ext=&uid=wiaui_1324659653_8798&st=3&bd_page_type=1&bk_fr=bk_view&word=%E6%AF%9B%E6%B3%BD%E4%B8%9C%E6%80%9D%E6%83%B3";
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        /*
         * nvps.add( new BasicNameValuePair( "sessionId",
         * "3631c63df6c94d598f082c1b46dec46f" ) );// 名值对 nvps.add( new
         * BasicNameValuePair( "userId", "1ebd58c2cf3e4a958910a9f9af8bfde0" ) );
         * nvps.add( new BasicNameValuePair( "faqId",
         * "3662ab8af57d42a19ff71d55d2b991c4" ) ); nvps.add( new
         * BasicNameValuePair( "platform", "web" ) ); nvps.add( new
         * BasicNameValuePair( "city", "guangzhou" ) ); nvps.add( new
         * BasicNameValuePair( "question", "你好么" ) ); nvps.add( new
         * BasicNameValuePair( "answer",
         * "1rvQ6NKqyP2yvaGjMaGi0aHU8dP7sOzA7bXEur22zqO7MqGi0aHU8df5zrujuzOhotfU0NDTw0E01r208tOhtce7+sXGo6jGvrTLtce7+sXGo6y/ydaxvdPNqLn9sLK87KOssqK1x7v6o6mhow=="
         * ) ); nvps.add( new BasicNameValuePair( "reason", "1" ) ); nvps.add(
         * new BasicNameValuePair( "brand", "gdcmcc" ) ); nvps.add( new
         * BasicNameValuePair( "option", "2" ) );
         */

        try{
            String str = HttpUtils.getContent( url, nvps, "UTF-8" );
            System.out.println( str );
        }catch( Exception e ){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println( "耗时:" + (System.currentTimeMillis() - b) );
        System.out.println( "耗时:" + (System.currentTimeMillis() - b) );

    }
}
