package org.geoserver.jira;

import org.joox.Match;

import net.rcarz.jiraclient.JiraClient;

public interface Migration {
    
    default void init(JiraClient client, String project) throws Exception {
        
    }
    
    String getTitle();

    void applyMigration(String key, Match item, JiraClientProvider jiraProvider) throws Exception;
    
    default void dispose(JiraClient client) throws Exception {
        
    }
}
