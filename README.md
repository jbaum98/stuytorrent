stuytorrent
===========

A custom bit torrent client

Current Status
==============
`java Driver <path to .torrent file>` will connect to the tracker, download a list of peers and connect to them, and begin downloding the file.

Current Limitations
-------------------
- the download doesn't complete correctly. Even though each piece is hashed correctly, there are missing chunks of data in the written file
- the program uses a lot of RAM, because everything is stored in RAM until the end.
- only torrents that use http trackers are supported (no udp)
- only single file torrents are supported

Dev Log
---------------
Vacation: Read about [BitTorrent Specification](https://wiki.theory.org/BitTorrentSpecification)

January 4: Began to write a Bencoding decoder, because the .torrrent metainfo file and HTTP tracker communication are use Bencoding.

January 5: Finish a functioning Bencoding decoder.

January 6: Completely rewrote Bencoding decoder with a Max's new algorithm and packaged into a class that extends Map and calls the Bencoding decoder in its constructor.

January 9: Began work on creating and recieving conenctions with Sockets. As an initial goal, we created a program that could connect to other instances of itslef as well as recieve such connections, and send user input back and forth.

Januray 12: Refactoring and comments for our code reviewers. Improved parsing .torrent metainfo files.

January 13: Used longs for Bencoding integers because ints can be too small.

January 14: Fixed an issue with parsing the metainfo file. Because part of the file is a concatenation of SHA1 hashes, which are raw bytes, we have to specify the correct encoding to ensure that the length of the string is correct so the Bencoding Parser works. Started Piece and Bitfield classes, as well as a Bencoding Writer.

January 15: Moved SHA1 encoding to its own class. Continued work on Peers, which was based on the demo code. Added a Death class to close Peers after a timeout.

January 16: Refactored work from previous day, and added url encoding option for SHA1 hashes.

January 17: Spent many hours trying to debug issue where ASCII is not a one to one encoding, so by converting raw bytes of SHA1 hash to a String and then converting back to bytes, the hash gets messed up. Solved by using ISO-8859-1 encoding which is one to one. By the end of the day, we could communicate with the tracker to get a list of peers, and exchange handshakes with those peers.

January 18: Refactored previous day, commented everything.

January 20: Rename the old Message class to Bencoding Map so we can create a new Message abstract class as a parent of the various message classes.
January 21: Continued work on Messages, added Binteger class to handle conversion from Java int to 4-byte big-endian integers needed in peer messaging protocl.

January 22: Finished Reciever, which packages bytes into Messages and adds them to a buffer, and Responder, that reads from that buffer to process each Message. Also replaced booleans with AtomicBooleans where necessary.

January 24: Added a Downloader to send Requests to peers for pieces. We had issues with running the program on the laptop because the RAM was not big enough, but we thought that was caused by changing some ints to longs. We also removed the Binteger class because it was largely uneccessary now that we used DataInputStream for reading from Peers. After debugging for hours and catching many silly errors and removing deadlocks, we seemed to be downloading all of the file except for the last couple of pieces, which no one had. We couldn't figure it out and left it until the morning...

January 25: ...when we realized we were converting bytes into booleans from left to right instead of right to left when we created our Bitfields, misreading their messages. Fixing that problem only caused other all of our Peers to suddenly close the connections, which we found was caused by miscalculating the size of the last piece, resulting in nonsenical requests. By noon we could download the entire file and write it to the disk. However, it was corrupted and when we examine it with a hex editor we found gaps of 0s, even though each piece was hashed and confirmed. When we tried to change how we downloaded pieces to avoid the issue, the download stopped functioning.
