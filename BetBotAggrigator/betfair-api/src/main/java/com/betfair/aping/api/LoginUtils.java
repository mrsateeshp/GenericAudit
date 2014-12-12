package com.betfair.aping.api;

import com.betfair.aping.containers.SSOTokenContainer;
import com.betfair.aping.util.JsonConverter;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sateesh
 * @since 12/12/2014
 */
public class LoginUtils {
    public static String getSSOToken(String applicationKey, String uid, String password) throws RuntimeException{
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://identitysso.betfair.com/api/login");

            String USER_AGENT = "Mozilla/5.0";

            post.setHeader("X-Application", applicationKey);
            post.setHeader("Accept", "application/json");
            post.setHeader("User-Agent", USER_AGENT);


            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("username", new String(Base64.decodeBase64(uid))));
            urlParameters.add(new BasicNameValuePair("password", new String(Base64.decodeBase64(password))));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            SSOTokenContainer container = JsonConverter.convertFromJson(result.toString(), SSOTokenContainer.class);

            if(container.getError().trim().length() > 0){
                throw new RuntimeException("failed to get token, error: "+container.getError());
            }
            return container.getToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void keepConnectionLive(String applicationKey, String ssoToken){
        //dummy
    }
}
