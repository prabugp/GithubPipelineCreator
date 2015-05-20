/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.mckinsey.jenkins.GithubPipelineCreator.github;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.util.SequentialExecutionQueue;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import com.google.common.io.CharStreams;
import com.mckinsey.jenkins.GithubPipelineCreator.github.events.GitHubEventUtil;

@Extension
@ExportedBean
public class GithubWebhook implements UnprotectedRootAction {
    private static final Logger LOGGER = Logger.getLogger(GithubWebhook.class.getName());
    private final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor());

    @Override
    public String getUrlName() {
        return "githook";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void doIndex(StaplerRequest req, StaplerResponse response) throws IOException {
        String payload = req.getParameter("payload");
        String eventType = req.getHeader("X-Github-Event");
        if (StringUtils.isEmpty(payload) && "POST".equalsIgnoreCase(req.getMethod())) {
            payload = getRequestPayload(req);
        }
        if (StringUtils.isEmpty(payload)) {
            throw new IllegalArgumentException("Not intended to be browsed interactively (must specify payload parameter)");
        }
        // processGitHubPayload(payload, req);
        GitHubEventUtil.handleEvent(eventType, payload);
    }

    protected String getRequestPayload(StaplerRequest req) throws IOException {
        return CharStreams.toString(req.getReader());
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

}
