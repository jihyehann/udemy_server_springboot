package com.example.demo.src.post;

import com.example.demo.src.post.model.GetPostImgRes;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PostImgUrlsReq;
import com.example.demo.src.post.model.PostPostsReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetPostsRes> selectPosts(int userIdx){
        String selectPostsQuery =
                "SELECT p.postIdx as postIdx,\n" +
                        "            u.userIdx as userIdx,\n" +
                        "            u.nickName as nickName,\n" +
                        "            u.profileImgUrl as profileImgUrl,\n" +
                        "            p.content as content,\n" +
                        "            IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                        "            IF(commentCount is null, 0, commentCount) as commentCount,\n" +
                        "            case\n" +
                        "                when timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                        "                    then concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                        "                when timestampdiff(minute , p.updatedAt, current_timestamp) < 60\n" +
                        "                    then concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                        "                when timestampdiff(hour , p.updatedAt, current_timestamp) < 24\n" +
                        "                    then concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                        "                when timestampdiff(day , p.updatedAt, current_timestamp) < 365\n" +
                        "                    then concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                        "                else timestampdiff(year , p.updatedAt, current_timestamp)\n" +
                        "            end as updatedAt,\n" +
                        "            IF(pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                        "        FROM Post as p\n" +
                        "            join User as u on u.userIdx = p.userIdx\n" +
                        "            left join (select postIdx, userIdx, count(postLikeidx) as postLikeCount from PostLike WHERE status = 'ACTIVE' group by postIdx) plc on plc.postIdx = p.postIdx\n" +
                        "            left join (select postIdx, count(commentIdx) as commentCount from Comment WHERE status = 'ACTIVE' group by postIdx) c on c.postIdx = p.postIdx\n" +
                        "            left join Follow as f on f.followeeIdx = p.userIdx and f.status = 'ACTIVE'\n" +
                        "            left join PostLike as pl on pl.userIdx = f.followerIdx and pl.postIdx = p.postIdx\n" +
                        "        WHERE f.followerIdx = ? and p.status = 'ACTIVE'\n" +
                        "        group by p.postIdx;\n" ;

        int selectUserPostsParam = userIdx;
        return this.jdbcTemplate.query(selectPostsQuery,
                (rs,rowNum) -> new GetPostsRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNot"),
                        this.jdbcTemplate.query(
                                "SELECT pi.postImgUrlIdx, pi.imgUrl\n" +
                                    "FROM PostImgUrl as pi\n" +
                                        "join Post as p on p.postIdx = pi.postIdx\n" +
                                    "WHERE pi.status = 'ACTIVE' and p.postIdx = ?;\n",
                                (rk,rownum) -> new GetPostImgRes(
                                        rk.getInt("postImgUrlIdx"),
                                        rk.getString("imgUrl"))
                                ,rs.getInt("postIdx"))),selectUserPostsParam);
    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParam = userIdx;
        return this.jdbcTemplate.queryForObject(
                checkUserExistQuery,
                int.class,
                checkUserExistParam);
    }

    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParam = postIdx;
        return this.jdbcTemplate.queryForObject(
                checkPostExistQuery,
                int.class,
                checkPostExistParam);
    }

    public int insertPosts(int userIdx, PostPostsReq postPostsReq){
        String insertPostQuery = "INSERT INTO Post(userIdx, content) VALUES (?,?)";
        Object []insertPostsParams = new Object[] {userIdx, postPostsReq.getContent()};
        this.jdbcTemplate.update(insertPostQuery, insertPostsParams);

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 저장된 id 값을 리턴해주는 쿼리문
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 방금 추가된 id값 리턴
    }

    public int insertPostImgs(int postIdx, PostImgUrlsReq postImgUrlsReq){
        String insertPostImgsQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?,?)";
        Object []insertPostImgsParams = new Object[] {postIdx, postImgUrlsReq.getImgUrl()};
        this.jdbcTemplate.update(insertPostImgsQuery, insertPostImgsParams);

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 저장된 id 값을 리턴해주는 쿼리문
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 방금 추가된 id값 리턴
    }

    public int updatePost(int userIdx, String content){
        String updatePostQuery = "UPDATE Post SET content=? WHERE postIdx=?";
        Object []updatePostParams = new Object[] {content, userIdx};
        return this.jdbcTemplate.update(updatePostQuery, updatePostParams);
    }
}
