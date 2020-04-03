public class Client {
    private int id;
    private String ip;
    private int port;
    
    public Client(int ID, String ip, int destPort) {
        this.id = ID;
        this.ip = ip;
        this.port = destPort;
    }
    
    public int getId() {
        return id;
    }
    
    public String getIp() {
        return ip;
    }
    
    public int getPort() {
        return port;
    }
}
