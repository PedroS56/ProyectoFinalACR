package proyectofinalacr.hilos;

import proyectofinalacr.NodeData;
import proyectofinalacr.utils.Out;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class ServerMulticast extends Thread{
    private String host="228.1.1.1";
    private int port=2000;
    private NodeData nodeData = NodeData.getInstance();
    private static ServerMulticast instance;

    public static ServerMulticast getInstance(){
        if(null==instance){
            instance= new ServerMulticast();
        }

        return instance;
    }

    private ServerMulticast(){};

    @Override
    public void run(){
        try{
            SocketAddress remote = new InetSocketAddress(host, port);
            NetworkInterface netInterface = NetworkInterface.getByName("en0"); 
            DatagramChannel datagramChannel =DatagramChannel.open(StandardProtocolFamily.INET);
            datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, netInterface);
            datagramChannel.configureBlocking(false);
            Selector sel = Selector.open();
            datagramChannel.register(sel, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
            InetAddress group = InetAddress.getByName(host);
            datagramChannel.join(group, netInterface);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            Out.info("Eco de: "+nodeData.getMyPort());
            Out.log("\n" + nodeData.toString());
            while(true) {
                sel.select();
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    if (key.isWritable()) {
                        DatagramChannel channel = (DatagramChannel) key.channel();
                        buffer.clear();
                        buffer.putInt(nodeData.getMyPort());
                        buffer.flip();
                        channel.send(buffer, remote);
                        continue;
                    }
                }
                Thread.sleep(5000);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
