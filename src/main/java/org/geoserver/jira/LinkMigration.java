package org.geoserver.jira;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joox.Match;

import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.IssueLink;
import net.rcarz.jiraclient.JiraClient;

public class LinkMigration implements Migration {
    
    static final Logger LOGGER = Logger.getLogger(LinkMigration.class.getName());
    Set<String> projectVersions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider) throws Exception {
        
        // do we have any local outward links? 
        Match localLinks = item.children("issuelinks/issuelinktype/outwardlinks/issuelink");
        if(localLinks.size() == 0) {
            return;
        }

        // grab the remote issue and see the remote links
        JiraClient client = jiraProvider.getJiraClient();
        Issue issue = client.getIssue(key);
        List<IssueLink> remoteLinks = issue.getIssueLinks();
        
        // migrate what's not there
        for (int i = 0; i < localLinks.size(); i++) {
            Match localLink = localLinks.child(i);
            String linkType = localLink.attr("description");
            String linkedIssue = localLink.child("issuekey").text();
            try {
                if(!hasLink(linkType, linkedIssue, remoteLinks)) {
                    issue.link(linkedIssue, linkType);
                }
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to migrate link to " + linkedIssue + " of type " + linkType, e);
            }
        }
        
    }

    private boolean hasLink(String linkType, String linkedIssue, List<IssueLink> remoteLinks) {
        for (IssueLink remote : remoteLinks) {
            if(remote.getType().getName().equals(linkType) && remote.getOutwardIssue().getKey().equals(linkedIssue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTitle() {
        return "Migrate issue links";
    }


}
