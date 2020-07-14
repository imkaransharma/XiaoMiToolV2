package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.GuiListener;
import com.xiaomitool.v2.procedure.GuiListenerAbstract;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.CommandClassAbstract;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class InstallPane extends StackPane implements GuiListenerAbstract, CommandClassAbstract {

    private TextStackPane textStackPane;

    public InstallPane() {
        build();
    }

    private void build() {
        double winWidth = WindowManager.getContentWidth(), winHeight = WindowManager.getContentHeight();
        LoadingAnimation loadingAnimation = new LoadingAnimation(winWidth / 6);
        double animHeight = loadingAnimation.getCircleRadius() + 30;
        textStackPane = new TextStackPane(winWidth, winHeight - animHeight);
        VBox vBox = new VBox(textStackPane, loadingAnimation);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);
        super.getChildren().add(vBox);
    }


    @Override
    public void text(String message) {
        if (textStackPane == null) {
            toast(message);
            return;
        }
        textStackPane.addText(message);
    }

    @Override
    public void toast(String message) {
        WindowManager.toast(message);
    }

    @Override
    public void onException(InstallException exception) {
        WindowManager.setOnExitAskForFeedback(false); //TOO MANY FEEDBACKS
        Log.log("FATAL", exception.toString(), true);
        Log.exc(exception);
        Log.exc(new Exception("TraceBackException"));
        Log.printStackTrace(exception);
        String stackTrace = StrUtils.exceptionToString(exception);
        int len = stackTrace.length();
        stackTrace = StrUtils.firstNLines(stackTrace, 5);
        ErrorPane errorPane = new ErrorPane(LRes.CANCEL, LRes.STEP_BACK, LRes.TRY_AGAIN);
        errorPane.setTitle(LRes.PROCEDURE_EXC_TITLE.toString(), Color.rgb(128, 0, 0));
        errorPane.setText(LRes.PROCEDURE_EXC_TEXT.toString(LRes.PROCEDURE_EXC_DETAILS.toString(exception.getCode().toString(), exception.getMessage()) + "\n", LRes.TRY_AGAIN, LRes.STEP_BACK, LRes.CANCEL));
        Text t2 = new Text(LRes.PROCEDURE_EXC_ADV_DETAILS.toString() + ": " + exception.getMessage() + "\n" + stackTrace + (stackTrace.length() != len ? "\n..." : ""));
        t2.setTextAlignment(TextAlignment.CENTER);
        t2.setWrappingWidth(WindowManager.getContentWidth() - 100);
        t2.setFont(Font.font(14));
        t2.setFill(Color.gray(0.15));
        errorPane.appendContent(t2);
        WindowManager.setMainContent(errorPane, false);
        int msg;
        try {
            msg = errorPane.waitClick();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WindowManager.removeTopContent();
        if (msg == 0) {
            this.sendCommand(CommandClass.Command.ABORT);
        } else if (msg == 1) {
            this.sendCommand(CommandClass.Command.UPLEVEL);
        } else {
            this.sendCommand(CommandClass.Command.RETRY);
        }
    }

    private final CommandClass commandManager = new CommandClass();

    @Override
    public void sendCommand(CommandClass.Command cmd) {
        commandManager.sendCommand(cmd);
    }

    @Override
    public CommandClass.Command waitCommand() throws InterruptedException {
        return commandManager.waitCommand();
    }

    @Override
    public boolean isWaitingCommand() {
        return commandManager.isWaitingCommand();
    }
}
