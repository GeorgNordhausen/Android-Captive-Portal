# AndroidCaptivePortal
WebServer forked from: [JustWeEngine](https://github.com/lfkdsk/JustWeEngine)

## Disclaimer
I'm not responsible for anything you do with this app.  
I'm not responsible of any damage you receive using this app.

## What is this? Why?
I built this app to simplify the creation of basic "EvilTwin" captive portal on Android.  

The app is composed by a webserver (JustWeEngine) and a Dns server wrote from scratch.
Once a new device is connected, a DNS server resolves all the dns request with the Webserver IP.  

You can do the same attack with a pc, a raspberry pi or an [esp8266](https://github.com/reischle/CaptiveIntraweb).. but I decided to build one on android, because smartphones are easier to bring everywhere and I wanted something easy to use.

## Requirements  
I suggest to use an old phone, with a sim card with 0 euro on it: even if someone bypass the captive portal in some way... you spend 0.


I used a Nexus 5, with android 6 and **rooted**: the rooting is needed to add 2 iptables rules, to redirect all the incoming traffic from ports 80 and 53 to (Web Server) 8080 and (Dns Server)8053.

The 2 servers runs on the same android service controlled by an unique activity.

Adb Tools are required.  

## How To use it (right now):
The following steps have to be executed once, after each boot of the device, and are valid until the next reboot.

  * Enable debugging on the device
  * Connect the device with USB and on a terminal launchs "adb shell"
  * Execute the following commands:
    * su
    * iptables -t nat -A PREROUTING -p udp --dport 53 -j REDIRECT --to-port 8053
    * iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080  

These steps are always valid:

  * Setup tethering on the device with the exact same name of the original Access point or appending strings like "-Guest" to the name.
  * Enable tethering on the device
  * Servers Ip are **hardcoded** with **192.168.43.1** (default ip on tethering of my device).
  * Launch the app, press the floating button to start Web and Dns Server. 
  * To stop the application click again the floating button.
  
## Under the hood
All DNS requests answer to 192.168.43.1.   
All unknown path redirects to webroot/index.html : right now, this is relative to the asset folder of the android app.  
The asset folder contains a sample login form that you should adapt to your needs

Known path:
  * **any_address/login.cgi**: it should be invoked by a form in an html page, with GET methoed(see sample page). It answers back **Connected**.
  * **any_address/logs**: show saved logs of the application. The logs are saved in a file inside the application/data directory. Actually the logs trace all the login.cgi attempt.

## Roadmap
  - iptables rules should be automatically added by the application: delete & add automatically at every launch.
  - configurable Server IP or automatic detection (now is hardcoded 192.168.43.1).
  - add a new known command/path to **clean** log file
  - add **statistics** page: number of connected people, number of tries and so on, on the device main screen.
  - load file from external memory (eg. sdcard)
  - lower android version requirements
  - configurable root folders from app
  - password protect some pages (logs/clean/stats)
  - enforce redirecting with iptables rules

## Nice to have
  - page cloning: be able to capture the page of another captive portal and automatically change the <form> tag values.


I'm open to collaboration.  
**Have fun!**
