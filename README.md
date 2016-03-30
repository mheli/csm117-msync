# README #

### What is this repository for? ###

The goal of this project is to synchronize the playing of music on two Android phones. We use the Wi-Fi Peer-to-Peer API available on Android 4.0 (API level 14) to connect the two phones.

We started from the [WiFi Direct Demo](https://android.googlesource.com/platform/development/+/master/samples/WiFiDirectDemo) example from The Android Open Source Project, which is licensed under the Apache License, Version 2.0 (the "License").

### How do I get set up? ###

Import into Android Studio (preferably) and build it.

### Other Info ###
1. Project Name and Team member  
MSync: Elvin Ruslim, Michelle Li, Sahith Dan

2. Goals of the Project  
  + Transfer a music file from one phone to the other.
  + Allow both phones to control playback (play, stop) of the music file.
  + Synchronize playback of the music file.

3. Architecture Details  
  + Wi-Fi Peer-to-Peer (P2P)
The two phones connect directly to each other via Wi-Fi. We use Android 4.0 APIs to discover, request, and connect to peers with the WifiP2pManager class. This class interacts with the Wi-Fi hardware on the phones.  One of the phones becomes the group owner (server) and the other phone becomes the peer (client).
  + File Transfer  
After the connection is made, the client can push a music file to the server. When this transfer is complete, both phones enter the "Synchronization" stage.
  + Synchronization  
At this point, both phones have the same music file ready for playback. However, the server doesn't have the IP address of the client because only the group owner's IP address is provided by the API. To allow bi-directional communication, the client first "synchronizes" with the server by sending its local IP address. The server acknowledges receipt of the client's IP address, and both phones enter the "Music Player" stage.
  + Music Player  
Now that we have bi-directional communication, we enable the play and stop controls for music playback on both phones. We mirror the commands on one phone (the initiator) by sending the same command to the other phone. For example, if stop is pressed on the initiator, we send the command "STOP" to the other phone and the stop the music playback on the initiator. Similarly, if the initiator exits the Music Player stage (by pressing the back button), a "KILL" command is sent to the other phone so that it will also exit the Music Player stage.  
Playing the music file at the same time is not as simple. It takes time for the play command to reach and be processed by the other phone, so if we play the file right after sending the command from the initiator, its playback will always be ahead of the other phone. Our solution is to delay the playback at the initiator by the amount of time it takes the other phone to receive and process the play command. We measure this time by having the initiator ping the other phone to get the round trip time (RTT) of a command. Then the initiator can send the play command and delay its playback by half of the RTT.  
Unfortunately, this solution is not perfect. Network conditions are always changing, so the RTT at one instant may have changed in the next. We attempt to mitigate this by pinging right before sending the play command every time.  
  
4. Difficulties During the Project
  + Playback Start-up Time  
The time it takes to actually start playing the music file depends on factors out of our control (ex. hardware, system load, etc.), so even if we acucrately predicted the amount of time to delay playback at the initiator, the playback on the phones could still be out of sync.
  + Bi-directional Communication  
In the Music Player stage, neither phone is in a fixed client/server role. For example, sending the play command requires this chain of communication: initiator > other phone (ping) | other phone > initiator (return ping) | initiator > other phone (send play). We had some trouble making sure that each phone was ready to receive commands at the right time, and that the phones weren't stuck waiting for a command that wouldn't come.
