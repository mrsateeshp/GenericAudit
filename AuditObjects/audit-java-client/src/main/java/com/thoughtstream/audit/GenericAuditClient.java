package com.thoughtstream.audit;

import com.thoughtstream.audit.bean.AuditMessage;
import com.thoughtstream.audit.exception.AuditMessageSaveFailed;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sateesh
 * @since 04/01/2015
 */
public class GenericAuditClient {
    private String auditServer;
    private JSONParser parser = new JSONParser();

    final Logger logger = LoggerFactory.getLogger(GenericAuditClient.class);

    public GenericAuditClient(String auditServer) {
        if (auditServer == null) {
            throw new IllegalArgumentException("auditServer can not be null.");
        }
        this.auditServer = auditServer;
    }

    public void postAuditMessage(AuditMessage aMessage) throws AuditMessageSaveFailed {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://" + auditServer + "/api/saveAuditEvent");

            post.setHeader("Accept", "application/json");


            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("oldObjectXML", aMessage.getOldDataSnapshot()));
            urlParameters.add(new BasicNameValuePair("newObjectXML", aMessage.getNewDataSnapshot()));

            if (aMessage.getWho() != null) {
                urlParameters.add(new BasicNameValuePair("who", aMessage.getWho()));
            }
            if (aMessage.getWhen() != null) {
                urlParameters.add(new BasicNameValuePair("when", Long.toString(aMessage.getWhen().getTime())));
            }
            if (aMessage.getOperationType() != null) {
                urlParameters.add(new BasicNameValuePair("operationType", aMessage.getOperationType()));
            }

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);

            logger.debug("Response Header: {}", response.toString());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            logger.debug("AuditClient response body: {}", result.toString());
            JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
            Boolean success = (Boolean) jsonObject.get("success");
            if (!success) {
                String errorMessage = (jsonObject.get("error") != null) ? jsonObject.get("error").toString() : null;
                logger.error("Audit message save failed with the following error: {}", errorMessage);
                throw new AuditMessageSaveFailed(errorMessage);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AuditMessageSaveFailed(e);
        }
    }
}
