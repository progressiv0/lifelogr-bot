package de.lifelogr.communicator;

import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.logging.BotLogger;

public class Communicator
{
    private static Communicator instance = null;
    private static TelegramBot bot = null;
    private static final Object mutex = new Object();

    private Communicator()
    {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        bot = new TelegramBot();
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            BotLogger.error("REGISTER", e);
        }
    }

    public static Communicator getInstance()
    {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new Communicator();
                }
            }
        }

        return instance;
    }

    public void sendMessage(String userId, String message) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(userId);
        sendMessageRequest.setText(message);

        try {
            bot.sendMessage(sendMessageRequest);
        } catch (TelegramApiException e) {
            BotLogger.error("LIFELOG", e);
        }
    }
}
