# AndroidCaptivePortal

Forked from: [JustWeEngine](https://github.com/lfkdsk/JustWeEngine)
Files not deployed, yet.


Rules for iptables:

iptables -t nat -A PREROUTING -p udp --dport 53 -j REDIRECT --to-port 8053
iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
