package  proyectofinalacr.hilos;

import proyectofinalacr.NodeData;
import proyectofinalacr.clases.Node;
import proyectofinalacr.utils.Out;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class ClientDownload extends Thread{
    private NodeData nodeData=NodeData.getInstance();
    private String fileName;
    private Node nodeToDownload;

    public ClientDownload(String fileName, Node nodeToDownload){
        this.fileName=fileName;
        this.nodeToDownload=nodeToDownload;
    }

    @Override
    public void run(){
        try{
            String ip=nodeToDownload.getIPaddress().toString();
            ip=ip.substring(1,ip.length());
            int port=nodeToDownload.getRMIport()+100;
            int bufferSize=2048;
            Out.info("Enviando solicitud de descarga a: "+ip+":"+port+"...");
            Socket socket = new Socket(ip,port);
            socket.setReceiveBufferSize(bufferSize);
            socket.setSendBufferSize(bufferSize);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            //Send request
            output.writeUTF(fileName);
            output.flush();
            //Receiving file
            String name = input.readUTF();
            System.err.println(name);
            System.err.println(nodeData.getSource());
            name = nodeData.getSource().getAbsolutePath()+"/"+name;
            byte[] b = new byte[bufferSize];
            long size= input.readLong();
            DataOutputStream fileOutput = new DataOutputStream(new FileOutputStream(name));
            long receive=0;
            int n;
            while(receive < size){
                if(size-receive<bufferSize){
                    n = input.read(b,0,(int)(size-receive));
                }else{
                    n = input.read(b);
                }
                fileOutput.write(b,0,n);
                fileOutput.flush();
                receive = receive + n;
            }//While
            fileOutput.flush();
            fileOutput.close();
            input.close();
            output.close();
            socket.close();
            Out.strong("Archivo \""+fileName+"\" descargado con EXITO!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}