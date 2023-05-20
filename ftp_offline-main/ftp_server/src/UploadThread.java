import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.Vector;

public class UploadThread implements Runnable {

    private Socket clientsocket;
    private ftp_server server;

    private BufferedReader inFromClient;
    private PrintWriter outToClient;

    private String id;


    public UploadThread(Socket socket,ftp_server server)
    {
        this.clientsocket = socket;
        this.server = server;
        id = null;
    }

    @Override
    public void run() {

        try {
            System.out.println("In upload Thread");

            outToClient = new PrintWriter(clientsocket.getOutputStream());
            outToClient.println("confirmation");
            outToClient.flush();

            //here check buffer size


            inFromClient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            //get id
            id = inFromClient.readLine();
            System.out.println(id);

            //get Request or not
            String requ_check = inFromClient.readLine();
            System.out.println(requ_check);

            boolean isRequest = (requ_check.equals("Request"));

            //get file_size, privacy and name
            String msg = inFromClient.readLine();
            System.out.println(msg);

            StringTokenizer st = new StringTokenizer(msg, " ");
            String[] str = new String[2];

            int i = 0;
            while (st.hasMoreTokens()) {
                str[i] = st.nextToken();
                i++;
            }

            String privacy = str[0];
            long file_size = Long.parseLong(str[1]);

            System.out.println(privacy);
            System.out.println(file_size);

            String file_name = inFromClient.readLine();
            System.out.println(file_name);


            //randomly generate chunk size
            long chunk_size = getRandomNumber((int)server.getMIN_CHUNK_SIZE(), (int)server.getMAX_CHUNK_SIZE());
            System.out.println("chunk size: " + chunk_size);

            //chunk_size = Math.min(chunk_size,file_size);
            //System.out.println("chunk size: "+chunk_size);


            //generate unique file_id
            String file_id = generate_file_id();
            System.out.println("file_id: " + file_id);

            fileinfo finfo = new fileinfo(file_id, file_name, file_size, privacy, id);

            Vector<fileinfo> files = server.temp_map.get(id);

            if (files == null) {
                files = new Vector<>();
            }

            files.add(finfo);
            server.temp_map.put(id, files);


            outToClient = new PrintWriter(clientsocket.getOutputStream());

            System.out.println("chunk size: " + chunk_size);

            //check if Buffer space is available
//            boolean ok = false;

//            while(file_size > server.getMAX_BUFFER_SIZE())
//            {
//                if(!ok)
//                {
//                    outToClient.println("Buffer_Cap");
//                    outToClient.flush();
//                    ok = true;
//                }
//            }
//
//            try{
//                Thread.sleep(100);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//
//
//            outToClient.println("Buffer_Available");
//            outToClient.flush();


            if (file_size <= server.getMAX_BUFFER_SIZE()) {
                //occupy buffer space
                server.setMAX_BUFFER_SIZE(server.getMAX_BUFFER_SIZE() - file_size);

                //send chunk size to client
                outToClient.println(String.valueOf(chunk_size));
                outToClient.flush();

                //send file_id
                outToClient.println(file_id);
                outToClient.flush();

                //set file path where the file is saved
                String file_path = "files/" + id + "/" + privacy + "/" + file_name;

                FileOutputStream fileOutputStream = new FileOutputStream(file_path);

                DataInputStream dataInputStream = new DataInputStream(clientsocket.getInputStream());

                System.out.println("File uploading ongoing...");
                byte[] buffer = new byte[(int) chunk_size];
                long size = file_size;
                int bytes = 0;

                long cnt = 1;

                //sabotage file upload (simulation during debug session)
                long sabotaged_chunk = getRandomNumber(1, (int) chunk_size);
                System.out.println(sabotaged_chunk);

                //receive chunks one by one

                Vector<Integer> chunk_sizes = new Vector<>();

                while (true) {

                    try {
                        if (size <= 0) {
                            bytes = dataInputStream.read(buffer, 0, 43);

                        } else {
                            bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size));
                            chunk_sizes.add(bytes);
                        }

                        if (bytes == -1) {
                            System.out.println("End of Input Stream");
                            break;
                        }

                    } catch (IOException e) {
                        System.out.println("Server side socket connection reset");
                        //delete the file
                        fileOutputStream.close();
                        clientsocket.close();
                        delete_file(file_path);

                        break;
                    }

                    //check special signal
                    String signal = new String(buffer, 0, 43, StandardCharsets.US_ASCII);
                    //System.out.println("current chunk size: " + bytes);
                    //System.out.println(signal);


                    //check timeout signal
                    if (signal.equals("TIMEOUT_th1s_1s_4n_3ncrypt3d_t1m30ut_s1gn4l")) {
                        System.out.println("Timeout signal received!");
                        System.out.println("File upload crashed!");
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        delete_file(file_path);
                        break;
                    }

                    //check file upload completion
                    if (signal.equals("SUCCESS_th1s_1s_4n_3ncrypt3d_t1m30ut_s1gn4l")) {
                        fileOutputStream.close();

                        //verify file size
                        long total_size = 0;
                        for (long l : chunk_sizes) {
                            total_size += l;
                        }
                        if (total_size == file_size) {
                            //success
                            //add the file in the server hashmap
                            finfo = new fileinfo(file_id, file_name, file_size, privacy, id);

                            files = server.getFileMap().get(id);

                            if (files == null) {
                                files = new Vector<>();
                            }

                            files.add(finfo);
                            server.getFileMap().put(id, files);

                            System.out.println("File upload finished successfully!");

                            //send success message to client
                            //outToClient = new PrintWriter(clientsocket.getOutputStream());

                            try{
                                Thread.sleep(100);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            outToClient = new PrintWriter(clientsocket.getOutputStream());
                            outToClient.println("Upload_success");
                            outToClient.flush();


                            if (isRequest) {
                                //send a message to the person who requested the file
                                inFromClient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
                                String req_id = inFromClient.readLine();

                                System.out.println(req_id);

                                Vector<RequestInfo> requests = server.getRequests();


                                for (RequestInfo request : requests) {
                                    if (request.getRequest_id().equals(req_id)) {
                                        Vector<RequestInfo> msg_queue = server.getRequestMap().get(request.getSender());

                                        RequestInfo requestInfo = new RequestInfo(req_id, file_name, id, request.getSender(), true);
                                        msg_queue.add(requestInfo);

                                        server.getRequestMap().put(request.getSender(), msg_queue);

                                        break;
                                    }
                                }
                            }
                        } else {
                            //failure
                            delete_file(file_path);

                            //send failure message to client
                            //outToClient = new PrintWriter(clientsocket.getOutputStream());
                            outToClient.println("Upload_failure");
                            outToClient.flush();
                        }
                        break;
                    }

                    //write to file
                    fileOutputStream.write(buffer, 0, bytes);
                    fileOutputStream.flush();


                    //subtract chunk size from total file size
                    size -= bytes;

                    //check if debug session and the chunk is to sabotaged or not
                    if (!(server.DEBUG && cnt == sabotaged_chunk)) {
                        //send acknowledgement for a chunk received
                        outToClient.println("Acknowledged");
                        outToClient.flush();
                        //System.out.println("Chunk " + cnt + " is received successfully!");
                        //System.out.println(bytes);
                    }

                    cnt++;
                }

                System.out.println("End of upload process!");

                //restore server buffer space
                server.setMAX_BUFFER_SIZE(server.getMAX_BUFFER_SIZE() + file_size);

                clientsocket.close();


            }

            else
            {
                //buffer overflow logic here
                System.out.println("Buffer Overflow!");
                outToClient.println("Buffer_overflow");
                outToClient.flush();

                clientsocket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
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

    public String generate_file_id()
    {
        Vector<fileinfo> files = server.temp_map.get(id);

        String file_id = "";
        if(files == null)
        {
            file_id = id + "_1";
        }
        else
            file_id = id + "_" + (files.size()+1);

        return file_id;
    }
}
