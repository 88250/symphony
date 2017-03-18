/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.processor.channel;

import org.b3log.latke.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Char room channel.
 *
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.0.0.0, Mar 16, 2017
 * @since 2.0.1
 */
@ServerEndpoint(value = "/gobang-game-channel", configurator = Channels.WebSocketConfigurator.class)
public class GobangChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GobangChannel.class.getName());

    /**
     * Session set.
     */
    public static final Map<String,Session> SESSIONS = new ConcurrentHashMap<String,Session>();

    //正在进行中的棋局
    public static final Map<Long,ChessGame> chessPlaying=new ConcurrentHashMap<Long,ChessGame>();
    //等待的棋局队列
    public static final Queue<ChessGame> chessRandomWait=new ConcurrentLinkedQueue<ChessGame>();
    //对手
    public static final Map<String,String> antiPlayer=new ConcurrentHashMap<String,String>();

    //等待指定用户的棋局（暂不实现）
    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        String player=(String) Channels.getHttpParameter(session,"player");
        LOGGER.info("new connection from "+player);
        SESSIONS.put(player,session);
        ChessGame playing=chessPlaying.get(player);
        if(playing!=null){
            LOGGER.info("ING...");
            synchronized (session) {
                if(session.isOpen() ){
                    session.getAsyncRemote().sendText("");
                }
            }
        }else if(chessRandomWait.size()!=0){
            ChessGame chessGame=chessRandomWait.poll();
            chessGame.setPlayer2(player);
            chessPlaying.put(chessGame.getChessId(),chessGame);
            antiPlayer.put(chessGame.getPlayer1(),chessGame.getPlayer2());
        }else{
            ChessGame chessGame=new ChessGame(player);
            chessRandomWait.add(chessGame);
        }
    }

    /**
     * Called when the connection closed.
     *
     * @param session session
     * @param closeReason close reason
     */
    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        removeSession(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @OnMessage
    public void onMessage(final String message) throws JSONException {
        JSONObject jsonObject= new JSONObject(message);
        switch(jsonObject.optInt("type")){
            case 1: //聊天
                JSONObject sendText= new JSONObject();
                String anti=getAntiPlayer(jsonObject.optString("player"));
                sendText.put("type",1);
                sendText.put("player",anti);
                sendText.put("message",jsonObject.optString("message"));
                SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                break;
            case 2: //落子
                break;
        }
        if(jsonObject.optString("type").equals("chat")){
            LOGGER.info("chat:>"+jsonObject.optString("message"));
        }
    }

    /**
     * Called in case of an error.
     *
     * @param session session
     * @param error error
     */
    @OnError
    public void onError(final Session session, final Throwable error) {
        removeSession(session);
    }

    /**
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final Session session) {
//        SESSIONS.remove(session);
//
//        synchronized (SESSIONS) {
//            final Iterator<Session> i = SESSIONS.iterator();
//            while (i.hasNext()) {
//                final Session s = i.next();
//
//                if (s.isOpen()) {
//                    final String msgStr = new JSONObject().put(Common.GOBANG_PLAYER, SESSIONS.size()).put(Common.TYPE, "gobangPlayer").toString();
//                    s.getAsyncRemote().sendText(msgStr);
//                }
//            }
//        }
    }

    private String getAntiPlayer(String player){
        String anti=antiPlayer.get("player");
        if(null==anti||anti.equals("")){
            for(String temp:antiPlayer.keySet()){
                if(player.equals(antiPlayer.get(temp))){
                    anti=temp;
                }
            }
        }
        return anti;
    }
}
class ChessGame{
    private long chessId;
    private String player1;
    private String player2;
    private int state;//0空桌，1，等待，2满员
    private int[][] chess=null;
    public ChessGame(String player1){
        this.chessId=System.currentTimeMillis();
        this.player1=player1;
        this.chess=new int[20][20];
        for(int i=0;i<20;i++){
            for(int j=0;j<20;j++){
                chess[i][j]=0;
            }
        }
    }

    public long getChessId() {
        return chessId;
    }

    public void setChessId(long chessId) {
        this.chessId = chessId;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}