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
```

JobRunner

``` Screen  
  while loop
      break on TM Terminate or Shutdown
    loop
      jobStore resetCrashedJobs
      jobStore take job payload
      taskMediator push payload    
```

Monitor
	
TaskMediator

TaskRunner

State Fliper

TM Done - try shutdown
     shutdown set done
     shutdown try shutdown
       if all not done return false
       else change TM state to shutdown
     else
     TM Ready
  TM Shutdown - do nothing



