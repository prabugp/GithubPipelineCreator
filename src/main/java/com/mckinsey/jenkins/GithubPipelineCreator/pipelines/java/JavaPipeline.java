package com.mckinsey.jenkins.GithubPipelineCreator.pipelines.java;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.github.GHRepository;

import com.mckinsey.jenkins.GithubPipelineCreator.model.GitUrl;
import com.mckinsey.jenkins.GithubPipelineCreator.pipelines.Pipeline;

public class JavaPipeline extends Pipeline {

    public JavaPipeline() {
    }

    /**
     * Create a pipeline from a repo event. create only if project.yml is present.
     */
    @Override
    public String create(JSONObject repositoryJson) {
        if (!shouldCreatePipeline(getRepository(repositoryJson.getString("html_url")))) {
            return ""; // no op
        }
        String repoName = repositoryJson.getString("full_name");
        return createPipeline(repoName);
    }

    /**
     * Create a pipeline from any other event. If the initial repo creation event doesn't have the project.yml file, then no pipeline will
     * be created. On subsequent push, if someone pushes the yml file, then we need to create the pipline at that point and then trigger the
     * build as well.
     */
    @Override
    public String create(String gitUrl) {
        GitUrl url = new GitUrl(gitUrl);
        GHRepository repository = getRepository(url.getHttpsUrl());
        if (!shouldCreatePipeline(repository)) {
            return ""; // no op
        }
        return createPipeline(repository.getFullName());
    }

    private String createPipeline(String repoName) {
        try {
            repoName = getDashSeparatedRepoName(repoName);
            createMasterBuildJob(repoName);
            createPRBuildJob(repoName + JOB_NAME_SEPARATOR + PR_JOB_SUFFIX);
            createDeployerJob(repoName + JOB_NAME_SEPARATOR + DEPLOY_JOB_SUFFIX, repoName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repoName;
    }

    private void createMasterBuildJob(String repoName) throws IOException, Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("@@COMMANDS@@", getCombinedShellCommands("git-base-steps.txt", "java-maven-steps.txt"));
        createJenkinsJob(repoName, getBuildConfigXmlAsString("/templates/java-maven-build-template.xml", replacements));
    }

    private void createPRBuildJob(String repoName) throws IOException, Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("@@COMMANDS@@", getCombinedShellCommands("git-pr-steps.txt", "java-maven-steps.txt"));
        createJenkinsJob(repoName, getBuildConfigXmlAsString("/templates/java-maven-build-template.xml", replacements));
    }

    private void createDeployerJob(String repoName, String upstreamJobName) throws IOException, Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("@@COMMANDS@@", getCombinedShellCommands("java-deploy-tomcat.txt"));
        replacements.put("@@UPSTREAMJOB@@", upstreamJobName);
        createJenkinsJob(repoName, getBuildConfigXmlAsString("/templates/java-tomcat-deployer-template.xml", replacements));
    }

}
