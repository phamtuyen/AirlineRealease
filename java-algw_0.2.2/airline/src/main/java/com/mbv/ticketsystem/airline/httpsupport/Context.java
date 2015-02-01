package com.mbv.ticketsystem.airline.httpsupport;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;

public class Context extends BasicHttpContext {
    private CookieStore cookieStore;

    public Context() {
        cookieStore = new BasicCookieStore();
        setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public void addCookie(String domain, String path, String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookieStore.addCookie(cookie);
    }
}
