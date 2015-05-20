package com.mckinsey.jenkins.GithubPipelineCreator.github.events;

import java.util.Set;
import java.util.logging.Logger;

import org.reflections.Reflections;

import com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers.EventHandler;
import com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers.GitHubEventHandler;

public class GitHubEventUtil {
    private static final Logger LOGGER = Logger.getLogger(GitHubEventUtil.class.getName());
    private static Set<Class<?>> handlers = null;

    static {
        Reflections ref = new Reflections("com.mckinsey.jenkins.GithubPipelineCreator.github.events.handlers");
        handlers = ref.getTypesAnnotatedWith(GitHubEventHandler.class);
        LOGGER.info("Found " + handlers.size() + " github event handlers");
    }

    public static GitHubEvent getEventType(String t) {
        for (GitHubEvent e : GitHubEvent.values()) {
            if (e.name().equalsIgnoreCase(t))
                return e;
        }
        return null;
    }

    public static void handleEvent(String eventType, String payload) {
        GitHubEvent event = getEventType(eventType);

        for (Class<?> class1 : handlers) {
            if (class1.getAnnotation(GitHubEventHandler.class).eventHandled().equals(event)) {
                LOGGER.info("Got our man! calling handle on " + class1.getCanonicalName());
                try {
                    ((EventHandler) class1.newInstance()).handleEvent(payload);
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
