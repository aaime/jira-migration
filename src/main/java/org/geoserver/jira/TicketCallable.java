package org.geoserver.jira;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joox.Match;

import net.rcarz.jiraclient.JiraException;

class TicketCallable implements Callable<Void> {
    
    static final Logger LOGGER = Logger.getLogger(TicketCallable.class.getName());

    File ticketFile;
    JiraClientProvider jiraProvider;
    Migration migration;
    int maxAttempts;

    public TicketCallable(File ticketFile, JiraClientProvider jiraProvider, Migration migration, int maxAttempts) {
        this.ticketFile = ticketFile;
        this.jiraProvider = jiraProvider;
        this.migration = migration;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public Void call() throws Exception {
        try {
            Match document = $(ticketFile);
            Match item = document.find("item");
            String key = item.child("key").text();
            for(int i = 0; i < maxAttempts; i++) {
                try {
                    migration.applyMigration(key, item, jiraProvider);
                    return null;
                } catch(JiraException e) {
                    LOGGER.log(Level.FINE, "Failed attempt number " + i + " against ticket " + key, e);
                }
            }
            
            throw new JiraException("Failed to retrieve ticket " + key + " after " + maxAttempts + " attempts");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process ticket " + ticketFile.getName(), e);
        }
        return null;
    }

}