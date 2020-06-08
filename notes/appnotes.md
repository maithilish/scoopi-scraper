# Scoopi 

## Bootstrap

Module engine contains Scoopi and bootstrap classes. Scoopi main() creates scoopi.bootstrap.Bootstrap and boots it. Bootstrap creates BootConfigs (system and user defined properties) and find the run mode - solo or cluster. If solo, it creates DInjector (di) with SoloModule and creates solo IStore, opens it and sets it SoloModule. For cluster, it creates di with ClusterModule and creates cluster IStore. It then starts Hazelcast cluster and sets opened store to ClusterModule. 

The solo store is used to hold config and defs data in HashMap while cluster store uses Hazelcast distributed map to hold them. 

Next, main() gets di from Bootstrap and from di, gets instance of ConfigsComposer and boots it. The ConfigsComposer use ConfigBuilder to build CompositeConfiguration from config files. It obtains Properties instance, which is effective config created from CompositeConfiguration and stores the Properties object in store.

Next, main() gets instance of DefsComposer from di and boots it. The DefComposer initialises IDef where JsonNode of defs of locator, task, dataDef and steps are created. It builds data objects (such as ItemDefData) from respective json nodes and stores them in store.

Finally, main() gets instance of Scoopi from di where ScoopiEngine gets injected and starts it. The @Provides methods in BaseModule (super of solo and cluster modules) creates data object through data got from store. 

For example, when di creates IPluginDef it injects PluginDefData created by @Provides. Clients get List<Plugin> from IPluginDef which gets the list from injected PluginDefData. Members in cluster creates their own copy of IPluginDef but uses PluginDataDef got from distributed map.

// FIXME - only leader should boot config and def


  
  
  
  
  





	
