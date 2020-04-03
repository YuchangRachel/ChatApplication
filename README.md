# Two projects

**1project**: Peer2peer TCP socket programming -> develope a simple chat application for message exchange among remote peers.

**2project**: Distance Vector Routing Protocols -> implement DV algorithm, and protocol runs on top of servers/behaving as routers using UDP, finally constructing routing table for each router.


## How to run:
### 1project:
    1step: After compiling(java file), java Chat #port
    2step: type commands: help, myport, myip, connect, list, terminate, send, exit

### 2project:
    0step: After compiling(compile Node.java, Server.java, Client.java, DV.java), java DV
    1step: modify ip and port in topologyfile
    2step: server -t node1.txt -i interval
    3step: update myid serverid2 linkcost
    4step: step(update right away or not step command then periodic change)
    5step: command inputs: packets, display, disable serverid, crash

