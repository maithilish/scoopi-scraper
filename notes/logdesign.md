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
 - count error in Errors

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

## Exception stacktrace

In a logging statement, if exception is last argument then stacktrace is printed with %ex or %rEx patterns. For, console use %ex{short.message}, for error log file %ex{short}, for debug file %ex (full tracktrace but without nested exceptions) and for trace %rEx (full tracktrace including nested exceptions)

	Exception e = new IllegalStateException("x not allowed");
	
        LOGGER.error("some exception", e);	
	LogDemo:19           [ERROR]  some exception
	+ stacktrace
	
        LOGGER.error("some exception {}", "foo", e);	
	LogDemo:21           [ERROR]  some exception foo
	+ stacktrace

        LOGGER.error("some exception {}", e, "foo");	
	LogDemo:23           [ERROR]  some exception java.lang.IllegalStateException: not allowed
        
        LOGGER.error("some exception {}", e.getMessage());	
	LogDemo:25           [ERROR]  some exception not allowed

