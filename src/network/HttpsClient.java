package network;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.io.*;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Set;
import java.util.Vector;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import jsparser.Expression;
import jsparser.JSInt;
import jsparser.JSObject;
import jsparser.JSParser;
import jsparser.JSString;
import jsparser.JSValue;

public class HttpsClient extends JSObject implements Runnable {

    public HttpsClient() {
        items.put("__proto__", HttpsClientProto.getInstance());
    }

    public HttpsClient(String url) {
        this(url, "GET", "", true);
    }

    public HttpsClient(String url, String method) {
        this(url, method, "", true);
    }

    public HttpsClient(String url, String method, String post_data, boolean async) {
        items.put("__proto__", HttpsClientProto.getInstance());
        this.url_string = url;
        this.method = method;
        this.post_data = post_data.getBytes();
        this.async = async;
    }

    public void setUrl(String url) {
        this.url_string = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setData(String data) {
        post_data = data.getBytes();
    }

    public void setData(String data, String charset) {
        try {
            post_data = data.getBytes(charset);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
    

    public static void main(String[] args) {
        JSParser jp = new JSParser("function parse() { this.responseText }");
        Expression exp = Expression.create(jp.getHead());
        exp.eval();
        HttpsClient hc = new HttpsClient("http://freegeoip.net/json/94.188.20.58");
        hc.set("onload", Expression.getVar("parse", exp));
        hc.start();
        System.out.println("Main thread");
    }

    public void start() {
        if (async) new Thread(this).start();
        else request();
    }

    public String request() {
        stateChanged(0);
        String result = "";
        String https_url = url_string;
        URL url;
        try {
            start_time = System.currentTimeMillis();
            stateChanged(1);
            URLConnection con;
            url = new URL(https_url);
            if (url.getProtocol().equals("https")) {
                con = (HttpsURLConnection)url.openConnection();
            } else {
                con = (HttpURLConnection)url.openConnection();
            }
            stateChanged(2);
            ((HttpURLConnection)con).setRequestMethod(method);

            Request.setRequestHeaders(con);

            if (post_data != null && post_data.length > 0) {
                con.setDoOutput(true);
                if (requestHeaders.size() > 0) {
                    Set<String> keys = requestHeaders.keySet();
                    for (String header: keys) {
                        con.setRequestProperty(header, requestHeaders.get(header));
                    }
                } else {
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                }
                Request.sendData(con, post_data);
            }

            //dump all the content
            result = get_content(con);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (items.containsKey("onerror")) {
                JSValue val = items.get("onerror");
                if (val.getType().equals("Function")) {
                    val.call(this, new Vector<JSValue>(), false);
                }
            }
	    e.printStackTrace();
        }
        return result;
    }

    private void print_https_cert(HttpsURLConnection con){

        if (con != null) {

            try {

                System.out.println("Response Code : " + con.getResponseCode());
                System.out.println("Cipher Suite : " + con.getCipherSuite());
                System.out.println("\n");

                Certificate[] certs = con.getServerCertificates();
                for(Certificate cert : certs){
                    System.out.println("Cert Type : " + cert.getType());
                    System.out.println("Cert Hash Code : " + cert.hashCode());
                    System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
                    System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
                    System.out.println("\n");
                }

	    } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
	    } catch (IOException e){
                e.printStackTrace();
	    }
       }
    }

    private String get_content(URLConnection con) {

        String result = "";

        if (con != null) {

            try {

                items.put("status", new JSInt(((HttpURLConnection)con).getResponseCode()));
                items.put("statusText", new JSString(((HttpURLConnection)con).getResponseMessage()));
                stateChanged(3);

                //System.out.println("****** Content of the URL ********");
                BufferedReader br =
                    new BufferedReader(
                         new InputStreamReader(con.getInputStream()));

                String input;

                while ((input = br.readLine()) != null){
                    result += input + "\n";
                }
                br.close();

            } catch (java.net.ConnectException e) {
                if (System.currentTimeMillis() - start_time > timeout && items.containsKey("ontimeout")) {
                    JSValue val = items.get("ontimeout");
                    if (val.getType().equals("Function")) {
                        val.call(this, new Vector<JSValue>(), false);
                    }
                }
                else if (items.containsKey("onerror")) {
                    JSValue val = items.get("onerror");
                    if (val.getType().equals("Function")) {
                        val.call(this, new Vector<JSValue>(), false);
                    }
                }
            } catch (IOException e) {
                stateChanged(4);
                e.printStackTrace();
            }
        }
        items.put("response", new JSString(result));
        items.put("responseText", new JSString(result));
        stateChanged(4);
        return result;
    }

    private void stateChanged(int state) {
        items.put("readyState", new JSInt(state));
        if (items.containsKey("onreadystatechange")) {
            JSValue val = items.get("onreadystatechange");
            if (val.getType().equals("Function")) {
                val.call(this, new Vector<JSValue>(), false);
            }
        }
        if (items.containsKey("onload") && state == 4) {
            JSValue val = items.get("onload");
            if (val.getType().equals("Function")) {
                val.call(this, new Vector<JSValue>(), false);
            }
        }
    }

    public void run() {
        request();
    }

    private String url_string;
    private String method;
    private byte[] post_data;
    private boolean async;
    private long start_time;
    private int timeout = 10000;
    
    private LinkedHashMap<String, String> requestHeaders = new LinkedHashMap<String, String>();

}