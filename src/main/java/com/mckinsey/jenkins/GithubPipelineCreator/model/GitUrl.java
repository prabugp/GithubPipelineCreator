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
package com.mckinsey.jenkins.GithubPipelineCreator.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHRepository;

public class GitUrl {
    private static final Logger LOGGER = Logger.getLogger(GitUrl.class.getName());
    private static final Pattern GITHUB_HTTP_URL = Pattern.compile("^https?://(.*)/(.*)/(.*)");
    private static final Pattern GITHUB_SSH_URL = Pattern.compile("^git@(.*):(.*)/(.*).git");
    private final String url;
    private final String orgName;
    private final String name;
    private final String domain;
    private final String protocol;

    public GitUrl(String url) {
        if (StringUtils.isEmpty(url))
            throw new IllegalArgumentException("Invalid git url " + url);
        this.url = url;
        try {
            this.protocol = new URL(url).getProtocol();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid git url " + url);
        }
        Matcher matcher = GITHUB_HTTP_URL.matcher(url);
        if (matcher.matches()) {
            this.domain = matcher.group(1);
            this.orgName = matcher.group(2);
            this.name = stripDotGit(matcher.group(3));
        } else {
            Matcher sshMatcher = GITHUB_SSH_URL.matcher(url);
            if (sshMatcher.matches()) {
                this.domain = matcher.group(1);
                this.orgName = sshMatcher.group(2);
                this.name = sshMatcher.group(3);

            } else {
                throw new IllegalArgumentException("Invalid git url " + url);
            }
        }
        LOGGER.info("Git url has the following components: " + this.protocol + "-" + this.domain + "-" + this.orgName + "-" + this.name);
    }

    public String getFullRepoName() {
        return orgName + "/" + name;
    }

    public String getUrl() {
        return url;
    }

    public String getGitUrl() {
        return String.format("git@%s:%s/%s.git", domain, orgName, name);
    }

    public String getName() {
        return name;
    }

    public String getHttpsUrl() {
        return String.format("%s://%s/%s/%s", protocol, domain, orgName, name);
    }

    /**
     * Always gives you the https://<config.getGithubWebUrl>/orgName/repoName.git
     * 
     * @return
     */
    public String getHttpsGitCheckOutUrl() {
        return String.format("%s://%s/%s/%s.git", protocol, domain, orgName, name);
    }

    private String stripDotGit(String name) {
        if (name.endsWith(".git"))
            return name.replace(".git", "");
        return name;
    }

    public static String getHtmlUrl(GHRepository repository) {
        return new GitUrl(repository.getUrl()).getHttpsUrl();
    }

    public String getOrgName() {
        return orgName;
    }

    public String getDomain() {
        return domain;
    }
}
