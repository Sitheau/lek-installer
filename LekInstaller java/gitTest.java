import java.net.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.channels.*;

public class gitTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        URL url = new URL("https://github.com/EnormousApplePie/Lekmod/releases/download/v28.2/Assets.zip"); //fill this in with the right dynamically made link?
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/octet-stream");


        ReadableByteChannel uChannel = Channels.newChannel(connection.getInputStream());
        FileOutputStream foStream = new FileOutputStream("C:/Users/nicca/documents/assets.zip"); //specify user directory directly into steam and unzip?
        FileChannel fChannel = foStream.getChannel();
        fChannel.transferFrom(uChannel, 0, Long.MAX_VALUE);
        
        
        
        uChannel.close();
        foStream.close();
        fChannel.close();
        System.out.println(connection.getResponseCode());
    }
}
//assets.zip currently contains DLC, Maps and UI folder with a .gitignore and readme
//DLC contains LEKMOD folder which we want in civ5/assets/DLC, also has extra files, unknown if this complicates things