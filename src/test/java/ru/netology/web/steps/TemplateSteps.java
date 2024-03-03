package ru.netology.web.steps;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Пусть;
import io.cucumber.java.ru.Тогда;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPage;
import ru.netology.web.page.ReplenishmentPage;
import ru.netology.web.page.VerificationPage;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateSteps {
    private static LoginPage loginPage;
    private static DashboardPage dashboardPage;
    private static VerificationPage verificationPage;
    private static ReplenishmentPage replenishmentPage;
    DataHelper.AuthInfo authInfo = DataHelper.getAuthInfo();
    DataHelper.VerificationCode verificationCode = DataHelper.getVerificationCodeFor(authInfo);
    DataHelper.CardInfo firstCardInfo = DataHelper.getFirstCardInfo();
    DataHelper.CardInfo secondCardInfo = DataHelper.getSecondCardInfo();
    int firstCardBalance;
    int secondCardBalance;
    int amount;

    @Пусть("открыта страница с формой авторизации {string}")
    public void openAuthPage(String url) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        Configuration.browserCapabilities = options;
        loginPage = Selenide.open(url, LoginPage.class);
    }

    @Когда("пользователь пытается авторизоваться")
    public void loginWithNameAndPassword() {
        verificationPage = loginPage.validLogin(authInfo);
    }

    @И("пользователь вводит корректный проверочный код 'из смс'")
    public void setVerifyCode() {
        dashboardPage = verificationPage.validVerify(verificationCode);
    }

    @И("пользовать выбирает пополнить карту c id {string}")
    public void selectCardForReplenishment(String id) {
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo.getId());
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo.getId());
        replenishmentPage = dashboardPage.selectCardToReplenishment(id);
    }

    @И("вводит сумму и номер карты '5559 0000 0000 0001'")
    public void setAmountAndCardNumberOne() {
        amount = DataHelper.generateValidAmount(firstCardBalance);
        dashboardPage = replenishmentPage.makeValidReplenishment(String.valueOf(amount), firstCardInfo);
    }

    @И("вводит сумму больше, чем баланс и номер карты '5559 0000 0000 0002'")
    public void setAmountAndCardNumberTwo() {
        amount = DataHelper.generateInvalidAmount(secondCardBalance);
        replenishmentPage.makeReplenishment(String.valueOf(amount), secondCardInfo);
    }

    @Тогда("происходит успешное пополнение, пользовать попадает на страницу 'Ваши карты' и баланс карт изменился")
    public void verifyReplenishment() {
        int expectedBalanceFirstCard = firstCardBalance - amount;
        int expectedBalanceSecondCard = secondCardBalance + amount;
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo.getId());
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo.getId());
        assertAll(()->assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                ()->assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }

    @Тогда("появляется сообщение об ошибке, баланс карт не меняется")
    public void appearErrorMessage() {
        replenishmentPage.findErrorMessage("Ошибка! На балансе недостаточно средств");
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo.getId());
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo.getId());
        assertAll(()->assertEquals(firstCardBalance, actualBalanceFirstCard),
                ()->assertEquals(secondCardBalance, actualBalanceSecondCard));
    }
}
