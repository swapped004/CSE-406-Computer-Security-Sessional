import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.Vector;

public class DownloadThread {

    private long chunk_size;
    private Socket socket;
    private String file_id;
    private String id;

    public DownloadThread(Socket socket, String file_id, String id)
    {
        this.socket = socket;
        this.file_id = file_id;
        this.id = id;
    }

    public void download() {

        try {
            //download file
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = bufferedReader.readLine();

            if(msg.equals("download_start"))
            {
                //create a folder if it already does not exist
                File theDir = new File("downloads/"+id);
                if (!theDir.exists()){
                    theDir.mkdirs();
                }


                System.out.println("Downloading...");

                String file_name = bufferedReader.readLine();
                System.out.println("File Name: "+file_name);

                msg = bufferedReader.readLine();
                //System.out.println(msg);

                StringTokenizer st = new StringTokenizer(msg,"_");

                String[] str = new String[2];
                int i = 0;
                while(st.hasMoreTokens())
                {
                    str[i] = st.nextToken();
                    i++;
                }


                long file_size = Long.parseLong(str[0]);
                System.out.println("File Size: "+file_size);
                long chunk_size = Long.parseLong(str[1]);
                System.out.println("Chunk Size: "+chunk_size);

                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println("Acknowledge_download_start");
                printWriter.flush();

                long size = file_size;

                int bytes = 0;

                String file_path = "downloads/"+id+"/"+file_name;
                System.out.println(file_path);

                FileOutputStream fileOutputStream = new FileOutputStream(file_path);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                byte[] buffer = new byte[(int)chunk_size];

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream());

                long cnt = 1;
                boolean ok = true;
                while (true) {

                    try {
                           if(size <= 0)
                               break;

                           //System.out.println(size);

                           bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size));
                           //System.out.println("bytes: "+bytes);
                           if (bytes == -1) {
                               System.out.println("End of Input Stream");
                               break;
                           }
                           fileOutputStream.write(buffer,0,bytes);
                           fileOutputStream.flush();
                           size-=bytes;

                           outToClient.println("Acknowledged");
                           outToClient.flush();
//                           System.out.println("bytes: "+bytes);
//                           System.out.println("size: "+size);
//                           System.out.println("Chunk "+cnt+" received successfully!");

                           cnt++;
                    }

                    catch (IOException e)
                    {
                        System.out.println("Server side socket connection reset");
                        ok = false;
                        //delete the file
                        fileOutputStream.close();
                        delete_file(file_path);

                        break;
                    }
                }

                if(ok)
                {
                    fileOutputStream.close();

                    //receive completion message from Server
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    msg = bufferedReader.readLine();

                    if(msg.equals("Download_success"))
                    {
                        System.out.println("Successfully Downloaded!");
                    }

                    else
                    {
                        System.out.println("Error Downloading");
                        delete_file(file_path);
                    }


                }

                //System.out.println("Download Thread End!");
            }

            else if(msg.equals("download_invalid"))
            {
                System.out.println("Invalid download!");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void delete_file(String file_path)
    {
        File myObj = new File(file_path);
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file: "+myObj.getName());
        }
    }
}
