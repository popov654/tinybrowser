package network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import jsparser.Expression;
import jsparser.FormData;
import jsparser.JSInt;
import jsparser.JSObject;
import jsparser.JSParser;
import jsparser.JSString;
import jsparser.JSValue;
import jsparser.Null;

public class HttpsClient extends JSObject implements Runnable {

    public HttpsClient() {
        items.put("__proto__", HttpsClientProto.getInstance());
        JSObject upload = new JSObject();
        upload.set("onloadstart", Null.getInstance());
        upload.set("onprogress", Null.getInstance());
        upload.set("onabort", Null.getInstance());
        upload.set("onerror", Null.getInstance());
        upload.set("onload", Null.getInstance());
        upload.set("ontimeout", Null.getInstance());
        upload.set("onloadend", Null.getInstance());
        items.put("upload", upload);
    }

    public HttpsClient(String url) {
        this(url, "GET", "", true);
    }

    public HttpsClient(String url, String method) {
        this(url, method, "", true);
    }

    public HttpsClient(String url, String method, String post_data, boolean async) {
        this();
        this.url_string = url;
        this.method = method;
        this.post_data = new Vector<DataPart>();
        this.post_data.add(new DataPart("", post_data.getBytes(), ""));
        this.async = async;
    }

    public void setUrl(String url) {
        this.url_string = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setData(String data) {
        post_data = new Vector<DataPart>();
        post_data.add(new DataPart("", data.getBytes(), ""));
    }

    public void setData(String data, String charset) {
        post_data = new Vector<DataPart>();
        post_data.add(new DataPart("", data.getBytes(Charset.forName(charset)), ""));
    }

    public void setData(FormData data) {
        requestHeaders.put("Content-Type", data.encoding + "; charset=" + data.charset);
        formData = data;
    }

    public void setData(FormData data, String charset) {
        
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

            if (formData != null) {
                Vector<FormEntry> params = new Vector<FormEntry>();
                boolean multipart = false;

                Set<String> keys = formData.params.keySet();
                for (String key: keys) {
                    FormEntry entry;
                    JSValue value = formData.params.get(key);
                    if (value instanceof jsparser.File) {
                        entry = new FormEntry(key, ((jsparser.File)value).file);
                        multipart = true;
                    } else {
                        entry = new FormEntry(key, value.asString().getValue());
                    }
                    params.add(entry);
                }

                post_data = Request.prepareBody(con, params, formData.charset, multipart);
            }

            if (post_data != null && post_data.size() > 0) {
                con.setDoOutput(true);
                if (requestHeaders.size() > 0) {
                    Set<String> keys = requestHeaders.keySet();
                    for (String header: keys) {
                        con.setRequestProperty(header, requestHeaders.get(header));
                    }
                } else {
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                }
                final HttpsClient instance = this;
                Request.sendData(con, post_data, new NetworkEventListener() {
                    @Override
                    public void actionPerformed(String type, HashMap<String, String> data) {
                        if (type.equals("progress")) {
                            JSValue val = ((JSObject)items.get("upload")).get("onprogress");
                            if (val.getType().equals("Function")) {
                                Vector<JSValue> args = new Vector<JSValue>();
                                JSObject dataObj = new JSObject();
                                dataObj.set("loaded", new JSInt(data.get("loaded")));
                                dataObj.set("total", new JSInt(data.get("total")));
                                args.add(dataObj);
                                val.call(instance, args, false);
                            }
                        } else if (type.equals("error")) {
                            JSValue val = ((JSObject)items.get("upload")).get("onprogress");
                            if (val.getType().equals("Function")) {
                                val.call(instance, new Vector<JSValue>(), false);
                            }
                        }
                    }
                });
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

                JSValue val = items.get("onloadstart");
                if (val != null && val.getType().equals("Function")) {
                    val.call(this, new Vector<JSValue>(), false);
                }

                //System.out.println("****** Content of the URL ********");
                final HttpsClient instance = this;
                result = Request.getResponse(con, "UTF-8", new NetworkEventListener() {
                    @Override
                    public void actionPerformed(String type, HashMap<String, String> data) {
                        if (type.equals("progress")) {
                            JSValue val = items.get("onprogress");
                            if (val != null && val.getType().equals("Function")) {
                                Vector<JSValue> args = new Vector<JSValue>();
                                JSObject dataObj = new JSObject();
                                dataObj.set("loaded", new JSInt(data.get("loaded")));
                                dataObj.set("total", new JSInt(data.get("total")));
                                args.add(dataObj);
                                val.call(instance, args, false);
                            }
                        } else if (type.equals("error")) {
                            JSValue val = items.get("onprogress");
                            if (val != null && val.getType().equals("Function")) {
                                val.call(instance, new Vector<JSValue>(), false);
                            }
                        }
                    }
                });

            } catch (java.net.ConnectException e) {
                if (System.currentTimeMillis() - start_time > timeout && items.containsKey("ontimeout")) {
                    JSValue val = items.get("ontimeout");
                    if (val != null && val.getType().equals("Function")) {
                        val.call(this, new Vector<JSValue>(), false);
                    }
                }
                else if (items.containsKey("onerror")) {
                    JSValue val = items.get("onerror");
                    if (val != null && val.getType().equals("Function")) {
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

    @Override
    public void run() {
        request();
    }

    private String url_string;
    private String method;
    private Vector<DataPart> post_data;
    private FormData formData;
    private boolean async;
    private long start_time;
    private int timeout = 10000;
    
    private LinkedHashMap<String, String> requestHeaders = new LinkedHashMap<String, String>();

}
