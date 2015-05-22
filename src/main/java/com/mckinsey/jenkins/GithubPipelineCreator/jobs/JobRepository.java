package com.mckinsey.jenkins.GithubPipelineCreator.jobs;

import hudson.model.AbstractProject;

import java.util.List;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;
import com.mckinsey.jenkins.GithubPipelineCreator.model.GitUrl;

@SuppressWarnings("rawtypes")
public interface JobRepository {

    AbstractProject getJob(String jobName);

    List<AbstractProject> getJobsForGitUrl(GitUrl gitUrl);

    AbstractProject getJobForGitEvent(GitUrl gitUrl, GitHubEvent event);

    void addJobToRepository(GitUrl gitUrl, String jobName, GitHubEvent event);
}
