stuytorrent
===========

A custom bit torrent client

Current Status
==============
`java Driver <port>` will start a Client listening at that port number. If you type `connect <other port>`, it will try to connect to another client on that port. Typing `peers` will show a clients current peers. Connections are reciprocal, in that if a client on 2001 connects to a client on 2000, the client on 2000 is also conneted to 2001. Typing anything else will send that as a message to all of a clients peers.

Final Goal
===========
A client will read a `.torrent` file downloaded beforehand. It will then connect to a tracker, downloading a list of peer ip addresses. Then it will connect to those peers, and remain open for further connections from other peers. It will then begin requesting parts of the file from its peers and responding to similar requests.

Current Limitations (to make it doable)
-----------------------------------------
- we will only support single-file torrents
- we will store the entire file in RAM until it the download is completed, so beware large files

Classes
========

Client
--------
- ClientConnectionRunner

Torrent
--------
- connector

Peer
----
- Sender
- Reciever

Bifield
--------

Piece
-----