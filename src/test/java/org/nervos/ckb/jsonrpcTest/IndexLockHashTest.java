package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.NetworkUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IndexLockHashTest extends RPCTestBase {

    //rString createNode = "mkdir node1 && ckb -C node1 init && sed -i 's/Stats\\\", \\\"Experiment/Stats\\\", \\\"Indexer\\\", \\\"Experiment/' ./node1/ckb.toml";

    private int idlePort = NetworkUtils.getIdlePort();
    private CKBSystem getPeersCkbSystem;
    private String dockerName;
    private String ckbDockerImageTagName;
    private String rpcURL = "http://127.0.0.1:" + idlePort;

    @BeforeClass
    public void initGetPeerEnv() throws InterruptedException {
        // start a local chain
        getPeersCkbSystem = new CKBSystem();
        dockerName = getPeersCkbSystem.getDockerName();
        ckbDockerImageTagName = getPeersCkbSystem.getCkbDockerImageTagName();
        //ckbDockerImageTagName="v0.15.0-rc1";
        getPeersCkbSystem.enableDebug();
        getPeersCkbSystem.cleanEnv();

        // run a idle container
        getPeersCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

        // init ckb
        getPeersCkbSystem.initCKB();
        String addIndexerModule1 = "sed -i 's/Stats\\\", \\\"Experiment/Stats\\\", \\\"Experiment\\\", \\\"Indexer/' ckb.toml";
        getPeersCkbSystem.runCommandWithDocker(addIndexerModule1);
        // run ckb
        String ckbNode1Run = "ckb  run";
        getPeersCkbSystem.runCommandWithDocker(ckbNode1Run, "-d -it");

        Thread.sleep(3000);
    }

    @AfterClass
    public void cleanGetPeerEnv() {
        getPeersCkbSystem.cleanEnv();
    }

    // test case detail: ${TCMS}/testcase-view-1480-1
    @Test(dataProvider = "positiveData")
    public void testIndexLockHashPositive(String positiveData) {
        JSONObject jsonObject = JSONObject
                .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
        Assert.assertNull(jsonObject.get("error"));
    }

    // test case detail: ${TCMS}/testcase-view-1485-2
    @Test(dataProvider = "positiveNoBlockNumData")
    public void testIndexLockHashNoBlockNumPositive(String positiveData) {
        JSONObject jsonObject = JSONObject
                .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
        Assert.assertNull(jsonObject.get("error"));
    }

    // test case detail: ${TCMS}/testcase-view-1488-2
    @Test(dataProvider = "positiveBigBlockNumData")
    public void testIndexLockHashBigBlockNumPositive(String positiveData) {
        JSONObject jsonObject = JSONObject
                .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
        Assert.assertNull(jsonObject.get("error"));
    }

    // test case detail: ${TCMS}/testcase-view-1481-2
    @Test(dataProvider = "positiveReIndexData")
    public void testReIndexLockHashPositive(String positiveData) {
        HttpUtils.sendJson(rpcURL, buildJsonrpcRequest("index_lock_hash", lockHash, "1"));
        JSONObject jsonObject = JSONObject
                .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
        Assert.assertNull(jsonObject.get("error"));
    }

    // test case detail: ${TCMS}/testcase-view-1482-3
    // test case detail: ${TCMS}/testcase-view-1483-3
    // test case detail: ${TCMS}/testcase-view-1484-3
    // test case detail: ${TCMS}/testcase-view-1486-3
    // test case detail: ${TCMS}/testcase-view-1487-3
    // test case detail: ${TCMS}/testcase-view-1489-3
    @Test(dataProvider = "negativeData")
    public void testIndexLockHashNegative(String negativeData) {
        JSONObject jsonObject = JSONObject
                .parseObject(HttpUtils.sendJson(rpcURL, negativeData));
        printout(negativeData);
        printout(jsonObject.toJSONString());
        Assert.assertNotNull(jsonObject.get("error"));

    }

    @DataProvider
    public Object[][] negativeData() {
        return new Object[][]{
                {buildJsonrpcRequest("index_lock_hash")},
                {buildJsonrpcRequest("index_lock_hash", "")},
                {buildJsonrpcRequest("index_lock_hash", lockHash, "")},
                {buildJsonrpcRequest("index_lock_hash", lockHash, "1", "2")},
                {buildJsonrpcRequest("index_lock_hash", lockHash + "98", "1000000")},
                {buildJsonrpcRequest("index_lock_hash", lockHash.substring(0, 64) + "66", "1000000", lockHash)},
        };
    }

    @DataProvider
    public Object[][] positiveData() throws Exception {
        waitForBlockHeight(1, 180, 1);
        return new Object[][]{
                {buildJsonrpcRequest("index_lock_hash", lockHash, "1")},

        };
    }

    @DataProvider
    public Object[][] positiveNoBlockNumData() throws Exception {
        waitForBlockHeight(1, 180, 1);
        return new Object[][]{
                {buildJsonrpcRequest("index_lock_hash", lockHash)},
        };
    }

    @DataProvider
    public Object[][] positiveBigBlockNumData() throws Exception {
        waitForBlockHeight(1, 180, 1);
        return new Object[][]{
                {buildJsonrpcRequest("index_lock_hash", lockHash, "1000000")},

        };
    }

    @DataProvider
    public Object[][] positiveReIndexData() throws Exception {
        waitForBlockHeight(1, 180, 1);
        return new Object[][]{
                {buildJsonrpcRequest("index_lock_hash", lockHash, "1")},

        };
    }

}
