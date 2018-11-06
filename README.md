# bt-beacon-example
Android bluetooth beacon component used for indoor ranging
## Setup on raspberry pi
Start hcitool as a daemon on startup to transmit Eddystone-UID
```
hcitool -i hci0 cmd 0x08 0x0008 1e 02 01 1a 1a ff 4c 00 02 15 e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 12 34 00 01 66 00
```
* a 10-byte namespace identifier of 00010203040506070809
* a 6-byte instance identifier of 010203040506
* a zero meter Tx Power level of e7 (-25 dBm).
## Demo
https://drive.google.com/open?id=0B5klJ0ynFOn3UFpFTU95elBqTlk
