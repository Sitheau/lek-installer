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

    private static void move(String targetPath, String destPath, String newVer) throws IOException {
        File fileToMove = new File(targetPath);
        File fileDest = new File(destPath+"\\"+newVer);
        Files.move(fileToMove.toPath(), fileDest.toPath());
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
                
                String[] verNum = vTemp[1].split("\""); 
                double verDouble = Double.parseDouble(verNum[0].trim()); //convernt to double to compare
                
                String modPath = civAssetsPath + "\\DLC\\";
                String mapPath = civAssetsPath + "\\Maps\\";

                File dlcFolder = new File(modPath);
                File mapsFolder = new File(mapPath);

                File[] LekMatches = dlcFolder.listFiles(new FilenameFilter() { //find local lek mods
                    public boolean accept(File dlcFolder, String name)
                    {
                        return name.startsWith("LEKMOD");
                    }
                });
                File[] mapMatches = mapsFolder.listFiles(new FilenameFilter() { //find local lek mods
                    public boolean accept(File mapsFolder, String name)
                    {
                        return name.startsWith("Lekmap ");
                    }
                });

                System.out.println("latest ver " + verDouble);

                if(LekMatches.length > 0 && LekMatches[0] != null) {   //does lekmod exist already?
                    File modDirectory = LekMatches[0];
                    String localModVer = modDirectory.getPath();
                    String[] tempLocalModVer = localModVer.split("v");

                    double localLekmodVersion = Double.parseDouble(tempLocalModVer[2]);
                    
                    if(verDouble > localLekmodVersion) { //is the latest tag newer
                            System.out.println("out of date installing new");
                            install();
                            unpack(civAssetsPath+"\\assets.zip", civAssetsPath);
                            
                            if(mapMatches.length > 0 && mapMatches[0] != null) {
                                File mapDirectory = mapMatches[0];
                                delete(mapDirectory);
                            }

                            delete(modDirectory);
                            mapsFolder = new File(civAssetsPath);
                            File[] newMapMatches = mapsFolder.listFiles(new FilenameFilter() { //find local lek mods
                                public boolean accept(File mapsFolder, String name)
                                {
                                    return name.startsWith("Lekmap");
                                }
                            });
                            File newMapDirectory = newMapMatches[0];
                            String newLocalMapPath =newMapDirectory.getPath();
                            String[] tempNewMap = newLocalMapPath.split("v");
                            double newMapver = Double.parseDouble(tempNewMap[2]);

                            move(civAssetsPath+"\\LEKMOD_v"+verDouble, civAssetsPath+"\\DLC", "LEKMOD_v"+Double.toString(verDouble)); //move mod folder
                            move(civAssetsPath+"\\Lekmap v"+newMapver, civAssetsPath+"\\Maps", "Lekmap v"+Double.toString(newMapver)); //move map folder
                            System.out.println("successfully installed lekmod and lekmap");
                    } else {
                        System.out.println("You are up to date :^)");
                    }
                } else { //first time set up
                    System.out.println("installing first time");

                    install(); //add check for successful install
                    unpack(civAssetsPath+"\\assets.zip", civAssetsPath);

                    mapsFolder = new File(civAssetsPath);
                    File[] newMapMatches = mapsFolder.listFiles(new FilenameFilter() { //find local lek mods
                        public boolean accept(File mapsFolder, String name)
                        {
                            return name.startsWith("Lekmap");
                        }
                    });
                    File newMapDirectory = newMapMatches[0];
                    String newLocalMapPath =newMapDirectory.getPath();
                    String[] tempNewMap = newLocalMapPath.split("v");
                    double newMapver = Double.parseDouble(tempNewMap[2]);

                    move(civAssetsPath+"\\LEKMOD_v"+verDouble, civAssetsPath+"\\DLC", "LEKMOD_v"+Double.toString(verDouble)); //move mod folder
                    move(civAssetsPath+"\\Lekmap v"+newMapver, civAssetsPath+"\\Maps", "Lekmap v"+Double.toString(newMapver)); //move map folder
                    System.out.println("success");
                }
                   
        }
    }
}