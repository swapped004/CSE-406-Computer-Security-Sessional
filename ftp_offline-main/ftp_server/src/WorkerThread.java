import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WorkerThread implements Runnable
{
    int MAX_CHUNK_SIZE = 4000;
    int MIN_CHUNK_SIZE= 500;


    private Socket clientsocket;
    private String id;
    private boolean newcomer;
    private ftp_server server;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;

    public WorkerThread(Socket clientsocket, String id, boolean newcomer,  ftp_server server)
    {
        this.clientsocket = clientsocket;
        this.id = id;
        this.server = server;
        this.newcomer = newcomer;

    }

    @Override
    public void run() {

        System.out.println("In workerThread: "+id);

            while (true) {

                try{
                    inFromClient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));

                    String command = inFromClient.readLine();

                    System.out.println(command);

                    if(command.equals("Lookup"))
                    {
                        String msg = lookup_users();
                        System.out.println("msg: "+msg);
                        outToClient = new PrintWriter(clientsocket.getOutputStream());
                        outToClient.print(msg);
                        outToClient.flush();
                    }

                    else if(command.equals("Lookup_own_files"))
                    {
                        System.out.println("Before Lookup own files");
                        lookup_files(id);
                        System.out.println("After Lookup own files");
                    }

                    else if(command.equals("Lookup_other_files"))
                    {
                        System.out.println("Before Lookup other files");
                        inFromClient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
                        String other_id = inFromClient.readLine();
                        lookup_files(other_id);
                        System.out.println("After Lookup other files");
                    }


                    else if(command.equals("Request"))
                    {
                        String desc = inFromClient.readLine();
                        System.out.println(desc);
                        String req_id = generate_request_id();

                        RequestInfo requestInfo = new RequestInfo(req_id,desc,id,null,false);
                        server.getRequests().add(requestInfo);

                        HashMap<String,Vector<RequestInfo>> requests = server.getRequestMap();



                        //add it to the request queue of other users except himself
                        for(String key:requests.keySet())
                        {
                            if(!key.equals(id))
                            {
                                Vector<RequestInfo> req = requests.get(key);
                                req.add(requestInfo);

                                requests.put(key,req);
                            }
                        }

                    }

                    else if(command.equals("Inbox"))
                    {
                        outToClient = new PrintWriter(clientsocket.getOutputStream());
                        HashMap<String,Vector<RequestInfo>> requests = server.getRequestMap();

                        Vector<RequestInfo> requestInfos = requests.get(id);

                        String msg = "\n";
                        int cnt = 1;
                        for(RequestInfo requestInfo:requestInfos)
                        {
                            msg += cnt+". ";
                            if(!requestInfo.isGrant())
                            {
                                msg+="Req_ID: "+requestInfo.getRequest_id()+" -> Request from "+requestInfo.getSender()+"  ->  "+requestInfo.getDescription();
                            }

                            else
                            {
                                msg+=requestInfo.getSender()+" uploaded a file for req_id: "+requestInfo.getRequest_id()+"  ->  "+requestInfo.getDescription();
                            }

                            msg+="\n";
                            cnt++;

                        }

                        if(msg.equals("\n"))
                        {
                            outToClient.println("No messages to read!");
                            outToClient.flush();
                        }

                        else
                        {
                            outToClient.print(msg);
                            outToClient.flush();
                        }

                        //clear request queue
                        requestInfos.clear();
                        server.getRequestMap().put(id,requestInfos);
                    }

                    else if(command.equals("Logout"))
                    {
                        //remove from hashmap
                        server.getSocketHashMap().remove(id);

                        clientsocket.close();
                        System.out.println("User id: "+id+" logged out of the server");
                        break;
                    }


                    else
                    {
                        System.out.println("invalid command!");
                    }

                }
                catch (SocketException e)
                {
                    System.out.println("Server side connect reset in WorkerThread");
                    //remove from live sockets
                    server.getSocketHashMap().remove(id);

                    break;
                }

                catch (IOException e)
                {
                    System.out.println("Server side connection reset in WorkerThread");
                    //remove from live sockets
                    server.getSocketHashMap().remove(id);

                    break;

                }

            }
    }

    public String lookup_users()
    {
        int cnt = 1;
        String msg = "";
        for(String i:server.getAll_sockets())
        {
            String temp = cnt+". "+i;
            if(server.getSocketHashMap().get(i) != null)
            {
                temp+=" (online)";
            }

            msg+=temp+"\n";
            cnt++;
        }

        return msg;
    }




    public void lookup_files(String id)
    {
        Vector<fileinfo> files = server.getFileMap().get(id);
        String msg = "\n";

        if(this.id.equals(id))
        {
            //show both private and public files

            if(files != null) {
                for (fileinfo file : files) {
                    if (file.getPrivacy().equals("Public")) {
                        msg += file.getFile_id() + "  ->     " + file.getFile_name() + " (Public)"+"\n";
                    }
                }

                for (fileinfo file : files) {
                    if (file.getPrivacy().equals("Private")) {
                        msg += file.getFile_id() + "  ->     " + file.getFile_name() + " (Private)"+"\n";
                    }
                }
            }

            else
            {
                msg+="No files to show\n";
            }
        }
        else
        {
            //show only public files
            if(files != null) {
                for (fileinfo file : files) {
                    if (file.getPrivacy().equals("Public")) {
                        msg += file.getFile_id() + "   ->     " + file.getFile_name() +"\n";
                    }
                }
            }

            else
                msg+="No files to show\n";
        }

        try {
            System.out.println(msg);
            outToClient = new PrintWriter(clientsocket.getOutputStream());
            outToClient.print(msg);
            outToClient.flush();
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    public String generate_request_id()
    {
        int size = server.getRequests().size();

        return "Req_"+size;
    }


}
