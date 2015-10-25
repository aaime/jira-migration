package org.geoserver.jira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class MigratorApp {

    final static String DEFAULT_TICKET_ROOT = "/home/aaime/tmp/codehaus/GEOS";

    final static int DEFAULT_NUM_THREADS = 6;

    final static String DEFAULT_JIRA_LOCATION = "https://osgeo-org.atlassian.net/";

    static final String DEFAULT_PROJECT_NAME = "GEOS";
    
    static final BufferedReader IN_READER = new BufferedReader(new InputStreamReader(System.in));

    static final String DEFAULT_USER_NAME = "aaime";

    private static int DEFAULT_MAX_ATTEMPTS = 6;;
    
    public static void main(String[] args) throws Exception {
        String jiraLocation = inquire("Jira location", DEFAULT_JIRA_LOCATION);
        String username = inquire("Username", DEFAULT_USER_NAME);
        String password = inquire("Password", null);
        String ticketRoot = inquire("Local ticket root", DEFAULT_TICKET_ROOT);
        String project = inquire("Project name", DEFAULT_PROJECT_NAME);
        int threads = Integer.parseInt(inquire("Number of threads", String.valueOf(DEFAULT_NUM_THREADS)));
        int maxAttempts = Integer.parseInt(inquire("Max migration attempts per ticket", String.valueOf(DEFAULT_MAX_ATTEMPTS )));
        
        List<Migration> migrations = getMigrations();
        System.out.println("Available migrations:");
        for (int i = 0; i < migrations.size(); i++) {
            Migration migration = migrations.get(i);
            System.out.println(i + " " + migration.getTitle());
        }
        int migrationIdx = Integer.parseInt(inquire("Chosen migration", "0"));
        Migration migration = migrations.get(migrationIdx);
        
        JiraClientProvider jiraProvider = new JiraClientProvider(jiraLocation, username, password);
        jiraProvider.test();
        migration.init(jiraProvider.getJiraClient(), project);
        
        Migrator migrator = new Migrator(jiraProvider, ticketRoot, project, threads, migration, maxAttempts);
        migrator.migrate();
    }
    
    private static List<Migration> getMigrations() throws InstantiationException, IllegalAccessException {
        Set<Class<? extends Migration>> migrations = new Reflections("org.geoserver.jira").getSubTypesOf(Migration.class);
        List<Migration> result = new ArrayList<>();
        for (Class<? extends Migration> clazz : migrations) {
            if(clazz.isInterface() ||  Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            result.add(clazz.newInstance());
        }
        return result;
    }

    private static String inquire(String prompt, String defaultValue) throws IOException {
        System.out.println(prompt + " (" + defaultValue + "): ");
        String value = IN_READER.readLine();
        if(value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

}
