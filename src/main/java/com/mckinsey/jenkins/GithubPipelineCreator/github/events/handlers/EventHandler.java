package com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers;

public interface EventHandler {

    void handleEvent(String payload);
}
