package com.GetEvents;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/event")
public class Crawler {

    // Common data
    static String rootUrl;
    static HashSet<String> urlSet = new HashSet<String>();
    static HashSet<String> finishedUrlSet = new HashSet<String>();
    static HashSet<String> eventLinksSet = new HashSet<String>();

    //Get content from the url and return them as string
    public String getContent(String url)
    {
        if(url == null)
            return null;
        StringBuilder sb = null;
        HttpURLConnection con =null;

        try
        {
            // Creating url connection
            URL myUrl = new URL(url);
            con = (HttpURLConnection) myUrl.openConnection();
            con.setRequestMethod("GET");

            //if success
            if(con.getResponseCode()!=200)
            {
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String strTemp = "";
            sb = new StringBuilder();

            //Saving rootURL
            if(rootUrl == null)
                rootUrl = "http://" + myUrl.getHost();

            //Reading data to string
            while((strTemp = br.readLine())!= null )
            {
                //Avoiding empty lines
                if(!strTemp.matches("\\s+"))
                    sb.append(strTemp);
            }
            br.close();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            con.disconnect();
        }
        return sb.toString();

    }

    //Get links from input page data and add them to url set
    private void getUrlsFromPage(String data)
    {
        // Pattern match links in href attribute
        Pattern linkPattern = Pattern.compile("href=\"(.*?)\"", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Matcher linkMatcher = linkPattern.matcher(data);

        while(linkMatcher.find())
        {
            // Pattern match only event links and eliminate others
            String link = linkMatcher.group(1);
            if(link.matches(".*(/e/|/events/|/exhibition/|.*page_id=.*&id=.*).*"))
            {
                // Add only unique links, those links that haven't been processed yet
                if(!finishedUrlSet.contains(link))
                {
                    urlSet.add(link);
                }
            }
        }
    }


    //Check event links from urlSet add them to event links set
    private void checkEventLinks()
    {
        //Iterate through urlset
        for(String currUrl: urlSet)
        {

            //Add currentUrl to finishedUrlSet
            finishedUrlSet.add(currUrl);

            //Check if the link belongs to individual event page till you get 10 links
            if(eventLinksSet.size()<10)
            {

                //Pattern match for url containing http
                if(currUrl.matches("http.*") &&
                        !currUrl.matches(".*mailto.*|.*android-app.*"))
                {

                    String currUrlData = this.getContent(currUrl);
                    if(currUrlData!=null)
                    {
                        //If the page is an individual event page
                        if(verifyEvent(currUrlData))
                        {
                            eventLinksSet.add(currUrl);
                        }
                    }

                }

                //Pattern match for url not having http
                else if(!currUrl.matches(".*mailto.*|.*android-app.*"))
                {

                    String currUrlData = this.getContent(rootUrl + currUrl);
                    if(currUrlData!=null) {
                        //If the page is an individual event page
                        if(verifyEvent(currUrlData))
                        {
                            eventLinksSet.add(rootUrl+currUrl);
                        }
                    }

                }
            }

            //If you have 10 event links stop performing the process and break it
            else break;
        }

        //Clear url set
        urlSet.clear();

    }

    // Check if the input data belongs to a events page based on patterns
    private boolean verifyEvent(String urlData){

        // Check for following patterns in the page to determine if its an individual event page or not
        if(urlData.matches("(?i).*(When & Where|Ticket Information|Who's Going|Event Details|upcoming event on Eventbrite|past events on Eventbrite)*.*")) return true;
        return false;
    }

    @POST
    @Path("/url")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/html")
    public static String performOperation(@FormParam("url") String url) {

        //Create crawler instance
        Crawler c = new Crawler();

        //Get URL content and check for events
        String data = c.getContent(url);
        if(data!=null)
        {
            c.getUrlsFromPage(data);
            c.checkEventLinks();
        }

        //If event links set is still less than 10 , go to root url and check all links from that page and send back the results
        String data1 = c.getContent(rootUrl);
        if(data1!=null){
            c.getUrlsFromPage(data1);
            c.checkEventLinks();
        }
        System.out.println("Output :\n");

        //Massaging all event links as unordered list
        StringBuilder links = new StringBuilder(10);
        links.append("<ul>");
        for(String l:eventLinksSet)
        {
            links.append("<li><a href=\"" + l);
            links.append("\" target=\"_blank\">" + l + "</a>");
            System.out.println(l);
        }
        links.append("</ul>");

        //Clear all common data
        eventLinksSet.clear();
        finishedUrlSet.clear();
        urlSet.clear();
        rootUrl=null;

        //HTML returned as output
        return "<html> " + "<title>" + "Links to Events" + "</title>"
                + "<body><h1>Links to individual events</h1>" + links.toString() + "<style>\n" +
                "    .input {\n" +
                "        border: 1px solid #006;\n" +
                "        background: #ffc;\n" +
                "    }\n" +
                "    .input:hover {\n" +
                "        border: 1px solid #f00;\n" +
                "        background: #ff6;\n" +
                "    }\n" +
                "    .button {\n" +
                "        border: none;\n" +
                "        padding: 2px 8px;\n" +
                "    }\n" +
                "    .button:hover {\n" +
                "        border: none;\n" +
                "        padding: 2px 8px;\n" +
                "    }\n" +
                "    label {\n" +
                "        display: block;\n" +
                "        width: 150px;\n" +
                "        float: left;\n" +
                "        margin: 2px 4px 6px 4px;\n" +
                "        text-align: right;\n" +
                "    }\n" +
                "    br { clear: left; }\n" +
                "    body {\n" +
                "        background-color : #f4a460;\n" +
                "        margin: 0;\n" +
                "        padding: 0;\n" +
                "    }\n" +
                "    h1 {\n" +
                "        color : #000000;\n" +
                "        text-align : center;\n" +
                "        font-family: \"SIMPSON\";\n" +
                "    }\n" +
                "    form {\n" +
                "        width: 300px;\n" +
                "        margin: 0 auto;\n" +
                "    }\n" +
                "\n" +
                "ul.menu { display: block; text-align:center; }\n" +
                "ul.menu li { display: inline-block; }</style>" +
                "</body>" + "</html> ";

    }

    public static void main(String[] args) {

        String url = "http://www.eventbrite.com/e/sausalito-art-festival-2014-tickets-11831764125?aff=ehometext&rank=0";
        long startTime = System.currentTimeMillis();
        performOperation(url);
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Time taken : " + timeTaken);

    }

}
