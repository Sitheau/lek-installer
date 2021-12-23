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
import java.util.Scanner;

public class LekInstaller {
    public static void install(String civAssetsPath) { //install to preset directory
        try {
            URL assetsUrl = new URL("https://github.com/EnormousApplePie/Lekmod/releases/latest/download/assets.zip"); //This link is the github the downloader targets /Author/repo name/releases
            HttpURLConnection connection = (HttpURLConnection) assetsUrl.openConnection();
            connection.setRequestProperty("Accept", "application/octet-stream");
            ReadableByteChannel uChannel = Channels.newChannel(connection.getInputStream());
            String userDir = civAssetsPath;
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
            File file = new File(destDir+"\\"+name);
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
    	Scanner sc = new Scanner(System.in);
        URL tagUrl = new URL("https://api.github.com/repos/EnormousApplePie/Lekmod/tags"); //url for all tags in the repo
        System.out.println("Do you want to use the default path to install lekmod? (y/n)");
        System.out.println("e.g: C:\\Program Files (x86)\\Steam\\steamapps\\common\\Sid Meier's Civilization V\\Assets");
        String civAssetsPath = null;
        String answer1 = sc.nextLine();
        
        if(answer1.equals("n")) {
        	System.out.println("Please enter the path to the civ 5 assets folder in the same format as above");
        	civAssetsPath = sc.nextLine();
        } else if (answer1.equals("y")){
        	civAssetsPath = System.getenv("ProgramFiles(X86)")+"\\Steam\\steamapps\\common\\Sid Meier's Civilization V\\Assets";

        } else {
        	System.out.println("Incorrect input please run the program again because I am a lazy programmer");
        	System.exit(0);
        }
        
        
        
        
        
        
        File tempCivFolder = new File(civAssetsPath);
        boolean civExists = tempCivFolder.exists();
        
        if(!civExists) {
        	System.out.println("Incorrect Path please run program again because I am a lazy programmer and its 6am");
        	System.exit(0);;
        }
        
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
                            install(civAssetsPath);
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
                            //emergency clean up
                            File leftoverMod = new File(civAssetsPath+"\\LEKMOD_v"+verDouble);
                            File leftoverMap = new File(civAssetsPath+"\\Lekmap v"+newMapver);
                            
                            if(leftoverMod.exists()) {
                            	delete(leftoverMod);
                            }
                            if(leftoverMap.exists()) {
                            	delete(leftoverMod);
                            }
                               
                    } else {
                        System.out.println("Your lekmod up to date :^)");
                        mapMatches = mapsFolder.listFiles(new FilenameFilter() { //find local lek mods
                            public boolean accept(File mapsFolder, String name)
                            {
                                return name.startsWith("Lekmap ");
                            }
                        });
                        boolean noMap = mapMatches.length > 0 && mapMatches[0] != null;
                        if(!noMap) {
                        	System.out.println("You do not have lekmap installed would you like to install it? (y/n) WARNING DOWNLOADS BOTH LEKMOD AND LEKMAP");
                        	String answer = sc.nextLine();
                        	if(answer.equals("y")) {
                        		System.out.println("installing lek mod and lekmap");
                        		delete(modDirectory);
                                install(civAssetsPath); //add check for successful install
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
                                //emergency clean up
                                File leftoverMod = new File(civAssetsPath+"\\LEKMOD_v"+verDouble);
                                File leftoverMap = new File(civAssetsPath+"\\Lekmap v"+newMapver);
                                
                                if(leftoverMod.exists()) {
                                	delete(leftoverMod);
                                }
                                if(leftoverMap.exists()) {
                                	delete(leftoverMod);
                                }
                                   
                        	} else if(answer.equals("n")){
                        		System.out.println("Ok good bye");
                        		System.exit(0);
                        		
                        	} else {
                                System.out.println("wrong answer (not y or n) sorry kid run it again im lazy");
                        	}
                        }
                    }
                } else { //first time set up
                    System.out.println("installing first time");
                    
                    //edge case of only lekmap
                    File dir = new File(civAssetsPath+"\\Maps");
                    File[] foundFiles = dir.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.startsWith("Lekmap");
                        }
                    });
                    if(foundFiles.length > 0 && foundFiles[0] != null) {
                    	System.out.println("deleting local lekmap");
                    	delete(foundFiles[0]);
                    }
                    install(civAssetsPath); //add check for successful install
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
                    try {
                    	move(civAssetsPath+"\\Lekmap v"+newMapver, civAssetsPath+"\\Maps", "Lekmap v"+Double.toString(newMapver)); //move map folder
                    } catch(Exception e) {
                    	System.out.println("You already have lekmap installed");
                    }
                    
                    //emergency clean up
                    File leftoverMod = new File(civAssetsPath+"\\LEKMOD_v"+verDouble);
                    File leftoverMap = new File(civAssetsPath+"\\Lekmap v"+newMapver);
                    
                    if(leftoverMod.exists()) {
                    	delete(leftoverMod);
                    }
                    if(leftoverMap.exists()) {
                    	delete(leftoverMod);
                    }
                       
                    System.out.println("success");
                }
        }
    }
}