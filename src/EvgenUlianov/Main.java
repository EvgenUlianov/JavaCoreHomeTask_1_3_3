package EvgenUlianov;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {


    private static final String PATH = "C:\\Users\\EUlyanov\\Documents\\Учеба JAVA\\JavaCore\\Games";

    public static void main(String[] args) {
        System.out.println("Задача 3: Загрузка (со звездочкой *)");
        System.out.println("Работаем в папке:");
        System.out.println(PATH);
        File catalog = new File(PATH);
        if (!catalog.exists()) {
            System.out.println("указанная папка не существует");
            return;
        }
        String savegames = PATH + "\\" + "savegames";
        File catalogSavegames = new File(PATH);
        if (!catalogSavegames.exists()) {
            System.out.println("папка savegames не существует");
            return;
        }

        String archiveName = "";

        try (DirectoryStream<Path> dir = Files.newDirectoryStream(
                Paths.get(savegames), "*.zip")) {

            for (Path entry : dir) {
                archiveName = entry.toAbsolutePath().toString();
                // ...
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        if (!archiveName.isEmpty()) {
            System.out.println("Буферизованное чтение:");
            List<GameProgress> saves = openZipBuffered(savegames, archiveName);
            for (GameProgress save: saves) {
                System.out.println(save.toString());
            }
            System.out.println("Чтение через сохранение файла:");
            saves = openZip(savegames, archiveName);
            for (GameProgress save: saves) {
                System.out.println(save.toString());
            }
        }


//        File catadlog = new File(savegames);
//        String[] fileNames = catadlog.list((dir, name) -> name.matches("*.zip")));
//        for (String fileName: fileNames) {
//            System.out.println(fileName);
//
//        }

    }

    public static List<GameProgress> openZipBuffered(String savegames, String archiveName) {
        List<GameProgress> saves = new ArrayList<>();
        try{
            try (ZipInputStream zipIn = new ZipInputStream(
                    new FileInputStream(archiveName))) {
                ZipEntry entryZip;
                String fileName;
                while ((entryZip = zipIn.getNextEntry()) != null) {

                    fileName = savegames + "\\" + entryZip.getName();

                    // предполагаю, что наверное этот объект лучше всего подходит для сбора данных в коллекцию и выдачи в таком же порядке
                    Collection<Integer> preBuffer = new ArrayDeque<>() {};
                    for (int c = zipIn.read(); c != -1; c = zipIn.read()) {
                        preBuffer.add((Integer) c);
                    }
                    zipIn.closeEntry();

                    // вот это, кажется, как-то инвалидно: я создал сложный объект, который много памяти ест,
                    // из него через цепь преобразований получит массив байтов
                    // а все только потому что я не знаю как создать динамическую коллекцию с типом byte
                    // и там еще проблема, что я не знаю заранее, когда закончится файл...
                    // зато я не записывал промежуточный файл на диск)))
                    int[] buffer1 = preBuffer.stream()
                            .mapToInt(value ->  (byte) value.intValue())
                            .toArray();
                    byte[] buffer = new byte[buffer1.length];
                    for (int i = 0; i < buffer1.length ; i++)
                        buffer[i] = (byte) buffer1[i];

                    // read the file
                    try (ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                         ObjectInputStream ObjIn = new ObjectInputStream(in)) {
                        // десериализуем объект и скастим его в класс game
                        saves.add((GameProgress) ObjIn.readObject());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }

        return saves;
    }

    public static List<GameProgress> openZip(String savegames, String archiveName) {
        List<GameProgress> saves = new ArrayList<>();
        try{
            try (ZipInputStream zipIn = new ZipInputStream(
                    new FileInputStream(archiveName))) {
                ZipEntry entryZip;
                String fileName;
                while ((entryZip = zipIn.getNextEntry()) != null) {

                    fileName = savegames + "\\" + entryZip.getName();

                    FileOutputStream fileOut = new FileOutputStream(fileName);
                    for (int c = zipIn.read(); c != -1; c = zipIn.read()) {
                        fileOut.write(c);
                    }
                    fileOut.flush();
                    zipIn.closeEntry();
                    fileOut.close();

                    // read the file
                    try (FileInputStream  fileIn = new FileInputStream(fileName);
                         ObjectInputStream ObjIn = new ObjectInputStream(fileIn)) {
                        // десериализуем объект и скастим его в класс game
                        saves.add((GameProgress) ObjIn.readObject());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    // delete the file
                    try {
                        File deletingFile = new File(fileName);
                        deletingFile.deleteOnExit();
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }

        return saves;
    }
}
