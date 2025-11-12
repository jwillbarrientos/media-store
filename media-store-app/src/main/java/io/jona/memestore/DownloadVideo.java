package io.jona.memestore;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class DownloadVideo {
    private static Platform platform;
    private static Executable exe;
    private static final Properties props = new Properties();
    private static Process process;
    public static String downloadVideo(String link) {
        //https://www.tiktok.com/@gertu0k/video/7258798530066713862
        //https://www.instagram.com/reel/DM2mUqnKUYS/?igsh=MWNuOG5leXRjajE0dg==
        //https://www.youtube.com/watch?v=lWHYJOayAUg
        //https://youtu.be/lWHYJOayAUg?si=UgHR7MhnOECnnluj
        //https://www.facebook.com/reel/718363154596932
        try {
            return runDownloader(link);
        } catch (IOException | InterruptedException e) {
            log.error("Error downloading video ", e);
            return null;
        }
    }

    public static String runDownloader(String url) throws IOException, InterruptedException {
        platform = Platform.whatPlatformIs(url);
        if(platform == Platform.YOU_TUBE)
            exe = Executable.YOUTUBE_DL;
        else
            exe = Executable.YT_DLP;
        String[] command;
        log.info("user.dir: {}", System.getProperty("user.dir"));
        String ytDl = AppProps.getYtDl();
        String temporalFolder = AppProps.getTemporalPath();
        String destinyFolder = AppProps.getVideoOutputPath();
        switch (platform) {
            case FACEBOOK:
                command = buildCommand(ytDl, temporalFolder, url);
                break;
            case YOU_TUBE:
                command = buildCommand(ytDl, temporalFolder, url);
                break;
            case INSTAGRAM:
                command = buildCommand(ytDl, temporalFolder, url);
                break;
            case TIKTOK:
                command = new String[]{
                        ytDl,
                        "-o", temporalFolder + "/%(title)s.%(ext)s",
                        "--exec", "ffmpeg -y -i \"{}\" -c:v libx264 -c:a aac -movflags +faststart \"{}-temp.mp4\" && move /Y \"{}-temp.mp4\" \"{}\"",
                        url
                };
                break;
            default:
                log.debug("Invalid url");
                return null;
        }
        process = Runtime.getRuntime().exec(command);
        new Thread(DownloadVideo::printProcessOutput, "OutputThread").start();
        new Thread(DownloadVideo::printProcessErr, "ErrThread").start();
        process.waitFor();
        log.debug("Download finished");
        File dir = new File(temporalFolder);
        File[] files = dir.listFiles();
        if (files.length == 0)
            return null;
        String fileName = files[0].getName();
        Path source = Path.of(temporalFolder + "/" + fileName);
        Path target = Path.of(destinyFolder + "/" + fileName);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public static void printProcessOutput() {
        log.trace("Entering printProcessOutput() with param: {}", process);
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null) {
                log.debug("{}: {}", exe.name(), line);
            }
        } catch (IOException e) {
            log.error("{}: IOException reading the InputStream of process", exe.name(), e);
        }
        log.debug("Printing InputStream of process without exception");
    }

    public static void printProcessErr() {
        log.trace("Entering printProcessErr() with param: {}", process);
        try(BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while((line = errorReader.readLine()) != null) {
                log.error(exe.name() + ": {}", line);
            }
        } catch (IOException e) {
            log.error("{}: IOException reading the ErrorStream of process", exe.name(), e);
        }
        log.debug("Printing ErrorStream of process without Exception");
    }

    public static String[] buildCommand(String downloader, String destinyFolder, String url) {
        log.trace("Entering buildCommand() with params downloader={}, destinyFolder={}, url={}", downloader, destinyFolder, url);
        String[] command = new String[4];
        command[0] = downloader;
        command[1] = "-o";
        command[2] = destinyFolder + "/%(title)s.%(ext)s";
        command[3] = url;
        log.trace("Returning full command of buildCommand() method: {}", Arrays.toString(command));
        return command;
    }
}
