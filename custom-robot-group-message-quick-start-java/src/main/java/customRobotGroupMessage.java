import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

public class DingTalkBot {

    // ⚠️ 不要写死在代码里，建议用环境变量
    private static final String WEBHOOK = System.getenv("DINGTALK_WEBHOOK");
    private static final String SECRET = System.getenv("DINGTALK_SECRET");

    public static void main(String[] args) throws Exception {
        sendText("测试消息：机器人已正常工作");
    }

    public static void sendText(String content) throws Exception {

        String url = WEBHOOK;

        // ===== 如果开启“加签”，走这里 =====
        if (SECRET != null && !SECRET.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            String sign = getSign(timestamp, SECRET);

            url = WEBHOOK + "&timestamp=" + timestamp + "&sign=" + sign;
        }

        // ===== HTTP 请求 =====
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        String json = "{"
                + "\"msgtype\":\"text\","
                + "\"text\":{"
                + "\"content\":\"" + content + "\""
                + "}"
                + "}";

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes("utf-8"));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
    }

    // ===== 生成签名 =====
    private static String getSign(long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("utf-8"), "HmacSHA256"));

        byte[] signData = mac.doFinal(stringToSign.getBytes("utf-8"));

        return URLEncoder.encode(Base64.getEncoder().encodeToString(signData), "UTF-8");
    }
}
