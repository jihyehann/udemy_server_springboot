package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.post.model.PatchPostsReq;
import com.example.demo.src.post.model.PostPostsReq;
import com.example.demo.src.post.model.PostPostsRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostDao postDao;
    private final PostProvider postProvider;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PostService(PostDao postDao, PostProvider postProvider,JwtService jwtService) {
        this.postDao = postDao;
        this.postProvider = postProvider;
        this.jwtService = jwtService;
    }

    public PostPostsRes createPosts(int userIdx, PostPostsReq postPostsReq) throws BaseException {
        try {

            int postIdx = postDao.insertPosts(userIdx, postPostsReq);
            for(int i=0; i<postPostsReq.getPostImgUrls().size(); i++) {
                postDao.insertPostImgs(postIdx, postPostsReq.getPostImgUrls().get(i));
            }
            return new PostPostsRes(postIdx);
        } catch (Exception exception) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void modifyPost(int userIdx, int postIdx, PatchPostsReq patchPostsReq) throws BaseException {
        if(postProvider.checkUserExist(userIdx) == 0) {
            throw new BaseException(BaseResponseStatus.USERS_EMPTY_USER_ID);
        }
        if(postProvider.checkPostExist(postIdx) == 0) {
            throw new BaseException(BaseResponseStatus.POSTS_EMPTY_POST_ID);
        }

        try {
            int result = postDao.updatePost(postIdx, patchPostsReq.getContent()); //성공1, 실패0
            if(result == 0) {
                throw new BaseException(BaseResponseStatus.MODIFY_FAIL_POST);
            }

        } catch (Exception exception) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }
}
