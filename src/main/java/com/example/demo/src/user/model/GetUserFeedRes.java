package com.example.demo.src.user.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetUserFeedRes {
    private boolean _isMyFeed; //자신의 피드인지 구분
    private GetUserInfoRes getUserInfoRes;
    private List<GetUserPostsRes> getUserPosts;

}