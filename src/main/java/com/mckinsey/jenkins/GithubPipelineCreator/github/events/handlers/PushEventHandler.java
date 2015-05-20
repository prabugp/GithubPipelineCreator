package com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers;

import hudson.model.Item;
import hudson.model.ParameterValue;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;

@GitHubEventHandler(eventHandled = GitHubEvent.PUSH)
public class PushEventHandler implements EventHandler {

    private static final Logger LOGGER = Logger.getLogger(PullRequestEventHandler.class.getName());

    public PushEventHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleEvent(String payload) {
        Jenkins jenkinsInstance = Jenkins.getInstance();
        LOGGER.finer("Received payload " + payload);
        // trigger pull request dry run build.
        JSONObject payloadJson = JSONObject.fromObject(payload);
        JSONObject repo = payloadJson.getJSONObject("repository");
        String gitUrl = repo.getString("html_url");
        String jobName = RepositoryEventHandler.jobsList.get(gitUrl);
        for (Item item : jenkinsInstance.getAllItems()) {
            System.out.println(item.getFullDisplayName());
        }
        for (Item item : jenkinsInstance.getAllItems(AbstractProject.class)) {
            AbstractProject project = (AbstractProject) item;
            List<ParameterValue> values = new ArrayList<ParameterValue>();
            values.add(new StringParameterDefinition("GIT_URL", "").createValue("https://github.com/sscTest/b1.git"));
            values.add(new StringParameterDefinition("GIT_BRANCH", "").createValue("master"));
            values.add(new StringParameterDefinition("SHA", "").createValue("5523a35231a85bed892f5f5883b6514080fef612"));
            Jenkins.getInstance().getQueue().schedule(project, 0, new ParametersAction(values));
        }
    }

}
