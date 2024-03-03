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

    @Пусть("пользователь залогинен с именем «vasya» и паролем «qwerty123»,")
    public void openDashboardPage() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        Configuration.browserCapabilities = options;
        loginPage = Selenide.open("http://localhost:9999", LoginPage.class);
        verificationPage = loginPage.validLogin(authInfo);
        dashboardPage = verificationPage.validVerify(verificationCode);

    }

    @Когда("пользователь переводит {string} рублей с карты с номером 5559 0000 0000 0002 на свою 1 карту с главной страницы,")
    public void makeReplenishment(String sum) {
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo.getId());
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo.getId());
        replenishmentPage = dashboardPage.selectCardToReplenishment(firstCardInfo.getId());
        amount = Integer.parseInt(sum);
        dashboardPage = replenishmentPage.makeValidReplenishment(String.valueOf(amount), secondCardInfo);
    }

    @Тогда("баланс его 1 карты из списка на главной странице должен стать {string} рублей")
    public void verifyReplenishment(String total) {
        int expectedBalanceFirstCard = Integer.parseInt(total);
        int expectedBalanceSecondCard = secondCardBalance - amount;
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo.getId());
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo.getId());
        assertAll(()->assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                ()->assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }
}
