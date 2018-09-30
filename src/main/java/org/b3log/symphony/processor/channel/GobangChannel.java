/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.processor.channel;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.service.ActivityMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Gobang game channel.
 * <p>
 * 状态值约定（为取值方便不做enum或者常量值了，当然日后或许重构）
 * 1：聊天，2：下子，3：创建游戏，等待加入，4：加入游戏，游戏开始，5：断线重连，恢复棋盘，6：系统通知，7：请求和棋
 * </p>
 *
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.4, Jul 24, 2017
 * @since 2.1.0
 */
@ServerEndpoint(value = "/gobang-game-channel", configurator = Channels.WebSocketConfigurator.class)
public class GobangChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GobangChannel.class);

    /**
     * Session set.
     */
    public static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 正在进行中的棋局.
     * String参数代表开局者（选手1）的userId
     * ChessGame参数代表棋局
     */
    public static final Map<String, ChessGame> chessPlaying = new ConcurrentHashMap<>();

    /**
     * 对手，与正在进行的棋局Map配套使用.
     * 第一个String代表player1,
     * 第二个String代表player2
     */
    public static final Map<String, String> antiPlayer = new ConcurrentHashMap<>();

    /**
     * 等待的棋局队列.
     */
    public static final Queue<ChessGame> chessRandomWait = new ConcurrentLinkedQueue<>();

    /**
     * Activity management service.
     */
    @Inject
    private ActivityMgmtService activityMgmtService;

    // 等待指定用户的棋局（暂不实现）

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }
        final String userId = user.optString(Keys.OBJECT_ID);
        final String userName = user.optString(User.USER_NAME);
        boolean playing = false;
        LOGGER.debug("new connection from " + userName);
        if (SESSIONS.containsKey(userId)) {
            JSONObject sendText = new JSONObject();
            sendText.put("type", 6);
            sendText.put("message", "【系统】：您已在匹配队列中，请勿开始多个游戏，如需打开新的窗口，请先关闭原窗口再开始");
            session.getAsyncRemote().sendText(sendText.toString());
            return;
        } else {
            SESSIONS.put(userId, session);
        }
        for (String temp : chessPlaying.keySet()) {
            ChessGame chessGame = chessPlaying.get(temp);
            if (userId.equals(chessGame.getPlayer1())) { //玩家1返回战局
                recoverGame(userId, userName, chessGame.getPlayer2(), chessGame);
                chessGame.setPlayState1(true);
                playing = true;
            } else if (userId.equals(chessGame.getPlayer2())) { //玩家2返回战局
                recoverGame(userId, userName, chessGame.getPlayer1(), chessGame);
                chessGame.setPlayState2(true);
                playing = true;
            }
        }
        if (playing) {
            return;
        } else {
            ChessGame chessGame = null;
            JSONObject sendText = new JSONObject();

            do {
                chessGame = chessRandomWait.poll();
            } while (chessRandomWait.size() > 0 && SESSIONS.get(chessGame.getPlayer1()) == null);

            if (chessGame == null) {
                chessGame = new ChessGame(userId, userName);
                chessRandomWait.add(chessGame);
                sendText.put("type", 3);
                sendText.put("playerName", userName);
                sendText.put("message", "【系统】：请等待另一名玩家加入游戏");
                session.getAsyncRemote().sendText(sendText.toString());
            } else if (userId.equals(chessGame.getPlayer1())) { //仍然在匹配队列中
                chessRandomWait.add(chessGame);//重新入队
                sendText.put("type", 3);
                sendText.put("playerName", userName);
                sendText.put("message", "【系统】：请等待另一名玩家加入游戏");
                session.getAsyncRemote().sendText(sendText.toString());
            } else {
                final BeanManager beanManager = BeanManager.getInstance();
                chessGame.setPlayer2(userId);
                chessGame.setName2(userName);
                chessGame.setPlayState2(true);
                chessGame.setStep(1);
                chessPlaying.put(chessGame.getPlayer1(), chessGame);
                antiPlayer.put(chessGame.getPlayer1(), chessGame.getPlayer2());

                final ActivityMgmtService activityMgmtService = beanManager.getReference(ActivityMgmtService.class);


                sendText.put("type", 4);

                //针对开局玩家的消息
                sendText.put("message", "【系统】：玩家 [" + userName + "] 已加入，游戏开始，请落子");
                sendText.put("player", chessGame.getPlayer1());

                SESSIONS.get(chessGame.getPlayer1()).getAsyncRemote().sendText(sendText.toString());
                //针对参与玩家的消息
                sendText.put("message", "游戏开始~！您正在与 [" + chessGame.getName1() + "] 对战");
                sendText.put("player", chessGame.getPlayer2());
                session.getAsyncRemote().sendText(sendText.toString());

                JSONObject r1 = activityMgmtService.startGobang(chessGame.getPlayer1());
                JSONObject r2 = activityMgmtService.startGobang(chessGame.getPlayer2());
            }
        }
    }

    /**
     * Called when the connection closed.
     *
     * @param session     session
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
        JSONObject jsonObject = new JSONObject(message);
        final String player = jsonObject.optString("player");
        final String anti = getAntiPlayer(player);
        JSONObject sendText = new JSONObject();
        final BeanManager beanManager = BeanManager.getInstance();
        switch (jsonObject.optInt("type")) {
            case 1: //聊天
                LOGGER.debug(jsonObject.optString("message"));
                final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
                sendText.put("type", 1);
                try {
                    sendText.put("player", userQueryService.getUser(player).optString(User.USER_NAME));
                } catch (ServiceException e) {
                    LOGGER.error("service not avaliable");
                }
                sendText.put("message", jsonObject.optString("message"));
                SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                break;
            case 2: //落子
                ChessGame chessGame = chessPlaying.keySet().contains(player) ? chessPlaying.get(player) : chessPlaying.get(anti);
                int x = jsonObject.optInt("x");
                int y = jsonObject.optInt("y");
                int size = jsonObject.optInt("size");
                if (chessGame != null) {
                    if (chessGame.getChess()[x / size][y / size] != 0) {
                        return;
                    }
                    boolean flag = false;
                    if (player.equals(chessGame.getPlayer1())) {
                        if (chessGame.getStep() != 1) {
                            return;
                        } else {
                            sendText.put("color", "black");
                            chessGame.getChess()[x / size][y / size] = 1;
                            flag = chessGame.chessCheck(1);
                            chessGame.setStep(2);
                        }
                    } else {
                        if (chessGame.getStep() != 2) {
                            return;
                        } else {
                            sendText.put("color", "white");
                            chessGame.getChess()[x / size][y / size] = 2;
                            flag = chessGame.chessCheck(2);
                            chessGame.setStep(1);
                        }
                    }
                    sendText.put("type", 2);
                    sendText.put("player", player);
                    sendText.put("posX", x);
                    sendText.put("posY", y);
                    sendText.put("chess", chessGame.getChess());
                    sendText.put("step", chessGame.getStep());
                    //chessPlaying是一个以玩家1为key的正在游戏的Map
                    //按道理，两个玩家不会出现在多个棋局（卧槽？好像一个人想跟多个人下也不是不讲道理啊……whatever）
                    //故当游戏结束时，可以按照player和anti移除两次（因为不知道哪个才是玩家1）
                    //总有一次能正确移除，分开写只是为了好看，没有逻辑原因
                    if (flag) {
                        sendText.put("result", "You Win");
                        chessPlaying.remove(player);
                    }
                    SESSIONS.get(player).getAsyncRemote().sendText(sendText.toString());
                    if (flag) {
                        sendText.put("result", "You Lose");
                        chessPlaying.remove(anti);
                    }
                    SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                    if (flag) {
                        final ActivityMgmtService activityMgmtService = beanManager.getReference(ActivityMgmtService.class);
                        activityMgmtService.collectGobang(player, Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START * 2);
                        SESSIONS.remove(player);
                        SESSIONS.remove(anti);
                    }
                }
                break;
            case 7://和棋
                if ("request".equals(jsonObject.optString("drawType"))) {
                    sendText.put("type", 7);
                    SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                } else if ("yes".equals(jsonObject.optString("drawType"))) {
                    sendText.put("type", 6);
                    sendText.put("message", "【系统】：双方和棋，积分返还，游戏结束");
                    chessPlaying.remove(player);
                    chessPlaying.remove(anti);
                    antiPlayer.remove(player);
                    antiPlayer.remove(anti);
                    final ActivityMgmtService activityMgmtService = beanManager.getReference(ActivityMgmtService.class);
                    activityMgmtService.collectGobang(player, Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START);
                    activityMgmtService.collectGobang(anti, Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START);
                    SESSIONS.get(player).getAsyncRemote().sendText(sendText.toString());
                    SESSIONS.get(anti).getAsyncRemote().sendText(sendText.toString());
                    SESSIONS.remove(player);
                    SESSIONS.remove(anti);
                } else if ("no".equals(jsonObject.optString("drawType"))) {
                    sendText.put("type", 6);
                    sendText.put("message", "【系统】：对手拒绝和棋，请继续下棋");
                    SESSIONS.get(player).getAsyncRemote().sendText(sendText.toString());
                }
                break;
        }
    }

    /**
     * Called in case of an error.
     *
     * @param session session
     * @param error   error
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
        for (String player : SESSIONS.keySet()) {
            if (session.equals(SESSIONS.get(player))) {
                if (getAntiPlayer(player) == null) {
                    for (ChessGame chessGame : chessRandomWait) {
                        if (player.equals(chessGame.getPlayer1())) {
                            chessRandomWait.remove(chessGame);
                        }
                    }
                } else {
                    if (chessPlaying.get(player) != null) { //说明玩家1断开了链接
                        ChessGame chessGame = chessPlaying.get(player);
                        chessGame.setPlayState1(false);
                        if (!chessGame.isPlayState2()) {
                            chessPlaying.remove(player);
                            antiPlayer.remove(player);
                            //由于玩家2先退出，补偿玩家1的积分
                            final BeanManager beanManager = BeanManager.getInstance();
                            final ActivityMgmtService activityMgmtService = beanManager.getReference(ActivityMgmtService.class);
                            activityMgmtService.collectGobang(chessGame.getPlayer1(), Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START);
                        } else {
                            JSONObject sendText = new JSONObject();
                            sendText.put("type", 6);
                            sendText.put("message", "【系统】：对手离开了棋局");
                            SESSIONS.get(chessGame.getPlayer2()).getAsyncRemote().sendText(sendText.toString());
                        }
                    } else if (chessPlaying.get(getAntiPlayer(player)) != null) { //说明玩家2断开了链接
                        String player1 = getAntiPlayer(player);
                        ChessGame chessGame = chessPlaying.get(player1);
                        chessGame.setPlayState2(false);
                        if (!chessGame.isPlayState1()) {
                            chessPlaying.remove(player1);
                            antiPlayer.remove(player1);
                            //由于玩家1先退出，补偿玩家2的积分
                            final BeanManager beanManager = BeanManager.getInstance();
                            final ActivityMgmtService activityMgmtService = beanManager.getReference(ActivityMgmtService.class);
                            activityMgmtService.collectGobang(chessGame.getPlayer2(), Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START);
                        } else {
                            JSONObject sendText = new JSONObject();
                            sendText.put("type", 6);
                            sendText.put("message", "【系统】：对手离开了棋局");
                            SESSIONS.get(chessGame.getPlayer1()).getAsyncRemote().sendText(sendText.toString());
                        }
                    }
                }
                SESSIONS.remove(player);
            }
        }
    }

    private String getAntiPlayer(String player) {
        String anti = antiPlayer.get(player);
        if (null == anti || anti.equals("")) {
            for (String temp : antiPlayer.keySet()) {
                if (player.equals(antiPlayer.get(temp))) {
                    anti = temp;
                }
            }
        }
        return anti;
    }

    private void recoverGame(String userId, String userName, String antiUserId, ChessGame chessGame) {
        JSONObject sendText = new JSONObject();
        sendText.put("type", 5);
        sendText.put("chess", chessGame.getChess());
        sendText.put("message", "【系统】：恢复棋盘，当前轮到玩家 [" + (chessGame.getStep() == 1 ? chessGame.getName1() : chessGame.getName2()) + "] 落子");
        sendText.put("playerName", userName);
        sendText.put("player", userId);
        SESSIONS.get(userId).getAsyncRemote().sendText(sendText.toString());
        sendText = new JSONObject();
        sendText.put("type", 6);
        sendText.put("message", "【系统】：对手返回了棋局，当前轮到玩家 [" + (chessGame.getStep() == 1 ? chessGame.getName1() : chessGame.getName2()) + "] 落子");
        SESSIONS.get(antiUserId).getAsyncRemote().sendText(sendText.toString());
    }
}

class ChessGame {
    private long chessId;
    private String player1;
    private String player2;
    private String name1;
    private String name2;
    private boolean playState1;
    private boolean playState2;
    private int state;//0空桌，1，等待，2满员
    private int[][] chess = null;
    private int step;//1-player1,2-player2;
    private long starttime;

    public ChessGame(String player1, String name1) {
        this.chessId = System.currentTimeMillis();
        this.player1 = player1;
        this.name1 = name1;
        this.playState1 = true;
        this.playState2 = false;
        this.chess = new int[20][20];
        this.starttime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                chess[i][j] = 0;
            }
        }
    }

    public boolean chessCheck(int step) {
        //横向检查
        for (int i = 0; i < this.chess.length; i++) {
            int count = 0;
            for (int j = 0; j < this.chess[i].length; j++) {
                if (this.chess[i][j] == step) {
                    count++;
                } else if (this.chess[i][j] != step && count < 5) {
                    count = 0;
                }
            }
            if (count >= 5) {
                return true;
            }
        }
        //纵向检查
        for (int j = 0; j < this.chess[0].length; j++) {
            int count = 0;
            for (int i = 0; i < this.chess.length; i++) {
                if (this.chess[i][j] == step) {
                    count++;
                } else if (this.chess[i][j] != step && count < 5) {
                    count = 0;
                }
            }
            if (count >= 5) {
                return true;
            }
        }
        //左上右下检查，下一个检查点时上一个检查点横纵坐标均＋1
        //横向增长，横坐标先行出局
        for (int x = 0, y = 0; x < this.chess.length; x++) {
            int count = 0;
            for (int i = x, j = y; i < this.chess.length; i++, j++) {
                if (this.chess[i][j] == step) {
                    count++;
                } else if (this.chess[i][j] != step && count < 5) {
                    count = 0;
                }
            }
            if (count >= 5) {
                return true;
            }
        }
        //纵向增长，纵坐标先出局
        for (int x = 0, y = 0; y < this.chess[0].length; y++) {
            int count = 0;
            for (int i = x, j = y; j < this.chess.length; i++, j++) {
                if (this.chess[i][j] == step) {
                    count++;
                } else if (this.chess[i][j] != step && count < 5) {
                    count = 0;
                }
            }
            if (count >= 5) {
                return true;
            }
        }
        //左下右上检查x-1,y+1
        //横向增长，横坐标先行出局
        for (int x = 0, y = 0; x < this.chess.length; x++) {
            int count = 0;
            for (int i = x, j = y; i >= 0; i--, j++) {
                if (this.chess[i][j] == step) {
                    count++;
                } else if (this.chess[i][j] != step && count < 5) {
                    count = 0;
                }
            }
            if (count >= 5) {
                return true;
            }
        }
        //纵向增长，纵坐标先出局
        for (int x = this.chess.length - 1, y = 0; y < this.chess[0].length; y++) {
            int count = 0;
            for (int i = x, j = y; j < this.chess.length; i--, j++) {
                if (this.chess[i][j] == step) {
                    count++;
                } else if (this.chess[i][j] != step && count < 5) {
                    count = 0;
                }
            }
            if (count >= 5) {
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

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public boolean isPlayState1() {
        return playState1;
    }

    public void setPlayState1(boolean playState1) {
        this.playState1 = playState1;
    }

    public boolean isPlayState2() {
        return playState2;
    }

    public void setPlayState2(boolean playState2) {
        this.playState2 = playState2;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }
}