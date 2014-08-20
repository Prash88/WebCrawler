WebCrawler
==========

A Web Crawler that takes an url and finds individual event urls in the same domain.

Description
===========

A RESTful application that takes a url as input (http post) and finds individual event urls in the same domain and returns 10 urls as output.

Logic Used
==========

1) Application takes an input url. (Can be called through HTTP post as well).

2) Read all links from that page and save them in a hash set (pattern match for events url and eliminate some of them without reading its content itself - pattern used ".*(/e/|/events/|/exhibition/|.*page_id=.*&id=.*).*" )

3) Then checks each link if it belongs to an individual event by patten matching its content to the pattern - 
"(?i).*(When & Where|Ticket Information|Who's Going|Event Details|upcoming event on Eventbrite|past events on Eventbrite)*.*"

4) If the event links set is filled with 10 urls based on patten matching mentioned above ,it returns all urls as output and stops/breaks the process.

5) Else it takes to root/domain url and checks all links in  root/domain url content saves them in the same hash set mentioned above and keeps repeating steps 3 and 4, till all urls in the url set's exhausted

Technologies Used
=================
Java, Servlets, Maven, Tomcat, Jersey - RESTful Web Services in Java

Links
=====

Application URL : http://spotoncrawler.prash88.cloudbees.net

GitHub : https://github.com/Prash88/WebCrawler

REST API 
POST POST spotoncrawler.prash88.cloudbees.net/rest/rest/event/url
Content-Type:application/x-www-form-urlencoded

Form parameters:
url=<url string>

Example
url=http://www.eventbrite.com/e/sausalito-art-festival-2014-tickets-11831764125

Response:
HTTP/1.1 200 OK
Content-Type: HTML
