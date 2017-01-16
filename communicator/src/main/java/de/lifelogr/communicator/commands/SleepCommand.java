package de.lifelogr.communicator.commands;

import de.lifelogr.communicator.services.Emoji;
import de.lifelogr.dbconnector.impl.ICRUDUserImpl;
import de.lifelogr.dbconnector.services.ICRUDUser;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.ICommandRegistry;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Command: /sleep
 * <p>
 * BotCommand for setting a do-not-disturb range. (No Notifications and Recommendations)
 *
 * @author Marco Kretz
 */
public class SleepCommand extends BotCommand
{
    private static final String LOGTAG = "SLEEPCOMMAND";
    private final ICommandRegistry commandRegistry;

    /**
     * Constructor
     *
     * @param commandRegistry Global command-registry
     */
    public SleepCommand(ICommandRegistry commandRegistry)
    {
        super("sleep", "Ruhezeit einrichten z.B. \"/sleep 1h\" für eine Stunde");
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments)
    {
        ICRUDUser icrudUser = new ICRUDUserImpl();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        StringBuilder builder = new StringBuilder();

        // Cancel if User does not have a profile
        de.lifelogr.dbconnector.entity.User currentUser = icrudUser.getUserByTelegramId(user.getId());
        if (currentUser == null) {
            return;
        }

        if (arguments.length == 1) {
            String param = arguments[0].toLowerCase().trim();
            if (param.matches("([1-9][0-9]*)([h|m|s])")) {
                int length = Integer.parseInt(param.substring(0, param.length() - 1));
                String unit = param.substring(param.length() - 1);

                Calendar calendar = Calendar.getInstance();
                switch (unit) {
                    case "h":
                        calendar.add(Calendar.HOUR, length);
                        break;
                    case "m":
                        calendar.add(Calendar.MINUTE, length);
                        break;
                    case "s":
                        calendar.add(Calendar.SECOND, length);
                        break;
                }

                currentUser.setDndUntil(calendar.getTime());
                icrudUser.saveUser(currentUser);

                String dndDate = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
                String dndTime = new SimpleDateFormat("hh:mm").format(calendar.getTime());
                builder
                        .append("Alles klar, bis zum ")
                        .append(dndDate)
                        .append(" um ")
                        .append(dndTime)
                        .append(" Uhr hörst du nichts mehr von mir!");
            }
        } else {
            builder.append("Tut mir Leid, da ist ein Fehler drin. Versuch's mal mit \"/sleep 1h\"");
        }

        try {
            message.setText(builder.toString());
            absSender.sendMessage(message);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
