package com.mckinsey.jenkins.GithubPipelineCreator.pipelines.java;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class PatternChecker {

    public PatternChecker() {
        // TODO Auto-generated constructor stub
    }

    public static final Pattern PATTERN = Pattern.compile("@@([A-Z]+)@@");

    public static void main(String[] args) throws IOException, Exception {
        testHttpClient();
        // String[] lines = { "<hudson.tasks.Shell>", "<command>@@COMMAND@@</command>", "</hudson.tasks.Shell>" };
        // System.out.println("started");
        //
        // for (String string : lines) {
        // Matcher m = PATTERN.matcher(string);
        // while (m.find()) {
        // String key = m.group(0);
        // System.out.println("found match for " + key);
        // if ("$$COMMANDS$$".equalsIgnoreCase(key)) // replace the commands
        // System.out.println("match");
        // }
        // }
    }

    public static void testHttpClient() throws Exception, IOException {
        HttpClient client = new HttpClient();
        HostConfiguration config = new HostConfiguration();
        config.setHost("localhost", 8080);
        client.setHostConfiguration(config);
        PostMethod post = new PostMethod("http://localhost:8080/jenkins" + "/createItem?name=" + "test");
        post.setDoAuthentication(false);

        // RequestEntity entity = new FileRequestEntity(configXmlFile, "text/xml; charset=UTF-8");
        RequestEntity entity = new StringRequestEntity("test", "text/xml", "UTF-8");
        post.setRequestEntity(entity);
        try {
            int result = client.executeMethod(post);
            if (result != 200) {
                // not nice, but the easiest way
                throw new Exception("http-result-code:" + result);
            }
            System.out.println("Return code: " + result);
            for (Header header : post.getResponseHeaders()) {
                System.out.println(header.toString());
            }
            System.out.println(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

}
