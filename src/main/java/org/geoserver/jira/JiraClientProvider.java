package org.geoserver.jira;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

class JiraClientProvider {
    
    final int MAX_ATTEMPTS = 12;

    String jiraLocation;
    String username;
    String password;
    
    public JiraClientProvider(String jiraLocation, String username, String password) {
        this.jiraLocation = jiraLocation;
        this.username = username;
        this.password = password;
    }
    
    public void test() throws JiraException {
        getJiraClient().getProjects();
    }
    
    public JiraClient getJiraClient() {
        BasicCredentials creds = new BasicCredentials(username, password);
        JiraClient jira = new JiraClient(jiraLocation, creds);
        
        return jira;
    }
}
