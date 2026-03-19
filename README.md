# 📱 Appium Mobile Automation Framework

A production-ready mobile test automation framework built with **Appium 2.x + Java + TestNG**, following industry best practices.

---

##  Project Structure

```
AppiumMobileFramework/
├── .github/
│   └── workflows/
│       └── mobile-tests.yml          # GitHub Actions CI/CD pipeline
│
├── src/
│   ├── main/java/com/framework/
│   │   ├── base/
│   │   │   ├── AppiumDriverFactory.java  # Creates AndroidDriver with capabilities
│   │   │   ├── BasePage.java             # Parent class for all Page Objects
│   │   │   ├── BaseTest.java             # Parent class for all Test classes
│   │   │   └── DriverManager.java        # ThreadLocal driver (parallel support)
│   │   │
│   │   ├── pages/
│   │   │   ├── LoginPage.java            # Login screen POM
│   │   │   ├── HomePage.java             # Products screen POM
│   │   │   ├── CartPage.java             # Cart screen POM
│   │   │   ├── CheckoutPage.java         # Checkout info screen POM
│   │   │   └── OrderSummaryPage.java     # Order summary/confirmation POM
│   │   │
│   │   └── utils/
│   │       ├── ConfigReader.java         # Reads config.properties
│   │       ├── ExtentReportManager.java  # Extent Reports lifecycle
│   │       ├── GestureUtils.java         # Scroll, swipe, tap, permissions
│   │       ├── JsonDataReader.java       # Reads testdata.json
│   │       ├── RetryAnalyzer.java        # Retries failed tests
│   │       ├── RetryListener.java        # Applies retry to all tests
│   │       ├── ScreenshotUtils.java      # Captures screenshots
│   │       └── WaitUtils.java            # Explicit wait methods
│   │
│   └── test/
│       ├── java/com/framework/tests/
│       │   ├── LoginTest.java            # Login test cases (5 scenarios)
│       │   └── AddToCartTest.java        # Cart & checkout test cases (6 scenarios)
│       └── resources/
│           └── testng.xml               # TestNG suite (parallel execution config)
│
├── resources/
│   ├── config.properties               # Central configuration
│   ├── testdata.json                   # Test data (JSON)
│   └── log4j2.xml                      # Logging configuration
│
├── drivers/
│   └── (place your .apk file here)
│
├── reports/
│   ├── ExtentReport.html               # Generated HTML report
│   ├── screenshots/                    # Failure screenshots
│   └── logs/                           # Log4j log files
│
└── pom.xml                             # Maven dependencies
```

---

##  Prerequisites

| Tool              | Version  | Notes                              |
|-------------------|----------|------------------------------------|
| Java JDK          | 11+      | Set JAVA_HOME                      |
| Maven             | 3.8+     | Set M2_HOME                        |
| Node.js           | 18+      | Required for Appium                |
| Appium            | 2.x      | `npm install -g appium`            |
| UiAutomator2      | latest   | `appium driver install uiautomator2` |
| Android Studio    | latest   | For emulator / ADB                 |
| Android SDK       | API 33+  | Set ANDROID_HOME                   |

---

##  Quick Start

### 1. Install Appium
```bash
npm install -g appium@2.2.1
appium driver install uiautomator2
```

### 2. Place your APK in the `drivers/` folder
```
drivers/saucedemo.apk
```
> Download Sauce Labs Demo App: https://github.com/saucelabs/my-demo-app-android/releases

### 3. Update `resources/config.properties`
```properties
device.name=emulator-5554        # Your device name (adb devices)
platform.version=13.0            # Your Android version
udid=emulator-5554               # Device UDID
app.path=drivers/saucedemo.apk   # Path to APK
```

### 4. Start Appium Server
```bash
appium --port 4723
```

### 5. Start Emulator or connect Real Device
```bash
# List devices
adb devices

# Start emulator (replace with your AVD name)
emulator -avd Pixel_4_API_33
```

### 6. Run Tests
```bash
# Run all tests
mvn clean test

# Run specific test class
mvn clean test -Dtest=LoginTest

# Run with device override
mvn clean test -DdeviceName="Pixel 7" -Dudid=ABCDEF123

# Run specific TestNG suite
mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
```

---

##  Parallel Execution

Edit `src/test/resources/testng.xml` and uncomment the second `<test>` block:

```xml
<suite name="Appium Suite" parallel="tests" thread-count="2">
    <test name="Device 1 - Emulator">
        <parameter name="udid" value="emulator-5554"/>
        ...
    </test>
    <test name="Device 2 - Real Device">
        <parameter name="udid" value="YOUR_REAL_DEVICE_UDID"/>
        ...
    </test>
</suite>
```

---

##  Reports

After test run, open the report:
```
reports/ExtentReport.html
```

Screenshots on failure are saved to:
```
reports/screenshots/
```

Logs are written to:
```
reports/logs/automation.log
```

---

##  Retry Logic

Failed tests are automatically retried **up to 2 times**.
- Configure `MAX_RETRY_COUNT` in `RetryAnalyzer.java`
- `RetryListener.java` applies this to ALL tests automatically via `testng.xml`

---

##  Adding a New Page

1. Create `src/main/java/com/framework/pages/MyNewPage.java`
2. Extend `BasePage`
3. Add `@AndroidFindBy` locators
4. Implement `isLoaded()` method

```java
public class MyNewPage extends BasePage {
    @AndroidFindBy(accessibility = "test-MyElement")
    private WebElement myElement;

    public MyNewPage(AndroidDriver driver) {
        super(driver);  // Required!
    }

    @Override
    public boolean isLoaded() {
        return waitUtils.isElementDisplayed(myElement);
    }
}
```

---

##  Adding a New Test

1. Create `src/test/java/com/framework/tests/MyTest.java`
2. Extend `BaseTest`
3. Use `getDriver()` to access the driver

```java
public class MyTest extends BaseTest {
    @Test(description = "My test description")
    public void testSomething() {
        MyPage page = new MyPage(getDriver());
        Assert.assertTrue(page.isLoaded());
    }
}
```

---

##  CI/CD (GitHub Actions)

The pipeline in `.github/workflows/mobile-tests.yml`:
- Starts a Pixel 4 emulator (API 33)
- Installs and starts Appium 2.x
- Runs the full TestNG suite
- Uploads Extent Report, screenshots, and logs as artifacts

Trigger manually from **Actions → Run Workflow** in GitHub.

---

##  Tech Stack

| Component     | Technology                 |
|---------------|----------------------------|
| Language      | Java 11                    |
| Mobile Driver | Appium 2.x (UiAutomator2)  |
| Test Runner   | TestNG 7.x                 |
| Build Tool    | Maven 3.x                  |
| Design Pattern| Page Object Model (POM)    |
| Reporting     | Extent Reports 5.x         |
| Logging       | Log4j2                     |
| Data          | Jackson (JSON)             |
| CI/CD         | GitHub Actions             |
