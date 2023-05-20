import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public class ClientThread implements Runnable {
    private Socket socket;
    private String string;
    private BufferedReader inFromServer;
    private PrintWriter outToServer;
    private String id;
    private Vector<Integer> socket_ports;



    public ClientThread(Socket socket, String id) {
        this.socket = socket;
        this.id = id;

        socket_ports = new Vector<>();
        socket_ports.add(44444);
    }

    @Override
    public void run() {
        //System.out.println("In clientThread");

        try {

            while (true) {
                String choice = menu();

                if (choice.equals("1")) {
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println("Lookup");
                    outToServer.flush();

                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    char[] msg = new char[10000];
                    inFromServer.read(msg);

                    System.out.println("Username (Status)");
                    System.out.println(msg);
                }

                else if(choice.equals("2"))
                {
                    System.out.println("Choose an upload method:");
                    System.out.println("1. Personal Upload");
                    System.out.println("2. Respond to a Request");
                    Scanner sc = new Scanner(System.in);

                    String val = sc.nextLine();

                    if(val.equals("1"))
                        upload_file(false);
                    else if(val.equals("2"))
                        upload_file(true);
                    else
                        System.out.println("Invalid Command!");
                }

                else if(choice.equals("3"))
                {
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println("Lookup_own_files");
                    outToServer.flush();

                    //System.out.println("here");

                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    char[] msg = new char[10000];
                    inFromServer.read(msg);

                    //System.out.println("there");

                    System.out.println("Your files:");
                    System.out.println("file_id ->   File Name (Privacy)");
                    System.out.println(msg);

                    //options to download own files
                    download_file(true);
                }

                else if(choice.equals("4"))
                {
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println("Lookup_other_files");
                    outToServer.flush();


                    System.out.println("Type the username whose files you want to see:");
                    Scanner sc = new Scanner(System.in);

                    String other_id = sc.nextLine();

                    outToServer.println(other_id);
                    outToServer.flush();

                    //System.out.println("here");

                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    char[] msg = new char[10000];
                    inFromServer.read(msg);

                    //System.out.println("there");

                    System.out.println("Files of user: "+other_id);
                    System.out.println("file_id ->   File Name (Privacy)");
                    System.out.println(msg);

                    download_file(false);

                }

                else if(choice.equals("5"))
                {
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println("Request");
                    outToServer.flush();

                    System.out.println("Write a short file description:");
                    Scanner sc = new Scanner(System.in);

                    String desc = sc.nextLine();
                    outToServer.println(desc);
                    outToServer.flush();

                    System.out.println("Request sent successfully! Wait for other users to upload your requested file...");


                }

                else if(choice.equals("6"))
                {
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println("Inbox");
                    outToServer.flush();

                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    char[] buffer = new char[10000];
                    inFromServer.read(buffer);

                    System.out.println("--------Inbox---------");
                    System.out.println(buffer);
                }

                else if(choice.equals("10"))
                {
                    outToServer = new PrintWriter(socket.getOutputStream());
                    outToServer.println("Logout");
                    outToServer.flush();

                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    System.out.println("Logged out successfully");
                    socket.close();

                    System.exit(0);

                    break;
                }


            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public String menu()
    {
        System.out.println("Choose an option: ");
        System.out.println("1. View all users");
        System.out.println("2. Upload files");
        System.out.println("3. Lookup own files");
        System.out.println("4. Lookup files of others");
        System.out.println("5. Request files");
        System.out.println("6. See unread messages");
        System.out.println("10. Logout");

        Scanner sc = new Scanner(System.in);
        String val = sc.nextLine();

        return val;
    }

    public void upload_file(boolean request)
    {
        String privacy = null;
        String req_id = null;
        Scanner sc = new Scanner(System.in);

        if(!request) {
//            try {
//                outToServer = new PrintWriter(socket.getOutputStream());
//                outToServer.println("Upload");
//                outToServer.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            System.out.println("Choose privacy status: ");
            System.out.println("1. Public");
            System.out.println("2. Private");



            String choice = sc.nextLine();


            //send_privacy
            privacy = (choice.equals("1")) ? "Public" : "Private";
        }

        else
        {
//            try {
//                outToServer = new PrintWriter(socket.getOutputStream());
//                outToServer.println("Respond_request");
//                outToServer.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            System.out.println("Type Request ID: ");

            sc = new Scanner(System.in);

            req_id = sc.nextLine();


            //send_privacy
            privacy = "Public";

        }


        System.out.println("Choose File to Upload: ");

        //public directory
        File theDir = new File("files");
        if (!theDir.exists()){
            theDir.mkdirs();
        }

        File folder = new File("files");
        File[] listOfFiles = folder.listFiles();

        HashMap<Integer,File> fileHashMap = new HashMap<>();

        for (int i = 1; i <= listOfFiles.length; i++) {
            if (listOfFiles[i-1].isFile()) {
                System.out.println("File " + i +" -> "+listOfFiles[i-1].getName());
                fileHashMap.put(i,listOfFiles[i-1]);
            } else if (listOfFiles[i-1].isDirectory()) {
                System.out.println("Directory " + i + " -> " + listOfFiles[i-1].getName());
            }
        }

        int choice = sc.nextInt();

        File file = null;

        if(fileHashMap.get(choice) != null)
        {
            file = fileHashMap.get(choice);
        }


        //from here upload concurrent

        Upload_concurrent upload_concurrent = new Upload_concurrent(id,file,req_id,44444,privacy);
        Thread t  = new Thread(upload_concurrent);
        t.start();

        System.out.println("after upload Thread!");
    }

    public void download_file(boolean own)
    {
        //download own file

            System.out.println("1. Download File");
            System.out.println("2. Go back");

            Scanner sc = new Scanner(System.in);
            String choice = sc.nextLine();

            if(choice.equals("1"))
            {
                System.out.println("Type the file_id of the file you want to download:");
                sc = new Scanner(System.in);
                String file_id = sc.nextLine();
                StringTokenizer st = new StringTokenizer(file_id, "_");

                String[] arr = new String[2];

                int cnt = st.countTokens();

                int i = 0;
                while(st.hasMoreTokens())
                {
                    arr[i] = st.nextToken();
                    i++;
                }

                if((!arr[0].equals(id)) && own)
                {
                    System.out.println("You don't own the file!");
                }

                else if(cnt != 2)
                {
                    System.out.println("Invalid file_id");
                }

                else
                {
                    try{
                        //valid file_id
                        Download_concurrent download_concurrent= new Download_concurrent(id,file_id,own);
                        Thread t = new Thread(download_concurrent);
                        t.start();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }

            else if(choice.equals("2"))
                System.out.println("Going back");
            else
                System.out.println("Invalid command!");



        }

}
