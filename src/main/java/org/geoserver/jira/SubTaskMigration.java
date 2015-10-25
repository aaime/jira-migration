package org.geoserver.jira;

import org.joox.Match;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;

public class SubTaskMigration implements Migration {

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider) throws Exception {
        String localType = item.child("type").text();

        if ("Sub-task".equals(localType)) {
            String parent = item.child("parent").text();
            JiraClient jira = jiraProvider.getJiraClient();
            Issue issue = jira.getIssue(key);
            String remoteType = issue.getIssueType().getName();
            if(!remoteType.equals(localType)) {
                System.out.println("Migrating issue " + key + " to type " + localType);
                issue.update().field(Field.ISSUE_TYPE, localType).fieldAdd(Field.PARENT, parent).execute();
            }
        }
    }

    @Override
    public String getTitle() {
        return "Migrate sub-tasks and reattach them to their parents'";
    }

    @Override
    public void init(JiraClient client, String project) throws Exception {
        // nothing to do
        
    }

}
