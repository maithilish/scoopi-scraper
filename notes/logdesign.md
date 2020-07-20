# Scoopi Logging


## Categories

Critical errors 
 - throw CriticalException 
 - catch in ScoopiEngine 
 - terminate the app

Step errors 
 - throw StepRunException
 - catch in Task 
 - terminate the step
 - count error only if data error or error that has impact on data

Step errors in some items but others are fine
 - catch and log error
 - do not terminate the step

## Levels

Log4j2 levels - ERROR, WARN, INFO, DEBUG, TRACE, OFF

## Log 

info 
 - to output completion of phases and app progress

warn
 - any minor aspects
 - used by user to correct definitions/config
   
error
 - for critical and step errors
 - used by user to correct definitions 
 
debug
 - for critical, step errors and for variables
 - used by developer to debug
 - log stack trace with %ex in appender pattern (only top level, no nested exception)
 
trace
 - to debug variables
 - used by user to fine tune definitions and correct queries
 - log stack trace with %rEx in appender pattern (recursive - nested exceptions)
 
Notes:

 - Errors class counts errors using metrics counter
 
## Markers

JobInfo provides two markers - JobMarker (task-<locatorName>-<groupName>-<taskName>) and JobAbortedMarker (task-aborted).
 - JobMarker is used to mark any log (i.e.any level) that needs task wise classification. The logs are appended to task log file. 
 - The JobAbortedMarker is for error logs (only error level) that needs task wise classification. The logs are appended to task log file and to data error log file. Unmarked error logs go to error log file. 
 
DataDefDefs has one marker to trace datadef (datadef-xxx)

## Exception stack trace

In a logging statement, if exception is last argument then stack trace is printed with %ex or %rEx patterns. For, console use %ex{short.message}, for error log file %ex{short}, for debug file %ex (full tracktrace but without nested exceptions) and for trace %rEx (full tracktrace including nested exceptions)

	Exception e = new IllegalStateException("x not allowed");
	
%ex{short.message}
 	
    LOG.error("message", e);	
	LogDemo   [ERROR]  message x not allowed 
	
	LOG.error("message {}", e);		// {} not used for exception
	LogDemo   [ERROR]  message {} x not allowed 
	
	LOG.error("message {}", "foo", e);
	LogDemo   [ERROR]  message foo x not allowed 
	
%ex{short}
	
    LOG.error("message", e);
	LogDemo [ERROR]  message java.lang.IllegalStateException: x not allowed
	  at log2.LogDemo.logException(LogDemo.java:18) // stack trace