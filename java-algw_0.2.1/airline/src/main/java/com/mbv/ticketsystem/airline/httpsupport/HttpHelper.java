package com.mbv.ticketsystem.airline.httpsupport;

import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


public class HttpHelper {
    private static HttpClient httpClient;
    static {
        PoolingClientConnectionManager conman = new PoolingClientConnectionManager();
        conman.setMaxTotal(50);
        conman.setDefaultMaxPerRoute(20);

        conman.getSchemeRegistry().register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        try {
            SSLSocketFactory socketFactory = new SSLSocketFactory(new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            conman.getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        httpClient = new DefaultHttpClient(conman, params);
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }

    public static Context createContext() {
        return new Context();
    }

    private static Response Execute(HttpUriRequest request, Context context) throws Exception {
        try {
            HttpResponse response = httpClient.execute(request, context);
            Response result = new Response();
            result.setStatusCode(response.getStatusLine().getStatusCode());
            if (result.getStatusCode() == 302)
                result.setLocation(response.getFirstHeader("Location").getValue());
            HttpEntity entity = response.getEntity();
            if (entity != null)
                result.setBody(EntityUtils.toString(entity));
            return result;
        } catch (Exception ex) {
            request.abort();
            throw new Exception("CONNECTION_ERROR");
        }
    }

    public static Response POST(URI uri, Context context, HashMap<String, String> params) throws Exception {
        HttpPost httppost = new HttpPost(uri);
        if (params != null) {
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            for (Entry<String, String> entry : params.entrySet()) {
                postParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httppost.setEntity(new UrlEncodedFormEntity(postParams));
        }
        return Execute(httppost, context);
    }

    public static Response GET(URI uri, Context context) throws Exception {
        HttpGet get = new HttpGet(uri);
        return Execute(get, context);
    }

    public static URI createURI(String baseURI, HashMap<String, String> params) throws Exception {
        URIBuilder urlBuilder = new URIBuilder(baseURI);
        for (Entry<String, String> param : params.entrySet()) {
            urlBuilder.addParameter(param.getKey(), param.getValue());
        }
        return urlBuilder.build();
    }
}
