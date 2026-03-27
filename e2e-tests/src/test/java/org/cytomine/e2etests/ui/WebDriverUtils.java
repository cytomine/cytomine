package org.cytomine.e2etests.ui;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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

    void byClear(Wait<WebDriver> wait, By by) {
        wait.until(d -> {
            WebElement el = d.findElement(by);
            el.sendKeys(Keys.CONTROL + "a");
            el.sendKeys(Keys.DELETE);
            return true;
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

    void byHitEnter(Wait<WebDriver> wait, By by) {
        wait.until(d -> {
            try {
                d.findElement(by).sendKeys(Keys.ENTER);
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

    void byIsDisplayed(Wait<WebDriver> wait, By by) {
        waitLoading(wait);
        wait.until(d -> ExpectedConditions.visibilityOfElementLocated(by).apply(d));
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
                return d.findElements(by).stream().noneMatch(WebElement::isDisplayed);
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
