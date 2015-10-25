package org.geoserver.jira;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Migrator {

    private JiraClientProvider jiraProvider;

    private String ticketRoot;

    private int threads;

    private String projectName;

    private Migration migration;

    private int maxAttempts;

    public Migrator(JiraClientProvider jiraProvider, String ticketRoot, String projectName, int threads, Migration migration, int maxAttempts) {
        this.jiraProvider = jiraProvider;
        this.ticketRoot = ticketRoot;
        this.threads = threads;
        this.projectName = projectName;
        this.migration = migration;
        this.maxAttempts = maxAttempts;
    }
    
    void migrate() {
        ExecutorService executors = Executors.newFixedThreadPool(threads);
        launchCallables(new File(ticketRoot), executors);
        executors.shutdown();
    }

    

    private void launchCallables(File root, ExecutorService executors) {
        if (root.isDirectory()) {
            for (File f : root.listFiles()) {
                if (f.isDirectory()) {
                    launchCallables(f, executors);
                } else {
                    String name = f.getName();
                    if (f.isFile() && name.matches(projectName + "-.*xml")) {
                        executors.submit(new TicketCallable(f, jiraProvider, migration, maxAttempts));
                    }
                }
            }
        }
    }

}
