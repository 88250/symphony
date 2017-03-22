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
 * Gobang game channel.
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
    public static final Map<String,ChessGame> chessPlaying=new ConcurrentHashMap<String,ChessGame>();
    //对手，与正在进行的棋局Map配套使用
    public static final Map<String,String> antiPlayer=new ConcurrentHashMap<String,String>();

    //等待的棋局队列
    public static final Queue<ChessGame> chessRandomWait=new ConcurrentLinkedQueue<ChessGame>();


    //等待指定用户的棋局（暂不实现）
    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        String player=(String) Channels.getHttpParameter(session,"player");
        boolean playing=false;
        LOGGER.info("new connection from "+player);
        SESSIONS.put(player,session);
        for(String temp:chessPlaying.keySet()){
            if(player.equals(chessPlaying.get(temp).getPlayer1())
                    ||player.equals(chessPlaying.get(temp).getPlayer2())){
                playing=true;
            }
        }
        if(playing){
            return;
        }else if(playing == false && chessRandomWait.size()!=0){
            ChessGame chessGame=chessRandomWait.poll();
            chessGame.setPlayer2(player);
            chessGame.setStep(1);
            chessPlaying.put(chessGame.getPlayer1(),chessGame);
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
        final String player=jsonObject.optString("player");
        final String anti=getAntiPlayer(player);
        JSONObject sendText= new JSONObject();
        switch(jsonObject.optInt("type")){
            case 1: //聊天
                sendText.put("type",1);
                sendText.put("player",player);
                sendText.put("message",jsonObject.optString("message"));
                SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                break;
            case 2: //落子
                ChessGame chessGame=chessPlaying.keySet().contains(player)?chessPlaying.get(player):chessPlaying.get(anti);
                int x=jsonObject.optInt("x");
                int y=jsonObject.optInt("y");
                int size=jsonObject.optInt("size");
                if(chessGame!=null){
                    boolean flag=false;
                    if(player.equals(chessGame.getPlayer1())){
                        flag=chessGame.chessCheck(1);
                        if(chessGame.getStep()!=1){
                            return;
                        }else{
                            chessGame.setStep(2);
                        }
                        sendText.put("color","black");
                        chessGame.getChess()[x/size][y/size]=1;
                    }else{
                        flag=chessGame.chessCheck(2);
                        if(chessGame.getStep()!=2){
                            return;
                        }else{
                            chessGame.setStep(1);
                        }
                        sendText.put("color","white");
                        chessGame.getChess()[x/size][y/size]=2;
                    }
                    sendText.put("type",2);
                    sendText.put("player",player);
                    sendText.put("posX",x);
                    sendText.put("posY",y);
                    if(flag){
                        sendText.put("result","You win");
                    }
                    SESSIONS.get(player).getAsyncRemote().sendText(sendText.toString());
                    if(flag){
                        sendText.put("result","You Lose");
                    }
                    SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                }
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
        for(String temp:SESSIONS.keySet()){
            if(session.equals(SESSIONS.get(temp))){
                chessPlaying.remove(temp);
                String anti=getAntiPlayer(temp);
                if(anti!=null && !anti.equals("")){
                    chessPlaying.remove(anti);
                }
                SESSIONS.remove(temp);
            }
        }
    }

    private String getAntiPlayer(String player){
        String anti=antiPlayer.get(player);
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
    private int step;//1-player1,2-player2;
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
    
    public boolean chessCheck(int step){
        //横向检查
        for(int i=0;i<this.chess.length;i++){
            int count=0;
            for(int j=0;j<this.chess[i].length;j++){
                if(this.chess[i][j]==step){
                    count++;
                }else if(this.chess[i][j]!=step && count<5){
                    count=0;
                }
            }
            if(count>=5){
                return true;
            }
        }
        //纵向检查
        for(int j=0;j<this.chess[0].length;j++){
            int count=0;
            for(int i=0;i<this.chess.length;i++){
                if(this.chess[i][j]==step){
                    count++;
                }else if(this.chess[i][j]!=step && count<5){
                    count=0;
                }
            }
            if(count>=5){
                return true;
            }
        }
        //左上右下检查，下一个检查点时上一个检查点横纵坐标均＋1
        //横向增长，横坐标先行出局
        for(int x=0,y=0;x<this.chess.length;x++){
            int count=0;
            for(int i=x,j=y;i<this.chess.length;i++,j++){
                if(this.chess[i][j]==step){
                    count++;
                }else if(this.chess[i][j]!=step && count<5){
                    count=0;
                }
            }
            if(count>=5){
                return true;
            }
        }
        //纵向增长，纵坐标先出局
        for(int x=0,y=0;y<this.chess[0].length;y++){
            int count=0;
            for(int i=x,j=y;j<this.chess.length;i++,j++){
                if(this.chess[i][j]==step){
                    count++;
                }else if(this.chess[i][j]!=step && count<5){
                    count=0;
                }
            }
            if(count>=5){
                return true;
            }
        }
        //左下右上检查x-1,y+1
        //横向增长，横坐标先行出局
        for(int x=0,y=0;x<this.chess.length;x++){
            int count=0;
            for(int i=x,j=y;i>=0;i--,j++){
                if(this.chess[i][j]==step){
                    count++;
                }else if(this.chess[i][j]!=step && count<5){
                    count=0;
                }
            }
            if(count>=5){
                return true;
            }
        }
        //纵向增长，纵坐标先出局
        for(int x=this.chess.length-1,y=0;y<this.chess[0].length;y++){
            int count=0;
            for(int i=x,j=y;j<this.chess.length;i--,j++){
                if(this.chess[i][j]==step){
                    count++;
                }else if(this.chess[i][j]!=step && count<5){
                    count=0;
                }
            }
            if(count>=5){
                return true;
            }
        }
        return false;
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int[][] getChess() {
        return chess;
    }

    public void setChess(int[][] chess) {
        this.chess = chess;
    }
}