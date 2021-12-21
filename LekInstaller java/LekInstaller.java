import java.net.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class LekInstaller {
    public static void install() { //install to preset directory
        try {
            URL assetsUrl = new URL("https://github.com/EnormousApplePie/Lekmod/releases/latest/download/assets.zip"); //This link is the github the downloader targets /Author/repo name/releases
            HttpURLConnection connection = (HttpURLConnection) assetsUrl.openConnection();
            connection.setRequestProperty("Accept", "application/octet-stream");
            ReadableByteChannel uChannel = Channels.newChannel(connection.getInputStream());
            String userDir = System.getenv("ProgramFiles(X86)") + "\\Steam\\steamapps\\common\\Sid Meier's Civilization V\\Assets";;
            FileOutputStream foStream = new FileOutputStream(userDir + "\\assets.zip");
            FileChannel fChannel = foStream.getChannel();
            fChannel.transferFrom(uChannel, 0, Long.MAX_VALUE);
            uChannel.close();
            foStream.close();
            fChannel.close();
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
 
    }

    public static void delete(File folder) throws IOException{ //recursively delete folders contents then delete folder in documents, be careful :^)
        if (folder.isDirectory()) {
            for (File sub : folder.listFiles()) {
                delete(sub);
            }
        }
        folder.delete();
    }

    private static void move(String targetPath, String destPath, String oldVer) throws IOException {
        File fileToMove = new File(targetPath);
        File fileDest = new File(destPath+"\\"+oldVer);
        Files.move(fileToMove.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void unpack(String zipFilePath, String destDir) throws IOException { //unzips target at zipFilePath into destDir, making a folder
        // Open the zip file
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            String name = zipEntry.getName();
            long size = zipEntry.getSize();
            long compressedSize = zipEntry.getCompressedSize();
            System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", 
                    name, size, compressedSize);

            // Do we need to create a directory ?
            File file = new File(System.getenv("ProgramFiles(X86)") + "\\Steam\\steamapps\\common\\Sid Meier's Civilization V\\Assets\\"+name);
            if (name.endsWith("/")) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            // Extract the file
            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();

        }
        zipFile.close();
        File old_zipper = new File(zipFilePath); //kills old zipper :<
        delete(old_zipper);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        URL tagUrl = new URL("https://api.github.com/repos/EnormousApplePie/Lekmod/tags"); //url for all tags in the repo
        String civAssetsPath = System.getenv("ProgramFiles(X86)") + "\\Steam\\steamapps\\common\\Sid Meier's Civilization V\\Assets";

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
                //funky string manipulation to get the first tag out of the response of ALL tags, scrapes the first vX.X.X tag in the response as the latest version
                String[] vTemp = sb.toString().split("v"); //weird regex to get the latest version
                String[] verNum = vTemp[15].split("\""); //change vTemp[15] to not include vdev tags
                
                double verDouble = Double.parseDouble(verNum[0].trim()); //convernt to double to compare
                
                double localModVer = 29.1;
                double localMapVer = 3.4; //civAssetsPath+"\\Maps\\" search for folder with Lekmap+" " starter

                String localLekMod = "LEKMOD_v" + Double.toString(localModVer);
                String localLekMap = "Lekmap v" + Double.toString(localMapVer);

                File modDirectory = new File(civAssetsPath+"\\DLC\\"+localLekMod); //check if file exists
                File mapDirectory = new File(civAssetsPath+"\\Maps\\"+localLekMap); //check if file exists
                boolean tempExists = modDirectory.exists() && mapDirectory.exists();

                System.out.println("latest ver " + verDouble);

                if(tempExists) {    //check if that directory already exists (if yes need to update)
                    if(verDouble > localModVer) { //ONLY checks if LEKMOD is out of date not map, would need 2nd tag bleh
                        File civFolder = new File(civAssetsPath); //assign var to mech folder location
                        System.out.println("updating");
                        
                        install(); //add check for successful install
                        unpack(civAssetsPath+"\\assets.zip", civAssetsPath);
                        //delete(civFolder); //DELETE OLD
                        move(civAssetsPath+"\\"+localLekMod, civAssetsPath+"\\DLC\\", localLekMod); //move mod folder
                        move(civAssetsPath+"\\"+localLekMap, civAssetsPath+"\\Maps", localLekMap); //move map folder
                        System.out.println("success");
                    } else {
                        System.out.println("You are up to date :^)");
                    }
                } else { //first time set up
                    System.out.println("installing first time");

                    install(); //add check for successful install
                    unpack(civAssetsPath+"\\assets.zip", civAssetsPath);
                    move(civAssetsPath+"\\"+localLekMod, civAssetsPath+"\\DLC\\", localLekMod); //move mod folder
                    move(civAssetsPath+"\\"+localLekMap, civAssetsPath+"\\Maps", localLekMap); //move map folder
                    System.out.println("success");
                }
                   
        }
    }
}