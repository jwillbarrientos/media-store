package io.jona.memestore.dto;

import io.jona.framework.JonaDb;
import io.jona.framework.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class VideoTag extends Table {
    private static final String FULL_COLUMNS = "video_id, tag_id ";
    @Getter
    private long videoId;
    private long tagId;

    public static ThrowingFunction<ResultSet, VideoTag, SQLException> getFullMapping() {
        return rs -> new VideoTag(
                rs.getLong(1),
                rs.getLong(2)
        );
    }

    public static String getDeleteFromVideoId(long videoId) {
        return "delete from video_tag where video_id = " + videoId;
    }

    public String getInsert() {
        return "insert into video_tag (video_id, tag_id) values(?,?)";
    }

    public String getUpdate() {
        return "nothing yet";
    }

    public String getDelete() {
        return "delete from video_tag where video_id = " + videoId + " and tag_id = " + tagId;
    }

    public Object[] getValues() {
        return new Object[] {videoId, tagId};
    }

    public static List<VideoTag> getVideoTagsByVideoId(Long videoId) {
        return JonaDb.selectList("select " + FULL_COLUMNS + "from video_tag where video_id = ?",
                    VideoTag.getFullMapping(),
                    videoId
                );
    }
}
