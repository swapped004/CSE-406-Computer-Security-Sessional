import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class DownloadThread implements Runnable {

    private ftp_server server;
    private Socket clientsocket;
    private String id;
    private PrintWriter outToClient;
    private BufferedReader inFromClient;
    private boolean own;


    public DownloadThread(Socket cliensocket, ftp_server server,boolean own)
    {
        this.clientsocket = cliensocket;
        this.server =  server;
        id = null;
        this.own = own;

    }

    @Override
    public void run() {
        try {
            System.out.println("In Download Thread");

            outToClient = new PrintWriter(clientsocket.getOutputStream());
            outToClient.println("Download_acknowledged");
            outToClient.flush();

            inFromClient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            //get id
            id = inFromClient.readLine();
            System.out.println(id);

            //get file_id

            String file_id = inFromClient.readLine();
            System.out.println(file_id);

            //download validate and main process
            download_file(file_id,own);


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void download_file(String file_id, boolean own)
    {
        StringTokenizer st = new StringTokenizer(file_id,"_");

        System.out.println(file_id);
        String[] arr = new String[2];
        int i = 0;
        while(st.hasMoreTokens())
        {
            arr[i] = st.nextToken();
            i++;
        }

        String file_owner_id= arr[0];
        String file_number = arr[1];

        Vector<fileinfo> files = server.getFileMap().get(file_owner_id);
        String file_path = null, file_name = null;
        long file_size = 0;

        if(files != null)
        {
            for(fileinfo file:files)
            {
                if(file.getFile_id().equals(file_id))
                {
                    if(!own)
                    {
                        if(file.getPrivacy().equals("Public"))
                        {
                            file_path = file.getFile_path();
                            file_name  = file.getFile_name();
                            file_size = file.getFile_size();
                            break;
                        }

                    }
                    else
                    {
                        file_path = file.getFile_path();
                        file_name  = file.getFile_name();
                        file_size = file.getFile_size();
                        break;
                    }
                }
            }
        }

        System.out.println(file_path);
        System.out.println(file_name);
        System.out.println(file_size);

        if(file_path != null)
        {
            //file exists
            try{
                outToClient = new PrintWriter(clientsocket.getOutputStream());
                outToClient.println("download_start");
                outToClient.flush();

                download(file_path,file_name,file_size);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        else
        {
            //invalid file
            try{
                outToClient = new PrintWriter(clientsocket.getOutputStream());
                outToClient.println("download_invalid");
                outToClient.flush();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    public void download(String file_path, String file_name, long file_size) {

        try {
            //send file_name, file_size and chunk size
            outToClient = new PrintWriter(clientsocket.getOutputStream());
            outToClient.println(file_name);
            outToClient.flush();


            outToClient.println(String.valueOf(file_size)+"_"+String.valueOf(server.getMAX_CHUNK_SIZE()));
            outToClient.flush();

            System.out.println(file_size+"_"+server.getMAX_CHUNK_SIZE());

            String msg = inFromClient.readLine();
            System.out.println(msg);

            FileInputStream fileInputStream = new FileInputStream(file_path);
            DataOutputStream dataOutputStream = new DataOutputStream(clientsocket.getOutputStream());

            byte[] buffer = new byte[(int) server.getMAX_CHUNK_SIZE()];

            long cnt = 1;

            boolean ok = true;

            int bytes = 0;

            long chunk = 1;

            long size = 0;

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));

            while ((bytes = fileInputStream.read(buffer)) != -1) {

                try {
                    dataOutputStream.write(buffer, 0, bytes);
                    dataOutputStream.flush();


                    msg = bufferedReader.readLine();

                    if (msg.equals("Acknowledged")) {
                        //System.out.println("chunk " + cnt + " sent successfully!");
                        cnt++;
                    }

                    System.out.println("Chunk "+chunk+" sent successfully!");
                    size+=bytes;
                    System.out.println("Size done: "+size);
                    chunk++;
                }

                catch (IOException e)
                {
                    ok = false;
                    System.out.println("Connection Error while File downloading");
                    break;
                }
            }

            if(ok)
            {
                outToClient = new PrintWriter(clientsocket.getOutputStream());
                outToClient.println("Download_success");
                outToClient.flush();
            }

            System.out.println("File downloading over...");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
