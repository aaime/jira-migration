package org.geoserver.jira;

import org.joox.Match;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;

public class IssueTypeMigration implements Migration {

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider) throws Exception {
        String localType = item.child("type").text();

        if (!"Bug".equals(localType) && !"Sub-task".equals(localType) && !"Test".equals(localType)) {
            JiraClient jira = jiraProvider.getJiraClient();
            Issue issue = jira.getIssue(key);
            String remoteType = issue.getIssueType().getName();
            if(!remoteType.equals(localType)) {
                if("Wish".equals(localType)) {
                    localType = "New Feature";
                } 
                System.out.println("Migrating issue " + key + " to type " + localType);
                issue.update().field(Field.ISSUE_TYPE, localType).execute();
            }
        }
    }

    @Override
    public String getTitle() {
        return "Migrate issue type for all non 'Bug' tickets'";
    }

}
