package org.cytomine.e2etests.ui;

import lombok.SneakyThrows;
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
    byIsDisplayed(wait, by);
    wait.until(
        d -> {
          d.findElement(by).click();
          return true;
        });
  }

  void bySendKeys(Wait<WebDriver> wait, By by, String keys) {
    byIsDisplayed(wait, by);
    wait.until(
        d -> {
          d.findElement(by).sendKeys(keys);
          return true;
        });
  }

  void goTo(Wait<WebDriver> wait, String url) {
    wait.until(
        d -> {
          d.get(url);
          return true;
        });
  }

  @SneakyThrows
  boolean byIsDisplayed(Wait<WebDriver> wait, By by) {
    Thread.sleep(500);
    wait.until(d -> d.findElement(by).isDisplayed());
    return true;
  }

  @SneakyThrows
  void waitForTableToLoad(Wait<WebDriver> wait) {
    wait.until(
        d -> {
          var loadingOverlays = d.findElements(By.cssSelector(".loading-overlay.is-active"));
          return loadingOverlays.isEmpty()
              || loadingOverlays.stream().noneMatch(e -> e.isDisplayed());
        });
  }
}
