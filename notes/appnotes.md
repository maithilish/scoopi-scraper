# Scoopi 

## Bootstrap

Engine module contains Scoopi and bootstrap classes. Scoopi main() creates Bootstrap and calls bootDi. It creates BootConfigs (system and user defined properties) and gets run mode - solo or cluster. If solo, it creates DInjector (DI) with SoloModule else creates DI with ClusterModule. Next, main method calls bootCluster which from di gets IStore and ICluster instances. Actual instance created by DI depends on the module used to create the DI - SoloModule or ClusterModule. It starts the cluster and opens the store which is assigned to the module (its purpose is explained later). The SoloModule returns dummy cluster instance for symetry whereas ClusterModule returns actual cluster instance which starts hazelcast cluster. Next, main calls waitForQuorum to form the cluster quorum. 

Once cluster quorum is formed, bootstrap.setup() is called which sets a barricade and create ConfigsComposer and DefsComposer and call their compose methods. Barricade ensures that only one node (loosely called leader node) is allowed to compose defs and configs and store them in IStore. 

ConfigsComposer loads default, user defined property files and system properties. It builds effective configs from these and stores properties object in IStore.

DefsComposer loads definition files from scoopi.defs.dir and create appropriate defs object and stores them in IStore. The DefComposer initialises IDef where JsonNode of defs of locator, task, dataDef and steps are created. It builds data objects (such as ItemDefData) from respective json nodes and stores them in store.

The @Provides methods defined in BaseModule fetch config and defs data objects from IStore to create instances of configs and defs. For example, DefsComposer (in leader node) creates PluginDefData and stores it in IStore. In any node, when DI creates IPluginDef it wires PluginDefData object returned by @Provides method providePluginDefData() defined in BaseModule which in turn fetches PluginDefData object from IStore. Members in cluster creates their own copy of IPluginDef but uses PluginDataDef got from central IStore.

Next, main method uses DI to create Scoopi instance and to which DI wires ScoopiEngine. Finally, ScoopiEngine's initSystem() and runJobs() is called. 

The initSystem() takes care of starting Stats, Errors, metrics and plugging shutdown hook. It also initializes cluster, cluster listners and job mediator and seed jobs.

The runJobs() starts TaskMediator (TM) and JobMediator (JM) which starts the executor to execute jobs and task in multiple threads.

## Shutdown and Cancel

JM starts JobRunner and Monitor. Monitor is a ScheduledExecutor which monitors shutdown conditions every second (scoopi.monitor.timerPeriod). On each scheduled run, it checks whether payloads queue is empty and all tasks in poolService (poolService.isDone()) are completed. If true, it sets StateFliper.tmState to DONE and then asks StateFliper to try to change its state to SHUTDOWN (from DONE or READY). 

StateFliper first calls IShutdown.setDone() which marks node as done as there is not pending task in node.  Next it calls IShutdown.tryShutdown() (cluster or solo) and pass a function (to change tmState). Shutdown consults jobStore (cluster or solo) and executes the passed function if 1. all nodes and job store done or 2. cancel is set. The executed function changes the tmState to SHUTDOWN. If any node is not done it means that some task is still running in poolService in that node which inturn may create some new job, so all nodes should wait. Same is true if some job is pending in jobStore.

In short, on monitor's schedule run if no task is pending at node level (payload queue is empty and no task pending in pool service) then node is marked as done and like wise if all nodes are done (means no jobs in jobStore and all nodes are done) at that time then StateFliper.TMState is changed to SHUTDOWN.

Once tmState is SHUTDOWN then taskRunner while loop breaks and taskMediator wait marks tmState to TERMINATED. Similarly, jobRunner breaks and joins jobMediator waitForFinish which stops monitor.

The main thread in Scoopi waiting on waitForFinish of JobMonitor and AppenderMonitor passes them and call scoopiEngine.shutdown() in finally block which stops metrics, cluster and exits.

### Shutdown Hook

The JVM can shutdown orderly or abrupt manner. Orderly shut down happens when last normal thread (non-daemon) is terminated or when interrupted with Ctrl-C (SIGINT or kill -2). Abrupt shut down happens when JVM receives kill signal such as SIGTERM or SIGKILL etc (kill -12 or 15) or when Runtime.halt is called.

In abrupt shut down JVM simply halts and exits and shutdown hook is never called. 

Normal shutdown - In Scoopi, if cancel is not requested with Ctrl-C then normal shut down happens. In normal shutdown, main thread blocks and waits for jobMediator and appenderMediator to finish. Once they are finished main thread wakes up and calles scoopiEngine.shutdown(). At this point baring few such as metrics and cluster almost all other normal threads are terminates. When metrics and cluster are stopped, main becomes the last normal thread and it terminates. After last thread terminates JVM invokes regisitered shutdown hook which outputs run status and also closes selenium webdriver and log manager to flush any logs pending in async log appenders.

Cancel the run - If JVM receives Ctrl-C it agains goes for orderly shutdown but immediately starts shutdown hook. The shutdown hook run() sets cancel status by calling scoopiEngine.cancel() which in turn sets cancelled flag in JobRunner and waits for normal shut down as explained above using a countdown latch in shutdownModule. The cancelled flag in jobRunner breaks it and no more jobs are pushed to taskMediator. Whatever running in task pool are eventually are normally completed. When monitor checks the condition IShutdown.tryShutdown() changes tmState to SHUTDOWN as a special case because its cancelled flag is set. The main method calles scoopiEngine.shutdown() which apart from stopping the metrics and cluster as explained above, also count down the shutdownModule countdown latch. The shutdown hook thread waiting on this latch wakes and outputs cancel status and does cleanup such as closing webdriver and log manager and eventually JVM terminates. This is graceful termination as whatever already running jobs are completed but no new jobs are allowed to run.



## Mediators

Engine

``` Screen
  if jobSeedBarricade is allowed, 
      jobSeeder seedLocatorGroups
      jobSeeder awaitForSeedDone 
         with seedLatch, LocatorSeeder count down latch if locator is by def
      jobStore  state Ready
  
  taskMediator start
  jobSeeder awaitForSeedDone
  jobMediator start
  
  waitForJobMediator to finish
  waitForAppenderMediator to finish      
```

JobSeeder

``` Screen
  creates one seed payload for every locatorGroup
  push payloads to TM
```

LocatorSeeder (Step)

``` Screen
  Step input payload is LocatorGroup
  TM creates LocatorSeeder task and pushes to executor  
  handover() 
    creates one payload for each locator in LG
    for groups with multiple tasks, first one is considered so that loader fetches one doc for an locator
    if locator is by def then payload is pushed to JM else if it is by link then to TM
     if locator is by def, count down seed latch 
     if push fails then retry multiple times - scoopi.seeder.seedRetryTimes
```

JobMediator

``` Screen
  start is called after seed is done
  starts JobRunner and monitor
  provides pushJob, pushJobs, markFinsh, resetTakenJob methods
  waitForShutdown
   wait taskMediator finish
   joins JobRunner thread
   stops monitor
   closes jobStore (do nothing)
   cluster shutdown setTerminate
  cancel
   jobRunner cancel 
```

JobRunner

``` Screen  
  while loop
    break on TM Terminate or Shutdown or cancel
    else
      jobStore resetCrashedJobs
      jobStore take job payload
      taskMediator push payload    
  cancel
    set cancel true      
```

Monitor
	
``` Screen
  ScheduledExecutor that runs every scoopi.monitor.timerPeriod
  run
   if payloadStore payload count is 0 and poolService is done
     set stateFliper tmState to done
     stateFliper tryShutdown  
```

State Fliper

``` Screen
  holds TMState 
     READY - accept tasks for execution
     DONE - accepted tasks are completed and no task in pending in taskpool, but may flip to READY state.
     SHUTDOWN - no more tasks are accepted, wait for pool service to end and exit the task runner
     TERMINATE - exit
  tryShutdown()
   clusterShutdown tryShutdown
     consult jobStore and executes shutdown function only if no job pending or cancel is set
   shutdown function - if state id DONE change it to SHUTDOWN
```

TaskMediator

``` Screen
  start taskRunner
  waitForFinish
    taskRunner join
    stateFliper set state to TERMINATE
  push payloads to payloadStore if stateFliper state is READY or DONE
```

TaskRunner

``` Screen
  while loop
    if stateFliper state is SHUTDOWN 
      wait for poolService finish
      break
    else    
      take payload from payloadStore and push pool service
        no payload in store wait (scoopi.task.takeTimeout) else take payload without wait
```


