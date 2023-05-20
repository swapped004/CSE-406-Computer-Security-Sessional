import java.io.*;
import java.net.Socket;

public class Upload_concurrent implements Runnable {

    private String id;
    private File file;
    private String req_id;
    String privacy;

    private Socket socket;
    private BufferedReader inFromServer;
    private PrintWriter outToServer;


    public Upload_concurrent(String id, File file, String req_id, int port, String privacy) {
        this.id = id;
        this.file = file;
        this.req_id = req_id;
        this.privacy = privacy;

        try {
            //System.out.println(port);
            socket = new Socket("localhost", port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try{

            System.out.println("In upload Thread!");

            outToServer = new PrintWriter(socket.getOutputStream());

            outToServer.println("Upload_Thread");
            outToServer.flush();

            //get confirmation
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String conf = inFromServer.readLine();
            System.out.println(conf);


            //System.out.println("user_id: "+id);

            //send id
            outToServer = new PrintWriter(socket.getOutputStream());
            outToServer.println(id);
            outToServer.flush();


            //tell server if it is request response or not
            if(req_id == null)
            {
                outToServer.println("Not_Request");
            }

            else
                outToServer.println("Request");

            outToServer.flush();



            //uploading code

            try {
                try {
                    Thread.sleep(0);

                    //send_file_size, privacy and file_name
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println(privacy+" "+file.length());
                    System.out.println("File Size: "+file.length()+" bytes");
                    outToServer.flush();

                    outToServer.println(file.getName());
                    outToServer.flush();

                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    //here check server buffer overflow
//                    String buff_check = inFromServer.readLine();
//
//                    if(buff_check.equals("Buffer_Cap"))
//                    {
//                        System.out.println("Server Buffer is maxed out at the moment! Please wait for the buffer to be available...");
//                        buff_check = inFromServer.readLine();
//
//                    }
//
//                    if(!buff_check.equals("Buffer_Available"))
//                    {
//                        System.out.println("Error in buffer message");
//                        socket.close();
//                        return;
//                    }
//
//                    System.out.println("Buffer Available Now!");

                    //receive chunk size


                    String msg = inFromServer.readLine();




                    //receive file_id
                    String file_id = inFromServer.readLine();


                    if(!msg.equals("Buffer_overflow"))
                    {
                        long chunk_size = Long.parseLong(msg);
                        System.out.println("Chunk Size: " + chunk_size+" bytes");
                        System.out.println("File id: "+file_id);
                        //disassemble the file into chunks

                        System.out.println("File uploading underway...");
                        UploadThread uploadThread = new UploadThread(file, chunk_size, socket,req_id);
                        uploadThread.upload();
                    }

                    else
                    {
                        System.out.println("Currently Server Buffer is maxed out! Try again later");
                        socket.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            catch (InterruptedException f)
            {
                f.printStackTrace();
            }


            //close the Upload Socket after a while
            try {
                Thread.sleep(100);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }

            socket.close();

        }

        catch (IOException e)
        {
            e.printStackTrace();
        }



    }
}
