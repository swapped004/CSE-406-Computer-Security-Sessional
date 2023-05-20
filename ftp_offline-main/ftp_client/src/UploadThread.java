import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class UploadThread {

    private File file;
    private long chunk_size;
    private Socket socket;
    private String req_id;

    public UploadThread(File file, long chunk_size, Socket socket, String req_id)
    {
        this.file = file;
        this.chunk_size = chunk_size;
        this.socket = socket;
        this.req_id = req_id;
    }

    public void upload() {

        try {

            System.out.println("File uploading in progress...");

            int bytes = 0;

            FileInputStream fileInputStream = new FileInputStream(file);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            byte[] buffer = new byte[(int)chunk_size];

            long cnt = 1;

            boolean ok = true;

            long sabotaged_chunk = 15;

            while ((bytes = fileInputStream.read(buffer)) != -1) {


                if(!(ftp_client.DEBUG && cnt == sabotaged_chunk)) {
                    //System.out.println(bytes);
                    dataOutputStream.write(buffer, 0, bytes);
                    dataOutputStream.flush();


                }

                try {
                    //wait for 30 sec and then send time out signal
                    socket.setSoTimeout(30 * 1000);
                    String msg = bufferedReader.readLine();

                    if (msg.equals("Acknowledged")) {
                        //System.out.println("chunk " + cnt + " sent successfully!");
                        cnt++;
                    }

                }
                catch (SocketTimeoutException ste)
                {
                    //send time out signal
                    ok = false;

                    System.out.println("Socket timed out!");
                    System.out.println("File uploading failed!");

                    String timeout_string = "TIMEOUT_th1s_1s_4n_3ncrypt3d_t1m30ut_s1gn4l";
                    byte[] byteArrray = timeout_string.getBytes(StandardCharsets.US_ASCII);

                    //System.out.println(byteArrray.length);

                    String signal = new String(byteArrray, StandardCharsets.US_ASCII);
                    //System.out.println(signal);

                    dataOutputStream.write(byteArrray,0,byteArrray.length);
                    dataOutputStream.flush();

                    break;
                }
            }

            if(ok)
            {
                //send completion message that every chunk is sent successfully
                String success_string = "SUCCESS_th1s_1s_4n_3ncrypt3d_t1m30ut_s1gn4l";
                byte[] byteArrray = success_string.getBytes(StandardCharsets.US_ASCII);

                //System.out.println(byteArrray.length);

                //String signal = new String(byteArrray, StandardCharsets.US_ASCII);
                //System.out.println(signal);

                dataOutputStream.write(byteArrray,0,byteArrray.length);
                dataOutputStream.flush();

                System.out.println("File upload done from client side!");


                //get final upload status from server
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg = inFromServer.readLine();
                System.out.println(msg);

                if(msg.equals("Upload_success"))
                {
                    System.out.println("File upload done successfully (From Server)!");

                    if(req_id != null)
                    {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                        printWriter.println(req_id);
                        printWriter.flush();
                    }
                }

                else if(msg.equals("Upload_failure"))
                {
                    System.out.println("file upload failed!");
                }
            }

            //reset that 30 sec timeout
            socket.setSoTimeout(0);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }


    }
}
