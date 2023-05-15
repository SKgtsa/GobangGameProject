package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.RedisUtils;
import com.clankalliance.backbeta.utils.RoomCodeGenerator;
import com.clankalliance.backbeta.utils.TokenUtil;
import com.clankalliance.backbeta.utils.Websocket.GoBangRoom;
import com.clankalliance.backbeta.utils.Websocket.SocketDomain;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {

    private static TokenUtil tokenUtil;

    @Resource
    public void setTokenUtil(TokenUtil tokenUtil){WebSocketServer.tokenUtil = tokenUtil;}

    private static RoomCodeGenerator roomCodeGenerator;

    @Resource
    public void setRoomCodeGenerator(RoomCodeGenerator roomCodeGenerator){WebSocketServer.roomCodeGenerator = roomCodeGenerator;}

    /**
     * key: id
     * value: roomCode
     */
    private static StringRedisTemplate RedisTemplateIdRoomCode;

    @Resource
    public void setRedisTemplateIdRoomCode(StringRedisTemplate redisTemplateIdRoomCode){WebSocketServer.RedisTemplateIdRoomCode = redisTemplateIdRoomCode;}

    /**
     * key: roomCode
     * value: room
     */
    private static StringRedisTemplate RedisTemplateCodeRoom;

    @Resource
    public void setRedisTemplateCodeRoom(StringRedisTemplate redisTemplateCodeRoom){WebSocketServer.RedisTemplateCodeRoom = redisTemplateCodeRoom;}


    /**
     * key: roomCode
     */
    private static RedisTemplate<String,String> RedisTemplateMatch;

    @Resource
    public void setRedisTemplateMatch(RedisTemplate<String,String> redisTemplateMatch){WebSocketServer.RedisTemplateMatch = redisTemplateMatch;}

    private static UserService userService;

    @Resource
    public void setUserService(UserService userService){WebSocketServer.userService = userService;}

    private static final String MATCH_SET_NAME = "matchSet";

//    @PostConstruct
//    private void initialize(){
//        RedisTemplateMatch.opsForSet().add(MATCH_SET_NAME);
//    }

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    //在线客户端数目
    private static int onlineCount = 0;
    //Map用于存储已连接的客户端信息(打算用redis改进)
    private static ConcurrentHashMap<String, SocketDomain> websocketMap = new ConcurrentHashMap<String, SocketDomain>();

    private Session session;

    private String userId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token){
        System.out.println(token);
        CommonResponse response = tokenUtil.tokenCheck(token);
        this.session = session;
        if(!response.getSuccess()){
            sendMessage("loginFail");
            return;
        }
        String targetId = response.getMessage();
        User user = userService.findUserById(targetId);
        if(!websocketMap.containsKey(targetId)){
            onlineCount ++;
        }
        this.userId = targetId;
        SocketDomain socketDomain = new SocketDomain();
        socketDomain.setSession(session);
        socketDomain.setUri(session.getRequestURI().toString());
        websocketMap.put(userId, socketDomain);
        logger.info("id为" + userId + "的用户连接，当前人数为" + onlineCount);
        try{
            StringJoiner sj = new StringJoiner("?");
            sj.add("connected");
            sj.add(user.getNickName());
            sj.add("" + user.getWinNum());
            sj.add("" + user.getLoseNum());
            sendMessage(sj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(){
        if(websocketMap.containsKey(userId)){
            websocketMap.remove(userId);
            onlineCount --;
            logger.info("id为" + userId + "的用户断开连接，当前人数为" + onlineCount);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session){
        String[] request;
        request = message.split("\\?");
        switch (request[0]){
            case "match":
                //match
                handleMatch();
                break;
            case "private":
                //private
                handlePrivate();
                break;
            case "quit":
                //cancelMatch
                handleQuit();
                break;
            case "invite":
                //invite?{roomCode}
                handleInvite(request[1]);
                break;
            case "place":
                //place?{x}?{y}
                handlePlace(request[1],request[2]);
                break;
        }


        if(!StringUtil.isNullOrEmpty(message)){
            logger.info("收到id为" + userId + "的用户发来消息：" + message);
        }
    }

    private void handlePlace(String xS, String yS){
        int x,y;
        try{
            x = Integer.parseInt(xS);
            y = Integer.parseInt(yS);
        }catch (Exception e){
            sendMessage("error");
            return;
        }
        if(!RedisUtils.hasKey(userId, RedisTemplateIdRoomCode)){
            sendMessage("error");
            return;
        }
        String roomCode = RedisUtils.getObject(userId, RedisTemplateIdRoomCode, String.class);
        if(!RedisUtils.hasKey(roomCode, RedisTemplateCodeRoom)){
            sendMessage("error");
            return;
        }
        GoBangRoom room = RedisUtils.getObject(roomCode, RedisTemplateCodeRoom, GoBangRoom.class);
        int color = 1;
        String rivalId;
        if(room.getBlackId().equals(userId)){
            //棋盘以白子视角存储
            int i = x;
            x = y;
            y = i;
            color = 2;
            rivalId = room.getWhiteId();
        }else{
            rivalId = room.getBlackId();
        }
        int[][] board = room.getBoard();
        if(board[y][x] != 0){
            sendMessage("occupied");
            return;
        }
        board[y][x] = color;
        int xI,yI;
        int[][] num = new int[3][3];
        int[] direction = {1,0,-1};
        for(int i = 0;i < 3;i ++){
            for(int j = 0;j < 3;j ++){
                xI = x + direction[i];
                yI = y + direction[j];
                while(board[yI][xI] == color){
                    num[direction[j] + 1][direction[i] + 1] ++;
                }
            }
        }
        if(
                num[0][0] + num[2][2] >= 4
                        || num[0][1] + num[2][1] >= 4
                        || num[0][2] + num[2][0] >= 4
                        || num[1][0] + num[1][2] >= 4
        ){
            sendMessage("win");
            sendMessageTo(rivalId, "lose");
            userService.handleGameOver(true, userId);
            userService.handleGameOver(false,rivalId);
        }
    }

    private void startGame(String roomCode){
        GoBangRoom roomNeedPlayer = RedisUtils.getObject(roomCode, RedisTemplateCodeRoom, GoBangRoom.class);
        String rivalId;
        boolean first;
        if(roomNeedPlayer.getBlackId() == null){
            rivalId = roomNeedPlayer.getWhiteId();
            roomNeedPlayer.setBlackId(userId);
            first = true;
        }else{
            rivalId = roomNeedPlayer.getBlackId();
            roomNeedPlayer.setWhiteId(userId);
            first = false;
        }
        RedisUtils.add(roomCode,roomNeedPlayer, RedisTemplateCodeRoom);
        RedisUtils.add(userId, roomCode, RedisTemplateIdRoomCode);
        RedisUtils.setDel(MATCH_SET_NAME, RedisTemplateMatch, roomCode);
        sendMessageTo(rivalId,"start?" + !first);
        sendMessage("start?" + first);
    }

    private String createRoom(){
        GoBangRoom room = new GoBangRoom();
        room.setRoomCode(roomCodeGenerator.getCode());
        if(Math.random() > 0.5){
            //white
            room.setWhiteId(userId);
        }else{
            //black
            room.setBlackId(userId);
        }
        room.setBoard(new int[15][15]);
        RedisUtils.add(room.getRoomCode(),room, RedisTemplateCodeRoom);
        RedisUtils.add(userId, room.getRoomCode(), RedisTemplateIdRoomCode);
        sendMessage("success?" + room.getRoomCode());
        return room.getRoomCode();
    }
    private void handlePrivate(){
        createRoom();
    }

    private void handleMatch(){
        if(RedisTemplateMatch.opsForSet().size(MATCH_SET_NAME) != null && RedisTemplateMatch.opsForSet().size(MATCH_SET_NAME) != 0){
            String roomCode = RedisTemplateMatch.opsForSet().pop(MATCH_SET_NAME);
            startGame(roomCode);
            return;
        }
        RedisTemplateMatch.opsForSet().add(MATCH_SET_NAME, createRoom());
    }

    private void handleQuit(){
        if(RedisUtils.hasKey(userId,RedisTemplateIdRoomCode )){
            String roomCode = RedisUtils.getObject(userId, RedisTemplateIdRoomCode, String.class);
            if(RedisUtils.setContains(MATCH_SET_NAME, RedisTemplateMatch, roomCode)){
                //用户正在匹配
                RedisUtils.setDel(MATCH_SET_NAME, RedisTemplateMatch, roomCode);
            }
            GoBangRoom room = RedisUtils.getObject(roomCode, RedisTemplateCodeRoom, GoBangRoom.class);
            RedisUtils.delete(roomCode, RedisTemplateCodeRoom);
            if(room.getWhiteId() != null){
                RedisUtils.delete(room.getWhiteId(), RedisTemplateIdRoomCode);
            }
            if(room.getBlackId() != null){
                RedisUtils.delete(room.getBlackId(), RedisTemplateIdRoomCode);
            }
            roomCodeGenerator.returnCode(roomCode);
        }
    }

    /**
     * 凭邀请码进入房间
     * @param inviteCode
     */
    private void handleInvite(String inviteCode){
        if(!RedisUtils.hasKey(inviteCode, RedisTemplateCodeRoom)){
            //房间不存在
            sendMessage("invalid");
            return;
        }
        startGame(inviteCode);
    }

    private void sendMessage(String obj){
        synchronized (session){
            this.session.getAsyncRemote().sendText(obj);
        }
    }

    private void sendMessageTo(String userId,String obj){
        SocketDomain socketDomain = websocketMap.get(userId);
        try {
            if(socketDomain !=null){
                socketDomain.getSession().getAsyncRemote().sendText(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendMessageToAllExpectSelf(String message, Session fromSession) {
        for(Map.Entry<String, SocketDomain> client : websocketMap.entrySet()){
            Session toSession = client.getValue().getSession();
            if( !toSession.getId().equals(fromSession.getId())&&toSession.isOpen()){
                toSession.getAsyncRemote().sendText(message);
                logger.info("服务端发送消息给"+client.getKey()+":"+message);
            }
        }
    }

    private void sendMessageToAll(String message){
        for(Map.Entry<String, SocketDomain> client : websocketMap.entrySet()){
            Session toSeesion = client.getValue().getSession();
            if(toSeesion.isOpen()){
                toSeesion.getAsyncRemote().sendText(message);
                logger.info("服务端发送消息给"+client.getKey()+":"+message);
            }
        }
    }
    //给外部调用的方法接口
    public void sendAll(String Message){
        sendMessageToAll(Message);
    }
}
