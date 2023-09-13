Engage SDK Integration Samples
============

Introduction
------------

These sample demonstrates Engage SDK Integration with basic apps utilizing the Read and Watch verticals, the read version being written in Java while the watch version being written in Kotlin.

These sample aim to provide clear and well-documented code examples and cover varied use-cases of Engage SDK so third-party developers can learn how to integrate their existing apps cleanly and efficiently with Engage SDK.

Getting started
---------------
Clone this project to your workstation using a git client. You can use the
[instructions from GitHub](https://docs.github.com/en/free-pro-team@latest/github/creating-cloning-and-archiving-repositories/cloning-a-repository)
if you need guidance.

This project uses the Gradle build system. To build this project, use the
`gradlew build` command or use "Import Project" in Android Studio.

For more resources on learning Android development, visit the
[Developer Guides](https://developer.android.com/guide/) at
[developer.android.com](https://developer.android.com).

For Java developers
-------------------

The version of this sample is contained in the read/ directory. It is written fully in Java. The main focal point of the code is in the read/publish directory, containing all code involved with publishing through Engage SDK.

Specifically, EngageServiceWorker is the class in which all publishing occurs. We publish with [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager), as [recommended by Engage SDK](https://developer.android.com/guide/playcore/engage/publish#workmanager), and EngageServiceWorker is the worker which does the publishing. EngageServiceWorker extends ListenableWorker due to the asynchronous nature of the publishing calls, and we recommend your worker in your app should do the same. Note that we use one worker for publishing, passing in flags indicating which cluster to publish, but you may choose to use multiple workers depending on what fits your app architecture.

The work for EngageServiceWorker is triggered in SetEngageState.java. This is a class with methods to queue up periodic publishing when the app is closed, and one-time publishing for when a broadcast intent for publishing is received by EngageServiceBroadcastReceiver.

The other classes within the directory, such as GetContinuationCluster.java defines methods to build different clusters. These classes are useful for you to see how to build clusters, but the methods to construct entities and the content of the entities within the clusters will look very different within your app.

Outside of that publish/ directory, converters/EbookToEntityConverter.java contains methods to build an Entity for publishing. This class is useful to show how to construct an entity once you have your data that it should hold.

Any more specific information about these classes and the decisions made within them can be found in those classes JavaDocs and the JavaDocs for the methods in that class.

For Kotlin developers
-------------------

The version of this sample is contained in the watch/ directory. It is written fully in Kotlin. The main focal point of the code is in the watch/publish directory, containing all code involved with publishing through Engage SDK.

Specifically, EngageServiceWorker is the class in which all publishing occurs. We publish with [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager), as [recommended by Engage SDK](https://developer.android.com/guide/playcore/engage/publish#workmanager), and EngageServiceWorker is the worker which does the publishing. EngageServiceWorker extends CoroutineWorker due to the asynchronous nature of the publishing calls, and we recommend your worker in your app should do the same. Note that we use one worker for publishing, passing in flags indicating which cluster to publish, but you may choose to use multiple workers depending on what fits your app architecture.

The work for EngageServiceWorker is triggered in Publisher.kt. This is a class with methods to queue up periodic publishing when the app is closed.

ClusterRequestFactory.kt defines a factory to build different publish requests, from constructing the cluster to constructing a request from said cluster. This class is useful for you to see how to build clusters and requests, but the content of the entities within the clusters will look very different within your app.

Outside of that publish/ directory, data/converters/ItemToEntityConverter.kt contains methods to build an Entity for publishing. This class is useful to show how to construct an entity once you have your data that it should hold.

Any more specific information about these classes and the decisions made within them can be found in those classes JavaDocs and the JavaDocs for the methods in that class.

Libraries used
--------------

* [Engage SDK][0] - A library to send data locally to Google's on-device content aggregation surfaces.

[0]: https://developer.android.com/guide/playcore/engage

Support
-------

Please report issues with this sample in this project's issues page:
https://github.com/googlesamples/engage-sdk-samples/issues

License
-------

```
Copyright 2023 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
```
