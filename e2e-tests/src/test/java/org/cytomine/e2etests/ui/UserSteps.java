package org.cytomine.e2etests.ui;

import java.net.URL;

import org.cytomine.e2etests.utils.Role;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSteps {

    @Autowired
    WebDriverUtils webDriverUtils;

    public void createUser(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String username,
        String firstname,
        String lastname,
        String email,
        String password
    ) {
        createUser(wait, cytomineUrl, username, firstname, lastname, email, password, Role.GUEST);
    }

    public void createUser(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String username,
        String firstname,
        String lastname,
        String email,
        String password,
        Role role
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/admin?tab=users");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New user')]"));
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'New user')]");
        webDriverUtils.byIsDisplayed(wait, By.name("username"));
        webDriverUtils.bySendKeys(wait, By.name("username"), username);
        webDriverUtils.bySendKeys(wait, By.name("firstname"), firstname);
        webDriverUtils.bySendKeys(wait, By.name("lastname"), lastname);
        webDriverUtils.bySendKeys(wait, By.name("email"), email);
        webDriverUtils.bySendKeys(wait, By.name("password"), password);
        webDriverUtils.bySelectByValue(
            wait,
            By.xpath("//div[contains(@class, 'field') and .//label[normalize-space(text())='Role']]//select"),
            role.getValue()
        );
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'User successfully created')]"));
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td[normalize-space(text())='" + username + "']"));
    }

    public void verifyAccountRole(Wait<WebDriver> wait, URL cytomineUrl, Role role) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/account");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//div[contains(@class, 'field') and .//label[normalize-space(text())='Role']]"
                + "//span[contains(@class, 'tag') and normalize-space(text())='"
                + role.getLabel()
                + "']")
        );
    }

    public void editUser(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String username,
        String newFirstname,
        String newLastname
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/admin?tab=users");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New user')]"));
        webDriverUtils.xpathClick(
            wait,
            "//tr[.//td[normalize-space(text())='" + username + "']]//button[contains(text(), 'Edit')]"
        );
        webDriverUtils.byIsDisplayed(wait, By.name("firstname"));
        webDriverUtils.byClear(wait, By.name("firstname"));
        webDriverUtils.bySendKeys(wait, By.name("firstname"), newFirstname);
        webDriverUtils.byClear(wait, By.name("lastname"));
        webDriverUtils.bySendKeys(wait, By.name("lastname"), newLastname);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'User successfully updated')]"));
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//td[contains(normalize-space(text()), '" + newFirstname + " " + newLastname + "')]")
        );
    }
}
