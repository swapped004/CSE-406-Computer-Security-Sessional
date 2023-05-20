import java.io.File;
import java.io.FileInputStream;

public class fileinfo {
    private String file_id;
    private String file_name;
    private long file_size;
    private String privacy;
    private String file_owner;
    private String file_path;

    private File File;

    public fileinfo(String file_id, String file_name, long file_size, String privacy, String file_owner) {
        this.file_id = file_id;
        this.file_name = file_name;
        this.file_size = file_size;
        this.privacy = privacy;
        this.file_owner = file_owner;

        File = null;

        generate_file_path();
    }

    public String getFile_id() {
        return file_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public long getFile_size() {
        return file_size;
    }

    public String getPrivacy() {
        return privacy;
    }

    public File getFile() {
        return File;
    }

    public String getFile_owner() {
        return file_owner;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile(File file) {
        File = file;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }


    public void generate_file_path()
    {
        file_path ="files/"+file_owner+"/"+privacy+"/"+file_name;
    }


}
