import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class MyHttpClientDemoApplication {

    public static void main(String[] args) {

        // 使用HttpClient来访问localhost:8801
        String response1 = doGetResponseByHttpClient();
        System.out.println(response1);

        // 使用OKHttp来访问localhost:8801
        String response2 = doGetResponseByOkHttp();
        System.out.println(response2);
    }

    private static String doGetResponseByOkHttp() {

        String responseBody = null;

        // 1.new okhttp
        OkHttpClient okHttpClient = new OkHttpClient();
        // 2.new Request
        Request request = new Request.Builder().url("http://localhost:8801/").build();
        // 3.封装Call
        Call call = okHttpClient.newCall(request);
        // 4.开始调用
        // 同步
        try (Response response = call.execute()) {

            // 判断调用成功与否
            boolean isSuccess = response.isSuccessful();
            // 忽略业务判断

            responseBody = response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return responseBody;
    }

    private static String doGetResponseByHttpClient() {

        String responseBody = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet httpGet = new HttpGet("http://localhost:8801/");
            CloseableHttpResponse response = httpClient.execute(httpGet);

            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            // ........ 处理状态码忽略

            // 响应实体
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseBody;
    }
}
