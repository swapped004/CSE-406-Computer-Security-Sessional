import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Download_concurrent implements Runnable {

    private Socket socket;
    private String id;
    private String file_id;
    private boolean own;
    private BufferedReader inFromServer;
    private PrintWriter outToServer;

    public Download_concurrent(String id, String file_id, boolean own)
    {
        this.id = id;
        this.file_id = file_id;
        this.own = own;

        try{
            socket = new Socket("localhost", 44444);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    @Override
    public void run() {
        try {
            outToServer = new PrintWriter(socket.getOutputStream());
            if (own)
                outToServer.println("Download_own");
            else
                outToServer.println("Download_other");
            outToServer.flush();

            //send ID
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String msg = inFromServer.readLine();
            System.out.println(msg);

            if(msg.equals("Download_acknowledged")) {
                //send ID
                outToServer = new PrintWriter(socket.getOutputStream());
                outToServer.println(id);
                outToServer.flush();


                //System.out.println(file_id);
                outToServer.println(file_id);
                outToServer.flush();

                System.out.println("Starting Download...");

                DownloadThread downloadThread = new DownloadThread(socket, file_id,id);
                downloadThread.download();
            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
