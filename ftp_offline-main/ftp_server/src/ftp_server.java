import javafx.util.Pair;
import org.omg.CORBA.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ftp_server {


    //debug variable
    public boolean DEBUG;

    //maintain live sockets and all accounts
    private Set<String> all_sockets;
    private HashMap<String,Socket> socketHashMap;

    //keep track of files of each user
    private HashMap<String,Vector<fileinfo>> fileMap;

    //keep track of all files for generating unique file id during concurrent uploading
    public static HashMap<String,Vector<fileinfo>> temp_map;



    //Max file buffer size for uploading
    private long MAXIMUM_SIZE;
    private long MAX_BUFFER_SIZE;

    //min and max chunk sizes
    private long MIN_CHUNK_SIZE;
    private long MAX_CHUNK_SIZE;


    //to keep track of requests of all users
    private Vector<RequestInfo> requests;
    private HashMap<String,Vector<RequestInfo>> requestMap;

    //others
    private BufferedReader inFromClient;
    private PrintWriter outToClient;


    public ftp_server()
    {
        all_sockets = new HashSet<>();
        socketHashMap=new HashMap<>();
        fileMap = new HashMap<>();
        temp_map = new HashMap<>();
        requestMap = new HashMap<>();
        requests = new Vector<>();

        MAXIMUM_SIZE = 1999999999; //in Bytes
        MAX_BUFFER_SIZE = MAXIMUM_SIZE;

        MIN_CHUNK_SIZE = 500;
        MAX_CHUNK_SIZE = 5000;

        Scanner sc = new Scanner(System.in);
        System.out.println("Choose a Mode:");
        System.out.println("1. NORMAL MODE");
        System.out.println("2. TESTING MODE");
        int val = sc.nextInt();
        System.out.println();
        DEBUG = (val!=1);
        //System.out.println(DEBUG);

        System.out.println("MAX BUFFER SIZE (MB):");
        MAX_BUFFER_SIZE = sc.nextLong()*1024*1024;
        System.out.println(MAX_BUFFER_SIZE);
        MAXIMUM_SIZE = MAX_BUFFER_SIZE;

        System.out.println("MIN CHUNK SIZE (KB):");
        MIN_CHUNK_SIZE = sc.nextLong()*1024;
        System.out.println(MIN_CHUNK_SIZE);

        System.out.println("MAX CHUNK SIZE (KB):");
        MAX_CHUNK_SIZE = sc.nextLong()*1024;

        //internal buffer cap at 65536
        if(MAX_CHUNK_SIZE > 65536)
            MAX_CHUNK_SIZE = 65536;
        System.out.println(MAX_CHUNK_SIZE);
    }

    public void run_server() {
        System.out.println("Server is up and Running");
        try {
            ServerSocket ss = new ServerSocket(44444);

            while (true)
            {
                System.out.println("waiting for connections...");
                Socket socket = ss.accept();

                String id = read_from_client(socket);

                if(id.equals("Upload_Thread"))
                {
                    //Upload Thread detected

                    System.out.println("Upload Thread detected");

                    UploadThread uploadThread = new UploadThread(socket,this);
                    Thread t = new Thread(uploadThread);
                    t.start();
                }

                else if(id.equals("Download_own"))
                {
                    //Download own thread detected
                    System.out.println("Download own Thread");

                    DownloadThread downloadThread = new DownloadThread(socket,this,true);
                    Thread t = new Thread(downloadThread);
                    t.start();


                }

                else if(id.equals("Download_other"))
                {
                    //Download own thread detected
                    System.out.println("Download other Thread");

                    DownloadThread downloadThread = new DownloadThread(socket,this,false);
                    Thread t = new Thread(downloadThread);
                    t.start();


                }

                else {
                    System.out.println("id: " + id + " connected");

                    if (validate_session(socket, id)) {
                        create_session(socket, id);
                        System.out.println("after session");
                    } else {
                        close_invalid_session(socket);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public boolean validate_session(Socket socket, String id)
    {
        if(socketHashMap.get(id) != null)
        {
            return false;
        }

        return true;

    }

    public void close_invalid_session(Socket socket)
    {
        try {
            outToClient = new PrintWriter(socket.getOutputStream());
            outToClient.println("Already a session exists");
            outToClient.flush();

            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void create_session(Socket socket, String id)
    {
        socketHashMap.put(id,socket);
        boolean newcomer = true;

        //if not in all_sockets, create folder
        if(!all_sockets.contains(id))
        {
            //add to all sockets
            all_sockets.add(id);


            Vector<RequestInfo> vec = new Vector<>();
            requestMap.put(id,vec);

            File theDir = new File("files/"+id);
            if (!theDir.exists()){
                theDir.mkdirs();
            }


            String pub_path = "files/"+id+"/public";
            String prv_path = "files/"+id+"/private";

            //public directory
            theDir = new File(pub_path);
            if (!theDir.exists()){
                theDir.mkdirs();
            }

            //private directory
            theDir = new File(prv_path);
            if (!theDir.exists()){
                theDir.mkdirs();
            }


        }

        else
            newcomer = false;

        System.out.println("Successfully created session");

        try {
            outToClient = new PrintWriter(socket.getOutputStream());
            outToClient.println("Successfully created session");
            outToClient.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        //dispatch client thread
        System.out.println("thread dispatched id: "+id);
        WorkerThread workerThread = new WorkerThread(socket,id,newcomer,this);

        Thread t = new Thread(workerThread);
        t.start();

        System.out.println("After Thread");
    }



    public String read_from_client(Socket socket)
    {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String id = inFromClient.readLine();
            return id;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public Set<String> getAll_sockets() {
        return all_sockets;
    }

    public HashMap<String, Vector<fileinfo>> getFileMap() {
        return fileMap;
    }

    public void setFileMap(HashMap<String, Vector<fileinfo>> fileMap) {
        this.fileMap = fileMap;
    }

    public HashMap<String, Socket> getSocketHashMap() {
        return socketHashMap;
    }

    public HashMap<String, Vector<RequestInfo>> getRequestMap() {
        return requestMap;
    }

    public long getMAX_BUFFER_SIZE() {
        return MAX_BUFFER_SIZE;
    }

    public void setMAX_BUFFER_SIZE(long MAX_BUFFER_SIZE) {
        this.MAX_BUFFER_SIZE = MAX_BUFFER_SIZE;
    }

    public Vector<RequestInfo> getRequests() {
        return requests;
    }

    public long getMIN_CHUNK_SIZE() {
        return MIN_CHUNK_SIZE;
    }

    public long getMAX_CHUNK_SIZE() {
        return MAX_CHUNK_SIZE;
    }
}
