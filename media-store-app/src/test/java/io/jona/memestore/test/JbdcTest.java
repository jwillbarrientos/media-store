package io.jona.memestore.test;

import io.jona.framework.JonaDb;
import io.jona.memestore.AppProps;
import io.jona.memestore.dto.Client;
import io.jona.memestore.dto.Video;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

@Slf4j
public class JbdcTest {
    public static void main(String[] args) throws SQLException, IOException, ParseException {
//        JonaDb.init(AppProps.getJdbcUrl(), AppProps.getDbUser(), AppProps.getDbPassword());
//
//        Client client = new Client("barrientosjonah@gmail.com", "1234");
//        boolean successInsert = JonaDb.insert(client);
//
//        System.out.println(successInsert);
//        Client client1 = JonaDb.selectSingle("select  id, email, password from client where email = ?",
//                Client.getFullMapping(), "barrientosjonah@gmail.com");
//        System.out.println(client1);
//        Video video = new Video("tremendoProgramador",
//                "https://www.tiktok.com/@gertu0k/video/7258798530066713862",
//                "C:/Users/barri/IdeaProjects/padawan-learner/meme-store/downloads/EL REY DE LOS CURRÍCULUMS Gracias @desayunosinformales12 por invitarm....mp4",
//                18,
//                100,
//                Video.State.DOWNLOADED,
//                1);
//        boolean successInsertVideo = JonaDb.insert(video);
//        System.out.println(successInsertVideo);
//        List<Video> videos = JonaDb.selectList("select id, name, link, path, duration_seconds, file_size, video_state, date, client_id from video where client_id = ?",
//                Video.getFullMapping(), "1");
//        System.out.println(videos);
//        System.out.println(successInsert);
//        boolean successDelete = JonaDb.delete(client);
//        System.out.println(successDelete);
    }
}
