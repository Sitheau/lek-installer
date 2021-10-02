
import java.net.*;
import java.io.*;


public class gitScrape {
    public static String getText(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        //add headers to the connection, or check the status if desired..
        
        //handle error response
        int responseCode = connection.getResponseCode();
        InputStream inputStream;
        String linkle = "";
        connection.setRequestMethod("GET");
        connection.setRequestProperty("browser_download_url", linkle);
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
    
        BufferedReader in = new BufferedReader(
            new InputStreamReader(inputStream));
    
        StringBuilder response = new StringBuilder();
        String currentLine;
    
        while ((currentLine = in.readLine()) != null) 
            response.append(currentLine);
    
        in.close();
    
        return response.toString();
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        /*String link = "https://api.github.com/repos/EnormousApplePie/Lekmod/releases/latest";
        String fileName = "Assets.zip";

        URL url = new URL(link);
        URLConnection connect = url.openConnection();
        connect.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");

        InputStream input;
        input = connect.getInputStream();
        System.out.println(input);
        */
        String text = getText("https://api.github.com/repos/EnormousApplePie/Lekmod/releases/latest");
        System.out.println(text);
    }
}
//https://api.github.com/repos/EnormousApplePie/Lekmod/releases/46499601/assets
//this gets us release assets with assets.zip being much easier to see inside, but how do we get that juicy release_id tho