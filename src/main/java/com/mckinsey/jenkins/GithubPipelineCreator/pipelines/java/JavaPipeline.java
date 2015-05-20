package com.mckinsey.jenkins.GithubPipelineCreator.pipelines.java;

import hudson.security.ACL;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.acegisecurity.context.SecurityContextHolder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.mckinsey.jenkins.GithubPipelineCreator.config.SetupConfig;
import com.mckinsey.jenkins.GithubPipelineCreator.pipelines.Pipeline;

public class JavaPipeline extends Pipeline {

    public JavaPipeline() {
    }

    @Override
    public String create(JSONObject repositoryJson) {
        // open the default template xml
        // replace the params in the file with the ones in the event payload

        String accessToken = SetupConfig.get().getGithubPersonalAccessToken();
        String repoName = repositoryJson.getString("full_name");

        GitHub github = null;
        try {
            github = GitHub.connectUsingOAuth(SetupConfig.get().getGithubApiUrl(), accessToken);
            GHRepository repository = github.getRepository(repoName);
            if (repoName.contains("/")) {
                String[] parts = repoName.split("/");
                repoName = parts[0] + "-" + parts[1];
            }
            createJenkinsJob(repoName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return repoName;
    }

    private boolean createJenkinsJob(String jobName) throws Exception {
        // get parts of the given string-url
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        Jenkins.getInstance().createProjectFromXML(jobName, new ByteArrayInputStream(getConfigXmlFile().getBytes()));
        return true;
    }

    private String getConfigXmlFile() {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            String commands = getShellCommands();
            URL stream = this.getClass().getResource("/templates/java-maven-build-template.xml");
            File file = new File(stream.toURI());
            reader = new BufferedReader(new FileReader(file));
            String lineRead;
            while ((lineRead = reader.readLine()) != null) {
                Matcher m = PATTERN.matcher(lineRead);
                while (m.find()) {
                    String key = m.group(0);
                    System.out.println("found match for " + key);
                    if ("@@COMMANDS@@".equalsIgnoreCase(key)) // replace the commands
                        m.appendReplacement(sb, Matcher.quoteReplacement(commands));
                }
                m.appendTail(sb);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        System.out.println("the final config file is " + sb.toString());
        return sb.toString();
    }

    private String getShellCommands() throws IOException {

        List<Path> inputs = Arrays.asList(getPath("/templates/git-base-steps.txt"), getPath("/templates/java-maven-steps.txt"));
        List<String> commands = new ArrayList<String>();
        // read and combine all lines, replace variables with actual values, write back to XML and send it out.
        StringBuilder builder = new StringBuilder();
        for (Path path : inputs) {
            for (String line : Files.readAllLines(path, Charset.forName("UTF-8")))
                builder.append(line + LINE_SEPARATOR);
        }
        return builder.toString();
    }

    private Path getPath(String fileName) {
        URL stream = this.getClass().getResource(fileName);
        File file;
        try {
            file = new File(stream.toURI());
            this.getClass().getResource(fileName);
            System.out.println(file.exists());
            return file.toPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}
