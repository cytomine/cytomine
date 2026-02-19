package org.cytomine.e2etests.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.stereotype.Component;

@Component
public class WebDriverUtils {

    void xpathClick(Wait<WebDriver> wait, String xpath) {
        byClick(wait, By.xpath(xpath));
    }

    void byClick(Wait<WebDriver> wait, By by) {
        wait.until(d -> {
            d.findElement(by)
                    .click();
            return true;
        });
    }

    void bySendKeys(Wait<WebDriver> wait, By by, String keys) {
        wait.until(d -> {
            d.findElement(by)
                    .sendKeys(keys);
            return true;
        });
    }

    void goTo(Wait<WebDriver> wait, String url) {
        wait.until(d -> {
            d.get(url);
            return true;
        });
    }

    void byIsDisplayed(Wait<WebDriver> wait, By by) {
        wait.until(d -> d.findElement(by)
                .isDisplayed()
        );
    }

}
