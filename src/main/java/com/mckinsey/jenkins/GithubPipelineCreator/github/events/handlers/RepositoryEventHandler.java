package com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;
import com.mckinsey.jenkins.GithubPipelineCreator.pipelines.java.JavaPipeline;

@GitHubEventHandler(eventHandled = GitHubEvent.REPOSITORY)
public class RepositoryEventHandler implements EventHandler {

    private static final Logger LOGGER = Logger.getLogger(RepositoryEventHandler.class.getName());
    public static Map<String, String> jobsList = new HashMap<String, String>();

    private static final String REPOSITORY = "repository";
    private static final String ACTION = "action";

    @Override
    public void handleEvent(String payload) {
        JSONObject payloadJson = JSONObject.fromObject(payload);
        String action = payloadJson.getString(ACTION);
        JSONObject repo = payloadJson.getJSONObject(REPOSITORY);
        String repo_url = repo.getString("html_url");

        if (action == null || repo == null || !action.equalsIgnoreCase("created")) {
            throw new IllegalStateException("Not a repository created event");
        }

        try {
            String jobName = createPipeline(repo);
            jobsList.put(repo_url, jobName);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String createPipeline(JSONObject repoJson) throws IOException {
        LOGGER.info("Creating pipeline for the new repo");
        LOGGER.finest("event payload for creating pipeline is " + repoJson.toString(2));
        return new JavaPipeline().create(repoJson);
    }

}
