package proyectofinalacr;

import proyectofinalacr.clases.Node;
import proyectofinalacr.hilos.ClientDownload;
import proyectofinalacr.hilos.ClientMulticast;
import proyectofinalacr.hilos.ServerMulticast;
import proyectofinalacr.hilos.ServerUpload;
import proyectofinalacr.utils.FilesUtil;
import proyectofinalacr.utils.Out;
import proyectofinalacr.utils.portSorter;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import proyectofinalacr.MainFrame;

public class NodeData implements searchInterface{
    private Node myInfo, siguiente, anterior;
    private ArrayList<Node> nodesInNet;
    private boolean initializedNode;
    private static Registry registry;
    private static NodeData instance;
    private File source;

    public static NodeData getInstance(){
        if(instance==null) {
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName("en0");
                instance = new NodeData(networkInterface.getInetAddresses().nextElement());
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private NodeData(InetAddress myAddress){
        this.myInfo= new Node(myAddress);
        this.nodesInNet= new ArrayList<>();
        this.initializedNode = false;
    }

    

    public void initializeNode(){
        this.nodesInNet.add(myInfo);
        updateAntSigRMI();
        this.initializedNode=true;
        MainFrame.getWindow().updateTable();
    }

    public boolean isInitialized(){
        return this.initializedNode;
    }

    private void updateAntSigRMI(){
        nodesInNet.sort(new portSorter());
        //Updating RMIports
        int RMIPort=9000;
        for(int i=0; i<nodesInNet.size();i++){
            nodesInNet.get(i).setRMIport(RMIPort);
            RMIPort++;
        }
        if(nodesInNet.size()==1){
            siguiente=anterior=myInfo;
        }else{
            int myIndex=nodesInNet.indexOf(myInfo);
            siguiente=(myIndex==(nodesInNet.size()-1))?nodesInNet.get(0):nodesInNet.get(myIndex+1);
            anterior=(myIndex==0)?nodesInNet.get(nodesInNet.size()-1):nodesInNet.get(myIndex-1);
        }
        if(!initializedNode){
            initializeRMIServer(); 
        }else{
            restartRMIServer(); 
        }
    }

    public boolean containsMember(final int portMember){
        return nodesInNet.stream().anyMatch(o -> o.getMyPort()==portMember);
    }

    public void connect(){
        ClientMulticast.getInstance().start(); 
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Out.info("Inizializando...");
        initializeNode();
        ServerMulticast.getInstance().start(); 
        MainFrame.getWindow().enableBusqueda(); 
    }

    public void addNewMemberToRing(Node newMember) {
        this.nodesInNet.add(newMember);
        if(initializedNode){
            updateAntSigRMI();
            MainFrame.getWindow().updateTable();
            
        }
        Out.log("\n"+this.toString());
    }

    public boolean isMyIndex(int index){
        return nodesInNet.get(index).equals(myInfo);
    }

    private void initializeRMIServer(){
        String ip=this.getMyIPAddress().toString();
        ip=ip.substring(1,ip.length());
        int port=this.myInfo.getRMIport();
        registry=null;
        try {
            //puerto default del rmiregistry
            try{
                registry=java.rmi.registry.LocateRegistry.createRegistry(port);
            }catch(Exception f){
                registry=java.rmi.registry.LocateRegistry.getRegistry(port); 
            }
        } catch (Exception e) {
            System.err.println("Excepcion RMI del registry:");
            e.printStackTrace();
        }
        try {
            System.setProperty("java.rmi.server.codebase","file:/C:/Temp/searchInterface/");
            System.setProperty("java.rmi.server.hostname", ip);
            searchInterface stub = (searchInterface) UnicastRemoteObject.exportObject(instance, 0);
            try {
                registry.bind("searchInterface", stub); 
            }catch(Exception f){
                registry.rebind("searchInterface", stub); 
            }
            Out.info("Servidor RMI listo...");
        } catch (Exception e) {
            System.err.println("Exception en el : " + e.toString());
            e.printStackTrace();
        }
    }

    private void restartRMIServer(){
        try {
            UnicastRemoteObject.unexportObject(instance, true);
            Thread.sleep(2000);
            initializeRMIServer();
        } catch (NoSuchObjectException | InterruptedException e) {
            System.err.println("Exception en servidor: " + e.toString());
            e.printStackTrace();
        }
    }


    
    public void buscaArch(String fileName){
        if(FilesUtil.isFileOnDirectory(fileName,this.source)){
            Out.strong("Archivo encontrado en este nodo");
        }else{ 
            String ip=siguiente.getIPaddress().toString();
            ip=ip.substring(1, ip.length());
            int indexToAsk=nodesInNet.indexOf(siguiente); //
            int founded=-1;
            try{
                Registry registry = LocateRegistry.getRegistry(ip, siguiente.getRMIport());
                searchInterface stub = (searchInterface) registry.lookup("searchInterface");
                    Out.info("Preguntando a "+nodesInNet.get(indexToAsk).toString()+"...");
                    founded = stub.buscaArchRemote(nodesInNet.indexOf(myInfo),indexToAsk, fileName);
                    if (founded == -1) {
                        Out.error(nodesInNet.get(indexToAsk).toString()+" NO encontrado...");
                        Out.error("El archivo no esta en la red");
                    }else{
                        Out.strong("El archivo se encontro en: "+nodesInNet.get(founded).toString()+"!");
                        new ClientDownload(fileName, nodesInNet.get(founded)).start();//Start download
                    }
                    
            }catch (Exception e){
                System.err.println("Algo salio mal...");
                e.printStackTrace();
            }
        }
    }

    @Override
    public int buscaArchRemote(int indexStart,int indexNode, String fileName) throws RemoteException {
         Out.info("Alguien busca el archivo: \""+fileName+"\"");
            boolean IHaveIt=FilesUtil.isFileOnDirectory(fileName,this.source);
            if(IHaveIt){
                Out.strong("Tengo el archivo\""+fileName+"\"");
                new ServerUpload().start();
                return nodesInNet.indexOf(myInfo);
            }else{
                Out.error("No tengo el archivo  \""+fileName+"\"");
                if(nodesInNet.indexOf(siguiente) == indexStart){
                    return -1;
                }else{
                    try {
                        int indexToAsk=nodesInNet.indexOf(siguiente);
                        Out.info("Preguntando a "+nodesInNet.get(indexToAsk).toString()+"...");
                        String ip=siguiente.getIPaddress().toString();
                        Registry registry = LocateRegistry.getRegistry(ip.substring(1, ip.length()), siguiente.getRMIport());
                        searchInterface stub = (searchInterface) registry.lookup("searchInterface");
//                        int founded= stub.buscaArchRemote(indexStart,indexNode,fileName);
                        return stub.buscaArchRemote(indexStart,indexNode,fileName);
                    }catch (Exception e){
                        System.err.println("Something went wrong...");
                        e.printStackTrace();
                        return -1;
                    }
                }
            }
    }
    @Override
    public String toString(){
        return ((initializedNode)?("Yo: "+myInfo.toString()+"\n"
                +"Siguiente: "+siguiente.toString()+"\n"
                +"Anterior: "+anterior.toString()+"\n"):"")
                +"Nodos: "+nodesInNet.toString();
    }
    
    public int getMyRMIPort() throws Exception{
        if(!initializedNode)
            throw new Exception("Nodo no inicializado", new Throwable("Inicia el nodo para usar el metodo..."));
        return this.myInfo.getRMIport();
    }

    public int getMyPort(){
        return this.myInfo.getMyPort();
    }

    public void setMyPort(int Port){
        this.myInfo.setMyPort(Port);
    }

    public InetAddress getMyIPAddress(){
        return this.myInfo.getIPaddress();
    }

    public Node getMyInfo() throws Exception{
        return this.myInfo;
    }

    public Node getSiguiente() throws Exception{
        return this.siguiente;
    }
    public Node getAnterior() throws Exception{
        return this.anterior;
    }

    public ArrayList<Node> getNodesInNet() {
        return nodesInNet;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }
}
