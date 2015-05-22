package com.mckinsey.jenkins.GithubPipelineCreator.jobs;

import hudson.model.AbstractProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;

import com.google.gson.Gson;
import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEvent;
import com.mckinsey.jenkins.GithubPipelineCreator.model.GitHubLinkedJenkinsJob;
import com.mckinsey.jenkins.GithubPipelineCreator.model.GitUrl;
import com.mckinsey.jenkins.GithubPipelineCreator.pipelines.Pipeline;

@SuppressWarnings("rawtypes")
public class PropertyFileBasedJobRepository implements JobRepository {

    String JOB_LIST_FILE_NAME = "pipeline-creator-jobs.json";
    private static Map<String, AbstractProject> repoEventProjectMap = new HashMap<String, AbstractProject>();

    private static File jobListFile = null;

    public PropertyFileBasedJobRepository() {
    }

    @Override
    public AbstractProject getJob(String jobName) {
        return null;
    }

    @Override
    public List<AbstractProject> getJobsForGitUrl(GitUrl gitUrl) {
        List<AbstractProject> selectedProjects = new ArrayList<AbstractProject>();
        String projectName = getProjectNameForRepoAndEvent(gitUrl, null);
        for (AbstractProject abstractProject : Jenkins.getInstance().getItems(AbstractProject.class)) {
            if (abstractProject.getName().startsWith(projectName))
                selectedProjects.add(abstractProject);
        }

        return selectedProjects;
    }

    @Override
    public AbstractProject getJobForGitEvent(GitUrl gitUrl, GitHubEvent event) {
        // FIXME improve this - cache??
        List<AbstractProject> projects = Jenkins.getInstance().getItems(AbstractProject.class);
        String projectName = getProjectNameForRepoAndEvent(gitUrl, event);
        for (AbstractProject abstractProject : projects) {
            if (abstractProject.getName().equalsIgnoreCase(projectName))
                return abstractProject;
        }

        return null;
    }

    private String getProjectNameForRepoAndEvent(GitUrl gitUrl, GitHubEvent event) {
        StringBuilder dashSeparatedUrl = new StringBuilder().append(gitUrl.getOrgName()).append(Pipeline.JOB_NAME_SEPARATOR).append(gitUrl.getName());
        if (event != null) {
            switch (event) {
                case PULL_REQUEST:
                    dashSeparatedUrl.append(Pipeline.JOB_NAME_SEPARATOR).append(Pipeline.PULL_REQUEST);
                    break;
                case PUSH:
                    // no op since push is always triggering default build.
                    // TODO think about branch specific push
                    break;
                default:
                    break;
            }
        }
        return dashSeparatedUrl.toString();
    }

    @Override
    public void addJobToRepository(GitUrl gitUrl, String jobName, GitHubEvent event) {
        GitHubLinkedJenkinsJob job = new GitHubLinkedJenkinsJob(jobName, gitUrl.getHttpsUrl(), event);
        Gson gson = new Gson();
        String json = gson.toJson(job);
        System.out.println(json);
        FileWriter writer = null;
        try {
            writer = new FileWriter(getJobListFile());
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private File getJobListFile() {
        synchronized (this.getClass()) {
            if (jobListFile == null) {
                File rootDir = Jenkins.getInstance().root;
                if (rootDir.exists() && rootDir.canWrite()) {
                    File file = new File(rootDir, JOB_LIST_FILE_NAME);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    jobListFile = file;
                }
            }
        }
        if (jobListFile == null)
            throw new IllegalStateException("Json file for storing job list is not found and can't be created.");
        return jobListFile;
    }

}
