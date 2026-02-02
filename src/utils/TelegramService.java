package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import models.Payment;

public class TelegramService {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    
    public static void sendKhmerPaymentNotification(Payment payment) {
        if (!TelegramConfig.isEnabled()) {
            return;
        }

        new Thread(() -> {
            try {
                System.out.println("📤 Telegram: Preparing Khmer notification...");
                String message = formatKhmerPaymentMessage(payment);
                System.out.println("📤 Telegram: Sending message to " + TelegramConfig.CHAT_ID);
                sendMessage(message);
            } catch (Exception e) {
                System.err.println("❌ Telegram: Failed to send Khmer notification: " + e.getMessage());
            }
        }).start();
    }

    private static String formatKhmerPaymentMessage(Payment payment) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String dateStr = sdf.format(payment.getPaymentDate() != null ? payment.getPaymentDate() : new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("ការកក់កន្លែងចតរថយន្តជោគជ័យ!\n");
        sb.append("-លេខវិកយប័ត្រ ៖ ").append(payment.getBookingRef() != null ? payment.getBookingRef() : "N/A")
                .append("\n");
        sb.append("-តម្លៃសរុប   ៖ ").append(String.format("%,.2f", payment.getPaidAmount())).append(" $\n");
        sb.append("-អ្នកគិតលុយ   ៖ ").append(payment.getFullName() != null ? payment.getFullName() : "System")
                .append("\n");
        sb.append("-តាមរយះ    ៖ Web\n");  
        sb.append("-កាលបរិច្ឆេទ  ៖ ").append(dateStr);
        return sb.toString();
    }   

    private static void sendMessage(String text) throws Exception {
        String urlString = "https://api.telegram.org/bot" + TelegramConfig.BOT_TOKEN + "/sendMessage";

        String query = "chat_id=" + TelegramConfig.CHAT_ID +
                "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                "&parse_mode=Markdown";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("Telegram API error: " + response.body());
                    }
                });
    }
}
