package utils;

public class TelegramConfig {
    // To get a token, message @BotFather on Telegram
    public static final String BOT_TOKEN = "8514036203:AAFtIa-x9q31Xoybz15EzH40FJk3-tLEPfU";

    // To get your chat ID, message @userinfobot on Telegram
    public static final String CHAT_ID = "853828296";

    public static boolean isEnabled() {
        return BOT_TOKEN != null && !BOT_TOKEN.equals("YOUR_BOT_TOKEN_HERE") &&
                CHAT_ID != null && !CHAT_ID.equals("YOUR_CHAT_ID_HERE");
    }
}
