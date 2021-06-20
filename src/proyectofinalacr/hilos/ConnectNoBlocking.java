package proyectofinalacr.hilos;

import proyectofinalacr.NodeData;

public class ConnectNoBlocking extends Thread{

    private NodeData nodeData= NodeData.getInstance();
    private static ConnectNoBlocking instance;

    public static ConnectNoBlocking getInstance(){
        if(instance==null){
            instance= new ConnectNoBlocking();
        }
        return instance;
    }

    @Override
    public void run(){
        nodeData.connect();
    }
}
