![scoopi-logo](https://user-images.githubusercontent.com/12656407/46675127-5e0de980-cbfb-11e8-9448-f9ea0dc070e7.png)

CodeTab Scoopi Guide <a href="https://www.codetab.org/tutorial/scoopi-web-scraper/introduction/">Quickstart and Guide</a>

<hr>

Scoopi is a tool to extract and transform data from web pages.

Libraries such as JSoup and HtmlUnit makes it quite easy to scrape web pages in Java, but the things get complicated when data is from large number of pages. They do well in scraping data from limited set of pages but they are not meant to handle thousands of pages. Scoopi is built on <a href="https://jsoup.org/">JSoup</a> and <a href="http://htmlunit.sourceforge.net/">HtmlUnit</a> and the functionality offered by Scoopi are:

   - Scoopi is completely definition driven. Data structure, task workflow and pages to scrape are defined with a set of YML definition files and no coding skill is required
   - It can be configured to use either JSoup or HtmlUnit as scraper
   - Query can be written either using Selectors with JSoup or XPath with HtmlUnit
   - Scoopi is a multithreaded application which process pages in parallel for maximum throughput. 
     - even on a low end system with core 2 duo processor, it can load, parse and transform around 1000 pages in under two minutes.
   - Scoopi ships as Docker image so that it can run without any cumbersome installation
   - Scoopi persists pages and data to database so that it recover from the failed state without repeating the tasks already completed
   - For Transparent persistence, Scoopi uses <a href="https://db.apache.org/jdo">JDO Standard</a> and <a href="http://www.datanucleus.org" >DataNucleus AccessPlatform</a> and you can choose your Datastore from a very wide range!
   - Allows to transform, filter and sort the data
   - With built-in appenders such as FileAppender, DBAppender and ListAppender.
   - ScoopiEngine can be embeded in other programs and access scrapped data with ListAppender
   - Flexible workflow allows one to change sequence of steps
   - Scoopi is extensible. Developers can extend the predefined base steps or even create new ones with different functionality and weave them in workflow

## Scoopi Installation

To install and run Scoopi see [CodeTab Scoopi Guide](CodeTab Scoopi Guide <a href="https://www.codetab.org/tutorial/scoopi-web-scraper/introduction/">Quickstart and Guide</a>). 


