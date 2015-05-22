package com.mckinsey.jenkins.GithubPipelineCreator.pipelines;

import hudson.model.TopLevelItem;
import hudson.security.ACL;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.mckinsey.jenkins.GithubPipelineCreator.config.SetupConfig;
import com.mckinsey.jenkins.GithubPipelineCreator.model.GitUrl;

public abstract class Pipeline {

    private static final String UTF_8 = "UTF-8";
    public static final String DEPLOY_JOB_SUFFIX = "deploy";
    public static final String PR_JOB_SUFFIX = "pr";
    public static final String JOB_NAME_SEPARATOR = "-";

    public static final String GIT_URL = "$$GIT_URL$$";
    public static final String GIT_BRANCH = "$$GIT_BRANCH$$";
    public static final String SHA = "$$SHA$$";
    public static final String PULL_REQUEST = "$$PULL_REQUEST$$";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final Pattern PATTERN = Pattern.compile("@@([A-Z]+)@@");
    public static final String TEMPLATES_DIR_PATH = "/templates/";

    public Pipeline() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Creates the pipeline with the given repository creation event payload and if successful, returns the newly created job name.
     * 
     * @param eventPayload
     * @return
     */
    public abstract String create(JSONObject eventPayload);

    public abstract String create(String gitUrl);

    protected boolean createJenkinsJob(String jobName, String xmlContent) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        TopLevelItem createdJob = Jenkins.getInstance().createProjectFromXML(jobName, new ByteArrayInputStream(xmlContent.getBytes()));
        return createdJob != null;
    }

    // protected String getCombinedShellCommands(String... templateNames) throws IOException {
    //
    // List<Path> inputs = new ArrayList<Path>();
    // for (String template : templateNames) {
    // inputs.add(getPath(TEMPLATES_DIR_PATH + template));
    // }
    // // read and combine all lines, replace variables with actual values, write back to XML and send it out.
    // StringBuilder builder = new StringBuilder();
    // for (Path path : inputs) {
    // for (String line : Files.readAllLines(path, Charset.forName(UTF_8)))
    // builder.append(line + LINE_SEPARATOR);
    // }
    // return builder.toString();
    // }

    protected String getCombinedShellCommands(String... templateNames) throws IOException {
        StringBuilder builder = new StringBuilder();
        List<InputStream> streams = new ArrayList<InputStream>();
        for (String template : templateNames) {
            streams.add(this.getClass().getResourceAsStream((TEMPLATES_DIR_PATH + template)));
        }
        for (InputStream inputStream : streams) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String lineRead = null;
            while ((lineRead = reader.readLine()) != null)
                builder.append(lineRead + LINE_SEPARATOR);
        }
        return builder.toString();
    }

    protected String getDashSeparatedRepoName(String repoName) {
        if (repoName.contains("/")) {
            String[] parts = repoName.split("/");
            repoName = parts[0] + "-" + parts[1];
            // TODO create a folder for each repo, if needed
        }
        return repoName;
    }

    protected String getBuildConfigXmlAsString(String templateXmlPath, Map<String, String> replacements) {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(templateXmlPath);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String lineRead;
            while ((lineRead = reader.readLine()) != null) {
                Matcher m = PATTERN.matcher(lineRead);
                while (m.find()) {
                    String key = m.group(0);
                    System.out.println("found match for " + key);
                    String replacement = replacements.get(key);
                    if (StringUtils.isEmpty(replacement))
                        throw new NullPointerException("The replacement for " + key + " is empty. Can't create job");
                    m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                m.appendTail(sb);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    // private Path getPath(String fileName) {
    // URL stream = this.getClass().getResource(fileName);
    // File file;
    // try {
    // file = new File(stream.toURI());
    // return file.toPath();
    // } catch (URISyntaxException e) {
    // e.printStackTrace();
    // }
    // return null;
    // }

    protected boolean shouldCreatePipeline(GHRepository repository) {
        if (repository == null)
            return false;
        try {
            GHContent ymlFile = repository.getFileContent("project.yml");
            String ymlString = ymlFile == null ? "" : ymlFile.getContent();
            return !StringUtils.isEmpty(ymlFile.getContent()) && isYmlConfigValid(ymlString);

            // TODO check if pipeline exists
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean isYmlConfigValid(String ymlConfig) {
        return true; // TODO write validation logic
    }

    protected GHRepository getRepository(String gitUrl) {
        SetupConfig config = SetupConfig.get();
        try {
            GitHub github = GitHub.connectUsingOAuth(config.getGithubApiUrl(), config.getGithubPersonalAccessToken());
            GitUrl url = new GitUrl(gitUrl);
            return github.getRepository(url.getFullRepoName());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
