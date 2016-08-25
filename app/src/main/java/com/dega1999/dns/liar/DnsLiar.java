package com.dega1999.dns.liar;

import android.content.Context;

import com.lfk.justwe_webserver.WebServer.LupinServer;
import com.lfk.justwe_webserver.WebServer.RequestSolve;
import com.lfk.justwe_webserver.WebServer.WebServerDefault;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dega1999 on 20/08/16.
 */
//TODO posso da HTTPS 443, forzare redirect a 80??
public class DnsLiar extends Thread {

    // listen to connect
    private DatagramSocket serverSocket;
    // log / error listener
    private static LupinServer.MessageHandler logResult;
    private Context context;
    private boolean IsRunning;
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    private byte redirectTo[] = {(byte)192,(byte)168,43,1}; //TODO TEMPORANEO

    public DnsLiar(Context context, LupinServer.MessageHandler logResult) {
        super();
        this.context = context;
        DnsLiar.logResult = logResult;
        this.IsRunning = true;

      //  try {

            //setRedirectTo(WebServerDefault.WebServerIp);

            logResult.OnResult("DNS is on to :" + WebServerDefault.WebServerIp + ":53");
        /*} catch (IOException e) {
            e.printStackTrace();
            logResult.OnError("Server IO error");
        }*/
    }

    //TODO unsafe method... can cause crashes.
    private void setRedirectTo(String ip) {
        if(ip==null || ip.isEmpty())
            return;
        int start = 0;
        int idx = ip.indexOf(".");
        if(idx==-1) return;
        try {
            byte tmp[] = new byte[4];
            tmp[0] = (byte)Integer.parseInt(ip.substring(start, idx));
            start = idx + 1;
            idx = ip.indexOf('.',start);
            if(idx==-1) return;
            tmp[1] = (byte)Integer.parseInt(ip.substring(start, idx));
            start = idx + 1;
            idx = ip.indexOf('.',start);
            if(idx==-1) return;
            tmp[2] = (byte)Integer.parseInt(ip.substring(start, idx));
            start = idx + 1;
            tmp[3] = (byte) Integer.parseInt(ip.substring(start));
            System.arraycopy(tmp,0,redirectTo,0,tmp.length);
            redirectTo = tmp;
        } catch (Exception exc) {
            return;
        }
    }

    @Override
    public void run() {
        DatagramSocket serverSocket = null;
        logResult.OnResult("DNS is running..");
        while (IsRunning) {
            try {

                serverSocket = new DatagramSocket(8053);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                byte dnsReq[] = receivePacket.getData();

                for(int i=0;i<sendData.length;i++){
                    sendData[i] = 0;
                }
                sendData = forgeDNSResponse(dnsReq,receivePacket.getLength());
                InetAddress nsClientIPAddress = receivePacket.getAddress();
                int nsClientPort = receivePacket.getPort();
                //logResult.OnResult("DNS is " + nsClientIPAddress);
                DatagramPacket nsPacket = new DatagramPacket(sendData, sendData.length, nsClientIPAddress, nsClientPort);
                serverSocket.send(nsPacket);
                serverSocket.close();

            } catch (IOException e) {
                logResult.OnError(e.getMessage());
            } finally {
                if(serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                }
            }
        }
    }

    private static byte[] toByteArray(ArrayList<byte[]> toAppend) {
        int pckSize = 0;
        for(int i=0;i<toAppend.size();i++){
            pckSize +=toAppend.get(i).length;
        }
        byte packetData[] = new byte[pckSize];

        int offset = 0;
        for(int i=0;i<toAppend.size();i++) {
            for(int j=0;j<toAppend.get(i).length;j++) {
                packetData[offset] = toAppend.get(i)[j];
                offset++;
            }
        }
        return packetData;
    }

    //I prefer to be explicit on how each packet is made.
    private byte[] forgeDNSResponse(byte request[],int pckLen) {
        int knownFieldsLen = 16;
        byte transactionId[] = {request[0],request[1]}; //sequence the same as query
        byte flags[] = {(byte)0b10000001,(byte)0b10000000}; //response flag
        byte questions[] = {request[4],request[5]};
        byte answerRR[] = {0,0b00000001};
        byte authorityRR[] = {0,0};
        byte additionalRR[] = {0,0};

        int hostLen = pckLen-knownFieldsLen;
        System.out.println("\nHOST: ");
        byte host[] = new byte[hostLen];
        for(int i=0;i<hostLen;i++) {
            char ascii = (char) Integer.parseInt(Byte.toString(request[12+i]));
//			System.out.print( " " + ascii);
            host[i] = request[12+i];
        }
        //System.out.println("\nHost request len: " + hostLen);*/
        byte type[] = {request[12+hostLen],request[12+hostLen+1]};
        byte classDNS[] =  {request[12+hostLen+2],request[12+hostLen+3]};
		/*printHex("Type:" ,type);
		printHex("Class:" ,classDNS);*/

        byte rspName[] = {(byte)192,12}; //pointer to hostname
        byte rspType[] = {0,1}; //unused type A
        byte rspClass[] = {0,1}; //unused
        byte rspTTL[] = {0,0,0,(byte)100}; //time to live for this query in seconds
        byte rspDLen[] = {0,4}; //response data len, ip...for redirectTo

        ArrayList<byte[]> responseArray = new ArrayList<byte[]>();
        responseArray.add(transactionId);
        responseArray.add(flags);
        responseArray.add(questions);
        responseArray.add(answerRR);
        responseArray.add(authorityRR);
        responseArray.add(additionalRR);
        responseArray.add(host);
        responseArray.add(type);
        responseArray.add(classDNS);
        responseArray.add(rspName);
        responseArray.add(type); //same as request
        responseArray.add(classDNS); //same as request

//		responseArray.add(rspType);
//		responseArray.add(rspClass);
        responseArray.add(rspTTL);
        responseArray.add(rspDLen);
        responseArray.add(redirectTo);
        return toByteArray(responseArray);
    }


    public void stopServer() {
        IsRunning = false;
        logResult.OnResult("DNS Server close");
    }

    public static LupinServer.MessageHandler getLogResult() {
        return logResult;
    }
}
