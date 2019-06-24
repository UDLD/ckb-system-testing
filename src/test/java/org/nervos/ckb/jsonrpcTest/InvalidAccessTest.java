package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.JsonrpcRequestBody;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class InvalidAccessTest extends RPCTestBase {


    // test case detail: ${TCMS}/testcase-view-847-2
    @Test(dataProvider = "negativeNoMethodData")
    public void testNoMethodNegative(String negativeData) {
        JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
        Assert.assertEquals(jsonObject.getJSONObject("error").get("message"),"Method not found");
        Assert.assertEquals(jsonObject.getJSONObject("error").get("code"),-32601);
    }

    // test case detail: ${TCMS}/testcase-view-848-2
    @Test
    public void testUseGetNegative() throws URISyntaxException {
        JSONObject jsonObject = JSONObject.parseObject(sendGettipblocknumberMethodByGet(url));
        Assert.assertNull(jsonObject);
    }

    // test case detail: ${TCMS}/testcase-view-850-2
    @Test(dataProvider = "negativeInvalidJsonrpcData")
    public void testInvalidJsonrpcNegative(String negativeData) throws URISyntaxException {
        JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
        Assert.assertEquals(jsonObject.getJSONObject("error").get("message"),"Invalid request");
    }

    // test case detail: ${TCMS}/testcase-view-851-2
    @Test(dataProvider = "negativeInvalidIdData")
    public void testInvalidIdNegative(String negativeData) throws URISyntaxException {
        JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
        Assert.assertEquals(jsonObject.getJSONObject("error").get("message"),"Parse error");
    }



    @DataProvider
    public Object[][] negativeNoMethodData() {
        return new Object[][]{
                {buildJsonrpcRequest("get_transaction-1","0x3abd21e6e51674bb961bb4c5f3cee9faa5da30e64be10628dc1cef292cbae324")},
        };
    }

    @DataProvider
    public Object[][] negativeInvalidJsonrpcData() {
        return new Object[][]{
                {setKeyAndValue(buildJsonrpcRequest("get_tip_header"),"jsonrpc","1.0")},
        };
    }

    @DataProvider
    public Object[][] negativeInvalidIdData() {
        return new Object[][]{
                {setKeyAndValue(buildJsonrpcRequest("get_tip_header"),"id",-10)},
        };
    }




    /**
     * 使用Get方式请求get_tip_block_number
     */
    public static String sendGettipblocknumberMethodByGet(String sendUrl) throws URISyntaxException {
        CloseableHttpClient client = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder(sendUrl);
        //构造访问参数
        uriBuilder.addParameter("id", "2");
        uriBuilder.addParameter("jsonrpc", "2.0");
        uriBuilder.addParameter("method", "get_tip_block_number");

        URI uri =uriBuilder.build();
        HttpGet httpGet = new HttpGet(uri);

        String responseContent = null;
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                responseContent = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (IOException e) {
            System.out.println("Request URL:" + sendUrl);
            System.out.println("Response content:" + responseContent);
            e.printStackTrace();
        } finally {
            HttpUtils.close(response, client);
        }
        return responseContent;
    }

    /**
     * 修改json，添加字段或修改字段的值
     */
    public static String setKeyAndValue(String source, String key,Object value)  {

        JSONObject tmp = (JSONObject) JSONObject.parse(source);
        tmp.put(key,value);
        JSON jsonObj = (JSON) JSONObject.toJSON(tmp);
        return jsonObj.toJSONString();

    }


}
