package wag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Retrieves a set of tweets from the WarframeAlerts account on Twitter.
 * @author Mike Young
 * @version 2013-08-01
 */
public class TweetRetriever {
    
    /**
     * Retrieves a set of tweets from the WarframeAlerts account on Twitter.
     * @return a list of alerts
     */
    public static List<String> pull() {
        
        return parse(retrieve());
    }
    
    /**
     * Sends an HTTP GET request to the target URL, then reads the raw HTML.
     * @return the raw HTML read from the target URL
     */
    private static List<String> retrieve(){
        
        String url = "https://twitter.com/search?q=from%3Awarframealerts&mode=realtime";
        List<String> fileContents = new ArrayList<>();
        
        try {
            // Open the connection, and attach a BufferedReader to it.
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            
            // Try reading from the input stream one line at a time.
            String line;
            while ((line = reader.readLine()) != null) {
                fileContents.add(line);
            }
            reader.close();
        } catch (MalformedURLException mue) {
            
        } catch (IOException ioe) {
            
        }
        
        return fileContents;
    }
    
    /**
     * Parses the raw HTML read from the target URL and stored in <input>.
     * @param input the raw HTML read from the target URL
     * @return a list of alerts in the format "timestamp text"
     */
    private static List<String> parse(List<String> input) {
        
        ArrayList<String> alerts = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        while (!input.isEmpty()) {
            // Pop the first line off the list.
            String line = input.remove(0);
            // Check for timestamp text.
            if (line.contains("tweet-timestamp")) {
                long timestamp = parseTime(line);
                // Pop lines off the list until there are no more or until we
                // find the text contents of the tweet.
                while (!line.contains("tweet-text") && !input.isEmpty()) {
                    line = input.remove(0);
                }
                // Format the timestamp and tweet contents into a single string,
                // then add it to the alerts list.
                alerts.add("<" + sdf.format(new Date(timestamp)) + "> " 
                               + parseText(line));
            }
        }
        return alerts;
    }
    
    private static long parseTime(String line) {
        
        int start = line.indexOf('"', line.indexOf("data-time")) + 1;
        int end = line.indexOf('"', start);
        
        return Long.parseLong(line.substring(start, end)) * 1000;
    }
    
    private static String parseText(String line) {
        
        int start = line.indexOf('>') + 1;
        int end = line.indexOf('<', start);
        
        return line.substring(start, end);
    }
    
    public static void main(String[] args) {
        
        List<String> alerts = TweetRetriever.pull();
        System.out.println(alerts.size() + " alerts parsed.");
        System.out.println(alerts.get(0));
    }
}