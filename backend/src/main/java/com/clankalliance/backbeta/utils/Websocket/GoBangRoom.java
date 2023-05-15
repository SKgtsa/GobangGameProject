package com.clankalliance.backbeta.utils.Websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoBangRoom {

    private String roomCode;

    //0null 1white 2black
    private int[][] board;

    private String blackId;

    private String whiteId;

}
