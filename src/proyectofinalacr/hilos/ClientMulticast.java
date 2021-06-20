package proyectofinalacr.hilos;

import proyectofinalacr.NodeData;
import proyectofinalacr.clases.Node;
import proyectofinalacr.utils.Out;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class ClientMulticast extends Thread{
    private String host="228.1.1.1";
    private int port=2000;
    private NodeData nodeData= NodeData.getInstance();;
    private static ClientMulticast instance;

    public static ClientMulticast getInstance(){
        if(instance==null){
            instance= new ClientMulticast();
        }
        return instance;
    }

    @Override
    public void run(){
        try {
            NetworkInterface netInterface = NetworkInterface.getByName("en0");
            InetSocketAddress dir = new InetSocketAddress(port);
            DatagramChannel datagramChannel = DatagramChannel.open(StandardProtocolFamily.INET);
            datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, netInterface);
            InetAddress group = InetAddress.getByName(host);
            datagramChannel.join(group, netInterface);
            datagramChannel.configureBlocking(false);
            datagramChannel.socket().bind(dir);
            Selector sel = Selector.open();
            datagramChannel.register(sel, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            Out.info("Obteniendo nodos de la red...");
            while(true){
                sel.select();
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                while(it.hasNext()){
                    SelectionKey key = (SelectionKey)it.next();
                    it.remove();
                    if(key.isReadable()){
                        DatagramChannel channel = (DatagramChannel)key.channel();
                        buffer.clear();
                        SocketAddress emisor = channel.receive(buffer);
                        buffer.flip();
                        int PortOfMember=buffer.getInt();
                        //If the node do not contains the member, just added.
                        if(!nodeData.containsMember(PortOfMember)){
                            InetSocketAddress address = (InetSocketAddress)emisor;
                            nodeData.addNewMemberToRing(new Node(address.getAddress(), PortOfMember));
                        }
                        continue;
                    }
                }//while
            }//while
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
