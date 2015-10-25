package org.geoserver.jira;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.joox.Match;

public class ReporterMigration implements Migration {
    
    static final Logger LOGGER = Logger.getLogger(ReporterMigration.class.getName());
    Set<String> projectVersions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider) throws Exception {
        
        
        String reporter = item.child("reporter").text();
        String userName = item.child("reporter").attr("username");

    }

    @Override
    public String getTitle() {
        return "Migrate reporter user names";
    }

}
