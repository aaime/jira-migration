package org.geoserver.jira;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.joox.Match;

import net.rcarz.jiraclient.Component;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;

public class ComponentMigration implements Migration {
    static final Logger LOGGER = Logger.getLogger(ComponentMigration.class.getName());
    Set<String> projectComponents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public void init(JiraClient client, String project) throws Exception {
        List<Component> components = client.getProject(project).getComponents();
        for (Component component : components) {
            projectComponents.add(component.getName());
        }
    }

    @Override
    public String getTitle() {
        return "Migrate components";
    }

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider)
            throws Exception {
        List<String> components = new ArrayList<>();
        Match componentMatch = item.children("component");
        for (int i = 0; i < componentMatch.size(); i++) {
            String component = componentMatch.content(i);
            if(!projectComponents.contains(component)) {
                LOGGER.severe("Skippping unknown " + component);
            } else {
                components.add(component);
            }
        }
        if(!components.isEmpty()) {
            JiraClient jira = jiraProvider.getJiraClient();
            System.out.println("Checking ticket " + key + " for components");
            Issue issue = jira.getIssue(key);
            if(issue.getComponents().isEmpty()) {
                System.out.println("Updating ticket " + key + " setting components " + components);
                issue.update().field(Field.COMPONENTS, components).execute();
            }
        }

    }

}
