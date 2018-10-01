import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
/**
 * SocketServer
 * Created by davidkrystall on 9/28/18.
 */
public class SocketServer extends Thread{
    public ArrayList<String> connections = new ArrayList<String>();

    public ArrayList list() {
        return connections;
    }

    public static void main(String[] args) {
        
        try

        {

            ServerSocket serverSocket = new ServerSocket(1625);

            Socket communicationSocket = serverSocket.accept();

            ObjectInput objInput = new ObjectInputStream(communicationSocket.getInputStream());

            ObjectOutput objOutput = new ObjectOutputStream(communicationSocket.getOutputStream());
            
            System.out.println("Server is running on: "+ InetAddress.getLocalHost().getHostAddress());
            //Add connections here 
            
            while (true)

            {
                

                String tmp = (String)objInput.readObject();

                System.out.println("Client --> " + tmp);

                Scanner sc = new Scanner(System.in);

                String inString = sc.nextLine();

                objOutput.writeObject(inString);

            }

        } catch (Exception e)

        { }

    }
}
