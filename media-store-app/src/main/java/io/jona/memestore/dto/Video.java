package io.jona.memestore.dto;

import io.jona.framework.JonaDb;
import io.jona.framework.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
public class Video extends Table {

    public enum State {
        SUBMITTED, DOWNLOADED, ERROR_DOWNLOADING
    }

    public static final String FULL_COLUMNS = "id, name, link, path, duration_seconds, file_size, video_state, date, client_id ";

    @Setter @Getter
    private long id;
    @Setter @Getter
    private String name;
    @Setter @Getter
    private String link;
    @Setter @Getter
    private String path;
    @Setter
    private int durationSeconds;
    @Setter @Getter
    private int fileSize;
    @Setter
    private State videoState;
    @Setter
    private Date date;
    @Setter
    private long clientId;

    public Video(String link,  State videoState, long clientId) {
        this(0, "", link, "", 0,0, videoState, new Date(), clientId);
    }

    public Video(long id,
                 String name,
                 String link,
                 String path,
                 int durationSeconds,
                 int fileSize,
                 State videoState,
                 Date date,
                 long clientId) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.path = path;
        this.durationSeconds = durationSeconds;
        this.fileSize = fileSize;
        this.videoState = videoState;
        this.date = date;
        this.clientId = clientId;
    }

    public static ThrowingFunction<ResultSet, Video, SQLException> getFullMapping() {
        return rs -> new Video(
                rs.getLong(1), // id
                rs.getString(2), // name
                rs.getString(3), // link
                rs.getString(4), // path
                rs.getInt(5), // durationSeconds
                rs.getInt(6), // fileSize
                State.valueOf(rs.getString(7)), // videoState
                rs.getTimestamp(8), // date
                rs.getLong( 9) // clientId
        );
    }

    public static List<Video> getAllVideosByClient(Client client) {
        return JonaDb.selectList(
                "select " + FULL_COLUMNS + "from video where video_state = 'DOWNLOADED' and client_id = ? order by date desc",
                Video.getFullMapping(),
                client.getId()
        );
    }

    public static List<Video> getShortVideosByClient(Client client) {
        return JonaDb.selectList(
                "select " + FULL_COLUMNS + "from video where video_state = 'DOWNLOADED' and duration_seconds <= 60 and client_id = ? order by date desc",
                Video.getFullMapping(),
                client.getId()
        );
    }

    public static List<Video> getLongVideosByClient(Client client) {
        return JonaDb.selectList(
                "select " + FULL_COLUMNS + "from video where video_state = 'DOWNLOADED' and duration_seconds > 60 and client_id = ? order by date desc",
                Video.getFullMapping(),
                client.getId()
        );
    }

    public static List<Video> getVideosWithTags(Client client) {
        List<Video> clientVideos = getAllVideosByClient(client);
        List<Video> videosWithTags = new ArrayList<>();
        for (Video video : clientVideos) {
            boolean videoHaveTag = JonaDb.findCount(
                    "select count(video_id) from video_tag where video_id = ?",
                    video.getId()) > 0;
            if (videoHaveTag)
                videosWithTags.add(video);
        }
        return videosWithTags;
    }

    public static List<Video> getVideosWithoutTags(Client client) {
        List<Video> clientVideos = getAllVideosByClient(client);
        List<Video> videosWithoutTags = new ArrayList<>();
        for (Video video : clientVideos) {
            boolean videoDontHaveTag = JonaDb.findCount(
                    "select count(video_id) from video_tag where video_id = ?",
                    video.getId()) == 0;
            if (videoDontHaveTag)
                videosWithoutTags.add(video);
        }
        return videosWithoutTags;
    }

    public static List<Video> getVideosWithSpecificTag(String tag) {
        List<Video> videosWithSpecificTag = new ArrayList<>();
        List<VideoTag> videosIdWithSpecificTag = JonaDb.selectList(
                "select video_id, tag_id from video_tag where tag_id = ?",
                VideoTag.getFullMapping(),
                tag
        );
        if (videosIdWithSpecificTag != null) {
            for (VideoTag videoTag : videosIdWithSpecificTag) {
                videosWithSpecificTag.add(findById(videoTag.getVideoId()));
            }
        }
        return videosWithSpecificTag;
    }

    public static Video nextVideoToDownload() {
        return JonaDb.selectSingle(
                "select " + FULL_COLUMNS + "from video where video_state = ?",
                Video.getFullMapping(),
                State.SUBMITTED.name()
        );
    }

    public static List<Video> getVideosDownloaded(long clientId) {
        return JonaDb.selectList(
                "select " + FULL_COLUMNS + "from video where client_id = ? and video_state = 'DOWNLOADED' order by date desc limit " + 10,
                Video.getFullMapping(),
                clientId
        );
    }

    public static boolean notYetInserted(Video video) {
        return JonaDb.findCount(
               "select count(id) from video where link = ? and client_id = ?",
               video.link,
               video.clientId
        ) < 1;
    }

    public static String getDeleteFromClient() {
        return "delete from video where client_id = ?";
    }

    public static Video findById(long id) {
        return JonaDb.selectSingle("select " + FULL_COLUMNS + "from video where id = " + id, Video.getFullMapping());
    }

    public boolean existClientWithThisVideo() {
        return JonaDb.findCount("select count(name) from video where name = ?", this.name) > 0;
    }

    public String getInsert() {
        setId(nextId());
        setDate(new Date());
        return "insert into video (" + FULL_COLUMNS + ") values(?,?,?,?,?,?,?,?,?)";
    }

    public String getUpdate() {
        return "update video set id = ?, name = ?, link = ?, path = ?, duration_seconds = ?, file_size = ?, video_state = ?, date = ?, client_id = ? where id = " + id;
    }

    public String getDelete() {
        JonaDb.updateGeneric("delete from video_tag where video_id = ?", this.id);
        return "delete from video where id = " + id;
    }

    public Object[] getValues() {
        return new Object[] {id, name, link, path, durationSeconds, fileSize, videoState.name(), new Timestamp(date.getTime()), clientId};
    }

    @Override
    public String toString() {
        return "Video id = " + id +
                ", name = " + name +
                ", link = " + link +
                ", path = " + path +
                ", duration_seconds = " + durationSeconds +
                ", file_size = " + fileSize +
                ", video_state = " + videoState +
                ", date = " + date +
                ", client_id + " + clientId;
    }
}
