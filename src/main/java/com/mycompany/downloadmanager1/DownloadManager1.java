/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.downloadmanager1;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Paths;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zafer
 */
public class DownloadManager1 {

    public static void main(String[] args) {

        String[] fileUrls = {
            "https://www.7-zip.org/a/7z2301.exe1",
            "https://www.7-zip.org/a/7z2301.exe",
            "https://www.7-zip.org/a/7z2301.exe",
            "https://www.7-zip.org/a/7z2301.exe",
            "https://www.7-zip.org/a/7z2301.exe"};

        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicLong totalBytesDownloaded = new AtomicLong(0);
        for (String fileUrl : fileUrls) {
            try {
                URI uri = new URI(fileUrl);

                Future<?> future = executor.submit(() -> {
                    downloadFile(uri, totalBytesDownloaded);
                });
                try {
                    future.get(); // Her bir iş parçacığının tamamlanmasını bekler
                    System.out.println("Bitti: " + uri);
                } catch (Exception e) {
                    e.printStackTrace();
                    retryDownload(uri, totalBytesDownloaded);
                }
                //System.out.println("Bitti: " + uri);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Tüm iş parçacıkları tamamlandı.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(URI uri, AtomicLong totalBytesDownloaded) {
        try (BufferedInputStream in = new BufferedInputStream(uri.toURL().openStream()); FileOutputStream fileOutputStream = new FileOutputStream(getFileName(uri))) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            long bytesDownloaded = 0;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                bytesDownloaded += bytesRead;
                totalBytesDownloaded.addAndGet(bytesRead);
           //     System.out.println("Downloaded: " + bytesDownloaded + " bytes from " + uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileName(URI uri) {
        return Paths.get(uri.getPath()).getFileName().toString();
    }

    private static void retryDownload(URI uri, AtomicLong totalBytesDownloaded) {
        try {
            downloadFile(uri, totalBytesDownloaded);
            System.out.println("Yeniden indirme başarılı: " + uri);
        } catch (Exception e) {
            System.err.println("Yeniden indirme başarısız: " + uri);
        }
    }

}
