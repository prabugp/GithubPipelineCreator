package com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers;

import hudson.model.ParameterValue;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;
import com.mckinsey.jenkins.GithubPipelineCreator.jobs.JobRepository;
import com.mckinsey.jenkins.GithubPipelineCreator.jobs.PropertyFileBasedJobRepository;
import com.mckinsey.jenkins.GithubPipelineCreator.model.GitUrl;
import com.mckinsey.jenkins.GithubPipelineCreator.pipelines.java.JavaPipeline;

@GitHubEventHandler(eventHandled = GitHubEvent.PUSH)
public class PushEventHandler implements EventHandler {

    private static final Logger LOGGER = Logger.getLogger(PullRequestEventHandler.class.getName());

    public PushEventHandler() {
    }

    @Override
    public void handleEvent(String payload) {
        LOGGER.finer("Received payload " + payload);
        // trigger pull request dry run build.
        JSONObject payloadJson = JSONObject.fromObject(payload);
        JSONObject repo = payloadJson.getJSONObject("repository");
        String gitHtmlUrl = repo.getString("html_url");
        GitUrl gitUrl = new GitUrl(gitHtmlUrl);
        String commitSha = payloadJson.getJSONObject("head_commit").getString("id");

        JobRepository jobRepository = new PropertyFileBasedJobRepository();
        @SuppressWarnings("rawtypes")
        AbstractProject project = jobRepository.getJobForGitEvent(gitUrl, GitHubEvent.PUSH);
        if (project == null) {
            // push event received, but no pipeline. create the pipeline and then trigger the job.
            String createProject = new JavaPipeline().create(gitHtmlUrl);
            if (StringUtils.isEmpty(createProject)) {
                System.out.println("Pipeline not found and cannot be created for " + gitHtmlUrl);
                System.out.println("Ignoring event!!");
                return;
            }
        }
        System.out.println("Building for " + gitUrl.getHttpsUrl() + " -- " + "master branch for commit " + commitSha);
        List<ParameterValue> values = new ArrayList<ParameterValue>();
        values.add(new StringParameterDefinition("GIT_URL", "").createValue(gitUrl.getHttpsGitCheckOutUrl()));
        values.add(new StringParameterDefinition("GIT_BRANCH", "").createValue("master"));
        values.add(new StringParameterDefinition("SHA", "").createValue(commitSha));
        project = jobRepository.getJobForGitEvent(gitUrl, GitHubEvent.PUSH);
        if (project != null) {
            System.out.println("Found job " + project.getName() + ". Enqueueing... ");
            Jenkins.getInstance().getQueue().schedule(project, 0, new ParametersAction(values));
        } else {
            System.out.println("Job not found for PUSH event for " + gitHtmlUrl);
        }
    }

}
