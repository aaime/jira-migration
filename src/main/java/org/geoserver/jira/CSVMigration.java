package org.geoserver.jira;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joox.Match;

import net.rcarz.jiraclient.JiraClient;

public class CSVMigration implements Migration {
    
    static final class Ticket implements Comparable<Ticket> {
        String key;
        String summary;
        String creationDate;
        String resolutionDate;
        
        public Ticket(String key, String summary, String creationDate, String resolutionDate) {
            this.key = key;
            this.creationDate = creationDate;
            this.resolutionDate = resolutionDate;
            this.summary = summary;
        }

        @Override
        public int compareTo(Ticket o) {
            return key.compareTo(o.key);
        }
    }
    
    ConcurrentLinkedQueue<Ticket> creations = new ConcurrentLinkedQueue<>();
    String location;

    @Override
    public String getTitle() {
        return "Collect ticket ids and creation/resolution dates in CSV file";
    }
    
    @Override
    public void init(JiraClient client, String project) throws Exception {
        this.location = MigratorApp.inquire("Output file: " , "/tmp/ticketCreations.csv");
    }

    @Override
    public void applyMigration(String key, Match item, JiraClientProvider jiraProvider)
            throws Exception {
        String created = item.child("created").text();
        String resolved = item.child("resolved").text();
        String summary = item.child("summary").text();
        if(created != null) {
            creations.add(new Ticket(key, summary, created, resolved));
        } else {
            System.out.println("Skipping issue " + key + ", no creation date");
        }
    }
    
    @Override
    public void dispose(JiraClient client) throws Exception {
        List<Ticket> result = new ArrayList<>(creations);
        Collections.sort(result);
        DateFormat df1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        DateFormat df2 = new SimpleDateFormat("dd/MMM/yy h:mm a", Locale.ENGLISH);
        df2.setTimeZone(TimeZone.getTimeZone("GMT"));
        try(PrintStream ps = new PrintStream(new File(this.location))) {
            ps.println("key, created, resolved, summary");
            for (Ticket tc : result) {
                String formattedCreation = formatDate(df1, df2, tc.creationDate);
                String formattedResolution = formatDate(df1, df2, tc.creationDate);
                ps.println(tc.key + "," + formattedCreation + "," + formattedResolution + ",\"" + tc.summary + "\"");
            }
        }
        System.out.println("Now run the CSV import according to these instructions: https://confluence.atlassian.com/display/JIRAKB/How+to+change+the+issue+creation+date+using+CSV+import");
        System.out.println("Btw, import of resolution dat does not work, sorry... at least it fixes the creation one");
    }

    private String formatDate(DateFormat df1, DateFormat df2, String originalDate)
            throws ParseException {
        if(originalDate == null || originalDate.isEmpty()) {
            return "";
        }
        Date date = df1.parse(originalDate);
        String formattedDate = "\"" + df2.format(date) + "\"";
        return formattedDate;
    }
    
}
