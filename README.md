![scoopi-logo](https://user-images.githubusercontent.com/12656407/46675127-5e0de980-cbfb-11e8-9448-f9ea0dc070e7.png)

CodeTab Scoopi Guide <a href="https://www.codetab.org/tutorial/scoopi-web-scraper/introduction/">Quickstart and Guide</a>

<hr>

Scoopi is a tool to extract and transform data from web pages.

Libraries such as JSoup and HtmlUnit makes it quite easy to scrape web pages in Java, but they do well in scraping data from limited set of pages but things get pretty compilcated when you start to scrape thousands of pages. Scoopi is built on <a href="https://jsoup.org/">JSoup</a> and <a href="http://htmlunit.sourceforge.net/">HtmlUnit</a> and the functionality offered by Scoopi are:

   - Scoopi is fully definition driven. Data structure, task workflow and pages to scrape are defined with a set of YML definition files and no coding skill is required
   - It can be configured to use either JSoup or HtmlUnit as scraper
   - Query can be written either using Selectors (JSoup) or XPath (HtmlUnit)
   - Scoopi is a multithreaded application which process pages in parallel for maximum throughput. 
     - even on a low end system with core 2 duo processor, it can load, parse and transform around 1000 pages in under two minutes.
   - Scoopi ships as Docker image so that it can run without any cumbersome installation
   - Scoopi persists pages and data to file system so that it recover from the failed state without repeating the tasks already completed
   - Can transform, filter and sort the data before output
   - Ships with built-in appenders such as FileAppender, DBAppender and ListAppender.
   - ScoopiEngine can be embeded in other programs and access scrapped data with ListAppender
   - Flexible workflow allows one to change sequence of steps
   - Scoopi is extensible. Developers can extend the predefined base steps or even create new ones with different functionality and weave them in workflow
   - Scoopi Cluster 
     - In cluster mode, it can scale horizontally by distributing tasks across multiple nodes
     - Designed to run in various environments; in bare JVM or in Docker containers or even on high end container orchestration platforms such as Kubernetes
     - For clustering, Scoopi Cluster uses <a href="https://hazelcast.org" target="_blank">Hazelcast IMDG</a>, a fault-tolerant distributed in-memory computing platform 

## Scoopi Installation

To install and run Scoopi refer <a href="https://www.codetab.org/tutorial/scoopi-web-scraper/introduction/">Quickstart and Guide</a>.


