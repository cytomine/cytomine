package org.cytomine.e2etests.ui;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.stereotype.Component;

@Component
public class WebDriverUtils {

    void xpathClick(Wait<WebDriver> wait, String xpath) {
        byClick(wait, By.xpath(xpath));
    }

    void byClick(Wait<WebDriver> wait, By by) {
        waitLoading(wait);
        wait.until(d -> {
            try {
                ExpectedConditions.elementToBeClickable(by).apply(d).click();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    void bySendKeys(Wait<WebDriver> wait, By by, String keys) {
        bySendKeysWait(wait, by, keys, true);
    }

    void bySendKeysWait(Wait<WebDriver> wait, By by, String keys, boolean waitDisplayed) {
        if (waitDisplayed) {
            byIsDisplayed(wait, by);
        }
        wait.until(d -> {
            try {
                d.findElement(by).sendKeys(keys);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    void goTo(Wait<WebDriver> wait, String url) {
        wait.until(
            d -> {
                try {
                    d.get(url);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
    }

    @SneakyThrows
    boolean byIsDisplayed(Wait<WebDriver> wait, By by) {
        Thread.sleep(500);
        waitLoading(wait);
        wait.until(d -> {
            try {
                return ExpectedConditions.visibilityOfElementLocated(by).apply(d);
            } catch (Exception e) {
                return false;
            }
        });
        return true;
    }

    void waitLoading(Wait<WebDriver> wait) {
        try {
            wait.until(d -> {
                var loadingOverlays = d.findElements(By.cssSelector(".loading-overlay.is-active"));
                return loadingOverlays.isEmpty();
            });
        } catch (TimeoutException ignored) {
        }
    }

    void waitUntilByEmpty(Wait<WebDriver> wait, By by) {
        wait.until(d -> {
            try {
                return d.findElements(by).isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    WebElement waitForCanvasReady(Wait<WebDriver> wait, By canvasLocator) {
        waitLoading(wait);
        return wait.until(d -> {
            try {
                WebElement canvas = d.findElement(canvasLocator);
                if (!canvas.isDisplayed()) {
                    return null;
                }
                int width = canvas.getSize().getWidth();
                int height = canvas.getSize().getHeight();
                if (width <= 0 || height <= 0) {
                    return null;
                }
                return canvas;
            } catch (Exception e) {
                return null;
            }
        });
    }
}
