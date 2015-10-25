package org.geoserver.jira;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.joox.Match;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.Project;
import net.rcarz.jiraclient.Version;

public class VersionMigration implements Migration {
    
    static final Logger LOGGER = Logger.getLogger(VersionMigration.class.getName());
    Set<String> projectVersions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    
    private String projectName;

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider) throws Exception {
        
        
        List<String> versions = new ArrayList<>();
        Match versionMatch = item.children("version");
        for (int i = 0; i < versionMatch.size(); i++) {
            String version = versionMatch.content(i);
            if(!projectVersions.contains(version)) {
                LOGGER.severe("Skippping unknown " + version);
            } else {
                versions.add(version);
            }
        }
        List<String> fixVersions = new ArrayList<>();
        Match fixVersionMatch = item.children("fixVersion");
        for (int i = 0; i < fixVersionMatch.size(); i++) {
            String version = fixVersionMatch.content(i);
            if(!projectVersions.contains(version)) {
                LOGGER.severe("Skippping unknown " + version);
            } else {
                fixVersions.add(version);
            }
        }
        
        if(!versions.isEmpty() || !fixVersions.isEmpty()) {
            JiraClient jira = jiraProvider.getJiraClient();
            System.out.println("Checking ticket " + key + " for versions");
            Issue issue = jira.getIssue(key);
            if(issue.getVersions().isEmpty() && issue.getFixVersions().isEmpty()) {
                System.out.println("Updating ticket " + key + " setting versions " + versions + " and fix versions " + fixVersions);
                issue.update().field(Field.VERSIONS, versions).field(Field.FIX_VERSIONS, fixVersions).execute();
            }
        }
    }

    @Override
    public String getTitle() {
        return "Migrate affects and fix-for versions";
    }

    @Override
    public void init(JiraClient client, String projectName) throws Exception {
        this.projectName = projectName;
        Project project = client.getProject(projectName);
        for (Version version : project.getVersions()) {
            projectVersions.add(version.getName());
        }
    }

}
