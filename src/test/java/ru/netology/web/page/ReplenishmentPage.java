package ru.netology.web.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import ru.netology.web.data.DataHelper;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class ReplenishmentPage {
    private SelenideElement head = $("h1");
    private SelenideElement amountField = $("[data-test-id='amount'] input");
    private SelenideElement fromField = $("[data-test-id='from'] input");
    private SelenideElement replenishmentButton = $("[data-test-id='action-transfer']");
    private SelenideElement errorMessage = $("[data-test-id='error-notification'] notification__content");

    public ReplenishmentPage() {
        head.shouldHave(exactText("Пополнение карты"));
    }

    public DashboardPage makeValidReplenishment(String amountToReplenishment, DataHelper.CardInfo cardInfo) {
        makeReplenishment(amountToReplenishment, cardInfo);
        return new DashboardPage();
    }

    public void makeReplenishment(String amountToReplenishment, DataHelper.CardInfo cardInfo) {
        amountField.setValue(amountToReplenishment);
        fromField.setValue(cardInfo.getCardNumber());
        replenishmentButton.click();
    }


    public void findErrorMessage(String expectedText) {
        errorMessage.shouldBe(visible).shouldHave(exactText(expectedText));
    }
}
