import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class ftp_client {

    private BufferedReader inFromServer;
    private PrintWriter outToServer;
    private Socket socket;



    public static boolean DEBUG = false;


    public ftp_client()
    {


    }

    public void run_client()
    {

        try {

            socket = new Socket("localhost",44444);

            System.out.println("Enter your id:");
            Scanner sc = new Scanner(System.in);

            String id = sc.nextLine();

            outToServer = new PrintWriter(socket.getOutputStream(),true);
            outToServer.println(id);

            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = inFromServer.readLine();
            //System.out.println(msg);

            if(!msg.equals("Already a session exists"))
            {
                System.out.println("Successfully created session");

                ClientThread clientThread = new ClientThread(socket,id);
                Thread t = new Thread(clientThread);
                t.start();

            }

            else
            {
                System.out.println("Session closed");
                socket.close();
            }

        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

    }


}
