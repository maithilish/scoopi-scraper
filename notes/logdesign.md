# Categories

Critical errors 
 - throw CriticalException 
 - catch in ScoopiEngine 
 - terminate the app

Step errors 
 - throw StepRunException
 - catch in Task 
 - terminate the step

Step errors in some items but others are fine
 - catch and log error
 - do not terminate the step


# Log 

info 
 - to output completion of phases and app progress
 - use standard logging statement

warn
 - any minor aspects
 - used by user to correct definitions 
 - use standard logging statement
   
error
 - for critical and step errors
 - used by user to correct definitions 
 - use ErrorLog class
 
debug
 - for critical, step errors and for variables
 - used by developer to debug
 - use standard logging statement
 - apart from message, output stack trace or debug variables
 
trace
 - use MDC to trace the flow of Job and Steps
 - to debug variables
 - used by user to fine tune definitions and correct queries
 - use standard logging statement
 
Notes:  ErrorLog class is used to centralise error logs as we can keep count of errors and output errors through logging mechanism. In errors, we are more interested in error message and not the file name. Hence, the deviation from standard logging practice. ErrorLog also output debug stack trace as it is useful for developers. 
  
         