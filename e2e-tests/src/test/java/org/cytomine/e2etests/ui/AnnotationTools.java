package org.cytomine.e2etests.ui;

import java.util.Random;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnnotationTools {

    public static final int DEFAULT_CANVA_OFFSET = 2;

    @Autowired
    WebDriverUtils webDriverUtils;

    private void selectDrawTool(Wait<WebDriver> wait, String iconClass) {
        webDriverUtils.xpathClick(wait, "//button[.//i[contains(@class, '" + iconClass + "')]]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//button[contains(@class, 'is-selected') and .//i[contains(@class, '" + iconClass + "')]]")
        );
    }

    @SneakyThrows
    public void drawPointAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-map-pin");

        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));
        Actions actions = new Actions(driver);
        actions.moveToElement(mapCanvas, 0, 0)
            .click()
            .perform();

        Thread.sleep(2000);
    }

    @SneakyThrows
    public void drawLineAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-minus");

        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));

        int canvasWidth = mapCanvas.getSize().getWidth();
        int canvasHeight = mapCanvas.getSize().getHeight();
        int xOffset = canvasWidth / 8;
        int yOffset = canvasHeight / 8;

        Actions actions = new Actions(driver);
        actions.moveToElement(mapCanvas, -xOffset, -yOffset)
            .click()
            .moveToElement(mapCanvas, xOffset, yOffset)
            .doubleClick()
            .perform();

        Thread.sleep(2000);
    }

    @SneakyThrows
    public void drawFreeHandLineAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.xpathClick(wait, "//button[.//*[@d and starts-with(@d,'m 28.507424')]]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//button[contains(@class, 'is-selected') and .//svg//*[starts-with(@d,'m 28.507424')]]")
        );

        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));

        int canvasWidth = mapCanvas.getSize().getWidth();
        int canvasHeight = mapCanvas.getSize().getHeight();
        int startX = canvasWidth / 4;
        int startY = canvasHeight / 4;
        int endX = canvasWidth * 3 / 4;
        int endY = canvasHeight * 3 / 4;

        Actions actions = new Actions(driver);
        actions.moveToElement(mapCanvas, startX - canvasWidth / 2, startY - canvasHeight / 2)
            .clickAndHold()
            .moveToElement(mapCanvas, endX - canvasWidth / 2, endY - canvasHeight / 2)
            .release()
            .perform();

        Thread.sleep(2000);
    }

    @SneakyThrows
    public void drawRectangle(Wait<WebDriver> wait, WebDriver driver, int xOffset, int yOffset) {
        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));

        int canvasWidth = mapCanvas.getSize().getWidth();
        int canvasHeight = mapCanvas.getSize().getHeight();
        int startX = canvasWidth / 4;
        int startY = canvasHeight / 4;
        int endX = canvasWidth * 3 / 4;
        int endY = canvasHeight * 3 / 4;

        Actions actions = new Actions(driver);
        actions.moveToElement(mapCanvas, startX - canvasWidth / xOffset, startY - canvasHeight / yOffset)
            .click()
            .moveToElement(mapCanvas, endX - canvasWidth / 2, endY - canvasHeight / 2)
            .click()
            .perform();

        Thread.sleep(2000);
    }

    public void drawRectangle(Wait<WebDriver> wait, WebDriver driver) {
        drawRectangle(wait, driver, DEFAULT_CANVA_OFFSET, DEFAULT_CANVA_OFFSET);
    }

    public void drawRectangleAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-square");
        drawRectangle(wait, driver);
    }

    public void drawRandomRectangleAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-square");
        int xOffset = new Random().nextInt(3) + DEFAULT_CANVA_OFFSET;
        int yOffset = new Random().nextInt(3) + DEFAULT_CANVA_OFFSET;
        drawRectangle(wait, driver, xOffset, yOffset);
    }

    public void drawRectangleAnnotationWithMagicWand(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-magic");
        drawRectangle(wait, driver);
    }

    public void drawCircleAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-circle");
        drawRectangle(wait, driver);
    }

    @SneakyThrows
    public void drawPolygonAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        selectDrawTool(wait, "fa-draw-polygon");

        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));

        int canvasWidth = mapCanvas.getSize().getWidth();
        int canvasHeight = mapCanvas.getSize().getHeight();
        int startX = canvasWidth / 4;
        int startY = canvasHeight / 4;

        int[][] points = {
            {startX, startY},
            {startX + 100, startY - 80},
            {startX + 160, startY + 40},
            {startX + 40, startY + 120},
            {startX - 80, startY + 40},
        };

        Actions actions = new Actions(driver);
        Actions chain = actions.moveToElement(mapCanvas, startX - canvasWidth / 2, startY - canvasHeight / 2)
            .click();
        for (int i = 1; i < points.length; i++) {
            chain = chain.moveToElement(mapCanvas, points[i][0] - canvasWidth / 2, points[i][1] - canvasHeight / 2)
                .click();
        }
        chain.moveToElement(mapCanvas, points[0][0] - canvasWidth / 2, points[0][1] - canvasHeight / 2)
            .click()
            .perform();

        Thread.sleep(2000);
    }

    @SneakyThrows
    public void drawFreeHandPolygonAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.xpathClick(wait, "//button[.//*[@d and starts-with(@d,'m 38.949622')]]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//button[contains(@class, 'is-selected') and .//svg//*[starts-with(@d,'m 38.949622')]]")
        );

        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));

        int canvasWidth = mapCanvas.getSize().getWidth();
        int canvasHeight = mapCanvas.getSize().getHeight();
        int startX = canvasWidth / 4;
        int startY = canvasHeight / 4;

        int[][] points = {
            {startX, startY},
            {startX + 100, startY - 80},
            {startX + 160, startY + 40},
            {startX + 40, startY + 120},
            {startX - 80, startY + 40},
        };

        Actions actions = new Actions(driver);
        actions.moveToElement(mapCanvas, points[0][0] - canvasWidth / 2, points[0][1] - canvasHeight / 2)
            .clickAndHold();
        for (int i = 1; i < points.length; i++) {
            actions.moveToElement(mapCanvas, points[i][0] - canvasWidth / 2, points[i][1] - canvasHeight / 2);
        }
        actions.moveToElement(mapCanvas, points[0][0] - canvasWidth / 2, points[0][1] - canvasHeight / 2)
            .release()
            .perform();

        Thread.sleep(2000);
    }
}
