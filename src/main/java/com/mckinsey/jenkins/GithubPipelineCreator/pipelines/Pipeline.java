package com.mckinsey.jenkins.GithubPipelineCreator.pipelines;

import java.util.regex.Pattern;

import net.sf.json.JSONObject;

public abstract class Pipeline {

    public static final String JENKINS_URL = "http://localhost:8080/jenkins/createItem?name=";
    public static final String GIT_URL = "$$GIT_URL$$";
    public static final String GIT_BRANCH = "$$GIT_BRANCH$$";
    public static final String SHA = "$$SHA$$";
    public static final String PULL_REQUEST = "$$PULL_REQUEST$$";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final Pattern PATTERN = Pattern.compile("@@([A-Z]+)@@");

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

}
