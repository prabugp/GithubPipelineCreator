package com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GitHubEventHandler {

    GitHubEvent eventHandled();
}
