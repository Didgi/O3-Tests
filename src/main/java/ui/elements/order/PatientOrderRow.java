package ui.elements.order;

import com.codeborne.selenide.SelenideElement;
import ui.elements.BaseElement;

public class PatientOrderRow extends BaseElement {
    public PatientOrderRow(SelenideElement row) {
        super(row);
    }

    public String orderNumber() {
        return findAll("td").get(1).getText();
    }

    public String dateOfOrder() {
        return findAll("td").get(2).getText();
    }

    public String orderType() {
        return findAll("td").get(3).getText();
    }

    public String orderDisplayName() {
        return findAll("td").get(4).getText();
    }

    public String priority() {
        return findAll("td").get(5).getText();
    }

    public String orderer() {
        return findAll("td").get(6).getText();
    }
}
