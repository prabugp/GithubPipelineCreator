package com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers;

import java.util.logging.Logger;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;

@GitHubEventHandler(eventHandled = GitHubEvent.PULL_REQUEST)
public class PullRequestEventHandler implements EventHandler {

    private static final Logger LOGGER = Logger.getLogger(PullRequestEventHandler.class.getName());

    public PullRequestEventHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleEvent(String payload) {
        LOGGER.finer("Received payload " + payload);
        // trigger pull request dry run build.
        String gitUrl = "";
        RepositoryEventHandler.jobsList.get(gitUrl);
    }

}
