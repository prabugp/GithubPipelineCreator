package com.mckinsey.jenkins.GithubPipelineCreator.model;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;

public class GitHubLinkedJenkinsJob {

    private String jobName;
    private GitUrl gitUrl;
    private GitHubEvent event;

    public GitHubLinkedJenkinsJob(String jobName, String gitUrl, GitHubEvent event) {

        super();
        this.jobName = jobName;
        this.gitUrl = new GitUrl(gitUrl);
        this.event = event;
    }

    public String getJobName() {
        return jobName;
    }

    public GitUrl getGitUrl() {
        return gitUrl;
    }

    public GitHubEvent getEvent() {
        return event;
    }

}
