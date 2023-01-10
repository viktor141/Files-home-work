import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    private static File savegamesDir, tempDir;
    private static StringBuilder log = new StringBuilder();

    public static void main(String[] args) {
        firstTaskWithFilesInstallation();

        secondTaskWithFilesInstallationSaving();

        thirdTaskWithFilesLoading();


        File fileTemp = new File(tempDir, "temp.txt");

        createFile(fileTemp);

        try (FileWriter tempWriter = new FileWriter(fileTemp)) {
            tempWriter.write(log.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void firstTaskWithFilesInstallation() {
        File dirGames = new File("D://NetologyHW" + File.separator + "Games");

        //src, res, savegames, temp
        File srcDir = new File(dirGames, getWS("src"));
        File resDir = new File(dirGames, getWS("res"));
        savegamesDir = new File(dirGames, getWS("savegames"));
        tempDir = new File(dirGames, getWS("temp"));

        createPath(dirGames);

        createPath(srcDir);
        createPath(resDir);
        createPath(savegamesDir);
        createPath(tempDir);

        File mainDir = new File(srcDir, getWS("main"));
        File testDir = new File(srcDir, getWS("test"));

        createPath(mainDir);
        createPath(testDir);

        File fileMain = new File(mainDir, "Main.java");
        File fileUtils = new File(mainDir, "Utils.java");

        createFile(fileMain);
        createFile(fileUtils);

        //drawables, vectors, icons
        File drawablesDir = new File(resDir, getWS("drawables"));
        File vectorsDir = new File(resDir, getWS("vectors"));
        File iconsDir = new File(resDir, getWS("icons"));

        createPath(drawablesDir);
        createPath(vectorsDir);
        createPath(iconsDir);
    }

    public static void secondTaskWithFilesInstallationSaving() {
        GameProgress gameProgress1 = new GameProgress(50, 3, 24, 65.3);
        GameProgress gameProgress2 = new GameProgress(50, 5, 32, 92.7);
        GameProgress gameProgress3 = new GameProgress(26, 2, 114, 15.1);

        saveGame(savegamesDir.getPath() + getWS("save1.dat"), gameProgress1);
        saveGame(savegamesDir.getPath() + getWS("save2.dat"), gameProgress2);
        saveGame(savegamesDir.getPath() + getWS("save3.dat"), gameProgress3);

        List<String> listOfSavedFiles = new ArrayList<>();

        Arrays.stream(Objects.requireNonNull(savegamesDir.listFiles())).filter(
                file -> file.isFile() && file.getName().split("\\.")[1].equals("dat")).forEach(file -> listOfSavedFiles.add(file.getName()));

        zipFiles(savegamesDir.getPath() + getWS("saves.zip"), listOfSavedFiles);
    }

    public static void thirdTaskWithFilesLoading() {
        openZip();

        System.out.println(openProgress(savegamesDir + getWS("save1.dat")));
    }

    private static void openZip() {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(savegamesDir.getPath() + getWS("saves.zip")))) {
            ZipEntry entry;
            String name;

            while ((entry = zis.getNextEntry()) != null) {
                name = entry.getName();

                FileOutputStream fos = new FileOutputStream(savegamesDir + getWS(name));
                for (int i = zis.read(); i != -1; i = zis.read()) {
                    fos.write(i);
                }

                fos.flush();
                zis.closeEntry();
                fos.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static GameProgress openProgress(String path) {
        GameProgress gameProgress = null;
        try (FileInputStream fis = new FileInputStream(path); ObjectInputStream ois = new ObjectInputStream(fis)) {
            gameProgress = (GameProgress) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return gameProgress;
    }


    private static void saveGame(String path, GameProgress gameProgress) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gameProgress);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void zipFiles(String path, List<String> files) {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(path))) {

            for (String file : files) {

                try (FileInputStream fis = new FileInputStream(savegamesDir.getPath() + getWS(file))) {
                    ZipEntry entry = new ZipEntry(file);
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    logger(fis.read(buffer) + " было прочитано при чтении файла: " + file);
                    zout.write(buffer);
                    zout.closeEntry();
                }

                File fileToDelete = new File(savegamesDir.getPath() + getWS(file));
                if (fileToDelete.delete()) {
                    logger("Успешное удаление сохранения: " + file);
                } else {
                    logger("Ошибка при удалении: " + file);
                }
            }

        } catch (IOException e) {
            logger(e.getMessage());
            System.out.println(e.getMessage());
        }


    }

    private static void createPath(File file) {
        if (file.mkdirs()) {
            logger("Создание каталога: " + file.getPath() + " пршло успешно");
        } else {
            logger("Ошибка! неудалось создать каталог: " + file.getPath());
        }
    }

    private static void createFile(File file) {

        try {
            if (file.createNewFile()) {
                logger("Создание файла: " + file.getPath() + " пршло успешно");
            } else {
                logger("Ошибка! неудалось создать файл: " + file.getPath());
            }
        } catch (IOException e) {
            logger("При создании файла: " + file.getPath() + " произошла ошибка: " + e.getMessage());
        }
    }

    private static String getWS(String s) {
        return File.separator + s;
    }

    private static void logger(String logStr) {
        log.append("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_TIME) + "]" + logStr + "\n");
    }

}