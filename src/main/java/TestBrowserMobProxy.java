import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.HarEntry;


public class TestBrowserMobProxy {

    String sFileName = "/home/alexey/Dev/Selenium/CaptureTraffic_Selenium_debugmode/output/harfile.har";

    @Test
    public void test() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        BrowserMobProxy proxy = getProxyServer(); //getting browsermob proxy


        Proxy seleniumProxy = getSeleniumProxy(proxy);
//        seleniumProxy.setSslProxy("trustAllSSLCertificates");


        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
//        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        WebDriver driver = new ChromeDriver(capabilities);

        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT,
                                    CaptureType.RESPONSE_CONTENT,
                                    CaptureType.REQUEST_BINARY_CONTENT,
                                    CaptureType.RESPONSE_BINARY_CONTENT,
                                    CaptureType.REQUEST_HEADERS,
                                    CaptureType.RESPONSE_HEADERS,
                                    CaptureType.REQUEST_COOKIES,
                                    CaptureType.RESPONSE_COOKIES);

        proxy.newHar(); // creating new HAR

        driver.get("https://petstore.swagger.io");
        Thread.sleep(5000);
        driver.findElement(By.cssSelector("#operations-pet-addPet > div.opblock-summary.opblock-summary-post")).click();
        driver.findElement(By.cssSelector("#operations-pet-addPet > div:nth-child(2) > div > div.opblock-section > div.opblock-section-header > div.try-out > button")).click();
        Thread.sleep(5000);
        driver.findElement(By.cssSelector("#operations-pet-addPet > div:nth-child(2) > div > div.execute-wrapper > button")).click();


        Har har = proxy.getHar();

        File harFile = new File(sFileName);
        try {
            har.writeTo(harFile);
        } catch (IOException ex) {
            System.out.println (ex.toString());
            System.out.println("Could not find file " + sFileName);
        }


        List<HarEntry> entries = proxy.getHar().getLog().getEntries();
        for (HarEntry entry : entries) {
//            System.out.println(entry.getRequest().getUrl());
        }
        proxy.stop();
        driver.close();
    }
    public Proxy getSeleniumProxy(BrowserMobProxy proxyServer) {
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxyServer);
        try {
            String hostIp = Inet4Address.getLocalHost().getHostAddress();
            seleniumProxy.setHttpProxy(hostIp + ":" + proxyServer.getPort());
            seleniumProxy.setSslProxy(hostIp + ":" + proxyServer.getPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Assert.fail("invalid Host Address");
        }
        return seleniumProxy;
    }
    public BrowserMobProxy getProxyServer() {
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.setTrustAllServers(true);
        proxy.start();
        return proxy;
    }
}