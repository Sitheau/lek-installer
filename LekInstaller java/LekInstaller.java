import java.net.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LekInstaller {

    public static void install() { //install to preset directory
        try {
            URL assetsUrl = new URL("https://github.com/mech-lang/mech/releases/latest/download/mech_0.0.5_windows_x86_64.7z"); //TODO: change to generic file name -montella end
            HttpURLConnection connection = (HttpURLConnection) assetsUrl.openConnection();
            connection.setRequestProperty("Accept", "application/octet-stream");
            ReadableByteChannel uChannel = Channels.newChannel(connection.getInputStream());
            String userDir = System.getProperty("user.home") + "\\Downloads";
            FileOutputStream foStream = new FileOutputStream(userDir + "\\mech_0.0.5_windows_x86_64.7z"); //default installs to user downloads
            FileChannel fChannel = foStream.getChannel();
            fChannel.transferFrom(uChannel, 0, Long.MAX_VALUE);
            uChannel.close();
            foStream.close();
            fChannel.close();
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
 
    }

    public static void delete() { //TODO: go into user/downloads/mech and delete mech.exe
        String filePath
    }

    public static void unpack() throws IOException { //TODO: unzip installed version and delete zip

    }

    public static void build()  { //TODO: place all folders from lekmod temp folder into right spots then delete temp folder

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        URL tagUrl = new URL("https://api.github.com/repos/mech-lang/mech/tags"); //url for all tags

        HttpURLConnection c = (HttpURLConnection) tagUrl.openConnection(); //GET JSON for all tags
        c.setRequestMethod("GET");
        c.setRequestProperty("Content-length", "0");
        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.connect();
        int status = c.getResponseCode();

        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream())); //success? read JSON body
                StringBuilder sb = new StringBuilder();
                sb.append(br.readLine());
                br.close();

                String[] vTemp = sb.toString().split("\"", 5); //weird regex to get the latest version 
                String[] verNum = vTemp[3].split("v0."); //get just the last 2 numbers out of v0.x.x
                double verDouble = Double.parseDouble(verNum[1].trim()); //convernt to double to compare


                //TODO: check if there is a local ver that exists if not skip ver check
                if(verDouble > 0.4) { //TODO: replace 0.4 with read local version
                    delete();
                    install();
                    unpack();
                    build();
                    System.out.println("swag");
                } else {
                    System.out.println("You are up to date :^)");
                }
               
                
        }
    }
}

//TODO: GUI lets user specify install location
//TODO: changes for lek version
//      change install location (need to move folders into steam ones)
//      change github sided version detector to whatever tag system EAP will use
//      either get EAP to make releases in 7zip or go back to native java unzipper