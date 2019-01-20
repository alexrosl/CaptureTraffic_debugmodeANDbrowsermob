import java.util.Iterator;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class TestChromeDebug
{
    public static void main(String[] args)
    {
        // simple page (without many resources so that the output is
        // easy to understand
//        String url = "http://www.york.ac.uk/teaching/cws/wws/webpage1.html";

        String url = "https://www.google.com/";
        DownloadPage(url);
    }

    private static void DownloadPage(String url)
    {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        ChromeDriver driver = null;

        try
        {
            ChromeOptions options = new ChromeOptions();
            // add whatever extensions you need
            // for example I needed one of adding proxy, and one for blocking
            // images
            // options.addExtensions(new File(file, "proxy.zip"));
            // options.addExtensions(new File("extensions",
            // "Block-image_v1.1.crx"));

            DesiredCapabilities cap = DesiredCapabilities.chrome();
            cap.setCapability(ChromeOptions.CAPABILITY, options);

            // set performance logger
            // this sends Network.enable to chromedriver
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

            driver = new ChromeDriver(cap);

            // navigate to the page
            System.out.println("Navigate to " + url);
            driver.navigate().to(url);

            //Try to catch post request
            driver.findElement(By.name("q")).sendKeys("find me");
            driver.findElement(By.name("q")).sendKeys(Keys.ENTER);


            // and capture the last recorded url (it may be a redirect, or the
            // original url)
            String currentURL = driver.getCurrentUrl();




            // then ask for all the performance logs from this request
            // one of them will contain the Network.responseReceived method
            // and we shall find the "last recorded url" response
            LogEntries logs = driver.manage().logs().get("performance");

            int status = -1;

            System.out.println("\nList of log entries:\n");

            for (Iterator<LogEntry> it = logs.iterator(); it.hasNext();)
            {
                LogEntry entry = it.next();

                try
                {
                    JSONObject json = new JSONObject(entry.getMessage());

//                    System.out.println(json.toString());

                    JSONObject message = json.getJSONObject("message");
                    String method = message.getString("method");

                    if (method != null
                            && "Network.responseReceived".equals(method))
                    {
                        JSONObject params = message.getJSONObject("params");

                        JSONObject response = params.getJSONObject("response");
                        String messageUrl = response.getString("url");


                            status = response.getInt("status");

                            System.out.println(
                                    "---------- bingo !!!!!!!!!!!!!! returned response for "
                                            + messageUrl + ": " + status);

                            System.out.println(
                                    "---------- bingo !!!!!!!!!!!!!! headers: "
                                            + response.get("headers"));

                    } else if (method != null
                            && "Network.requestWillBeSent".equals(method)) {
                        JSONObject params = message.getJSONObject("params");
                        JSONObject request = params.getJSONObject("request");
                        System.out.println("REQUEST SENT");
                        System.out.println(request);
                    }
                } catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            System.out.println("\nstatus code: " + status);
        } finally
        {
            if (driver != null)
            {
                driver.quit();
            }
        }
    }
}