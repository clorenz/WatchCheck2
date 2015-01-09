package de.uhrenbastler.watchcheck.provider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Date;

import de.uhrenbastler.watchcheck.provider.NtpMessage;

import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 09.01.15.
 */
public class NtpTimeProvider implements ITimeProvider{
    boolean valid=false;
    Date timestamp=null;
    Double offset=null;
    final ConnectivityManager cm;
    final int reconnectCounter;
    final int validCounter;

    int reconnectAttempt=0;
    int validCount=0;

    /**
     *
     * @param cm
     * @param reconnectCounter - every how many-th call of getTime() triggers an NTP reconnect attempt
     * @param validCounter - every how many-th call of getTime() invalidates the NTP data and forces retrieval of the next one
     */
    public NtpTimeProvider(ConnectivityManager cm, int reconnectCounter, int validCounter) {
        this.cm = cm;
        this.reconnectCounter = reconnectCounter;
        this.validCounter = validCounter;

    }


    @Override
    public String getTime() {
        if ( offset==null) {
            if ( reconnectAttempt==0) {
                calculateNtpOffset();
                Logger.info("Calculating NTP offset="+offset);
                reconnectAttempt = reconnectCounter;
            } else {
                reconnectAttempt--;
            }
        }
        if ( offset!=null) {
            timestamp = new Date((long)((double)System.currentTimeMillis() - offset));   // = Localtime - Localtime + Referencetime
            validCount--;
        }

        if ( validCount<=0) {
            offset=null;
            reconnectAttempt=0;
        }
        return valid?sdf.format(timestamp):"--:--:--";
    }

    @Override
    public boolean isValid() {
        return valid;
    }


    private void calculateNtpOffset() {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if ( networkInfo!=null && networkInfo.isConnected()) {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(1000);
                InetAddress address = InetAddress.getByName("europe.pool.ntp.org");
                byte[] buf = new NtpMessage().toByteArray();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);

                // Set the transmit timestamp *just* before sending the packet
                // ToDo: Does this actually improve performance or not?
                NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);
                socket.send(packet);

                // Get response
                //Logger.info("NTP request sent, waiting for response...\n");
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Immediately record the incoming timestamp
                double destinationTimestamp =
                        (System.currentTimeMillis() / 1000.0) + 2208988800.0;


                // Process response
                NtpMessage msg = new NtpMessage(packet.getData());

                // Corrected, according to RFC2030 errata
                double roundTripDelay = (destinationTimestamp - msg.originateTimestamp) -
                        (msg.transmitTimestamp - msg.receiveTimestamp);

                offset =
                        ((msg.receiveTimestamp - msg.originateTimestamp) +
                                (msg.transmitTimestamp - destinationTimestamp)) / 2;

                // offset = LocalTime - ReferenceTime


                // Display response
                //Logger.info(msg.toString());

                //Logger.info("Dest. timestamp:     " +
                //        NtpMessage.timestampToString(destinationTimestamp));

                //Logger.info("Round-trip delay: " +
                //        new DecimalFormat("0.00").format(roundTripDelay * 1000) + " ms");

                //Logger.info("Local clock offset: " +
                //        new DecimalFormat("0.00").format(offset * 1000) + " ms");

                socket.close();
                valid=true;
                validCount = validCounter;
            } catch ( Exception e) {
                Logger.error("Cannot communicate via NTP: ",e);
                valid=false;
            }
        }
    }
}
