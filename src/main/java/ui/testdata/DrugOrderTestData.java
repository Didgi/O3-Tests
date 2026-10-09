package ui.testdata;

import ui.models.DrugOrderData;

public final class DrugOrderTestData {
    private DrugOrderTestData() {}

    public static DrugOrderData validAcetaminophenOrder() {
        return new DrugOrderData(
                "Acetaminophen 325 mg",
                new DrugOrderData.Dosage(
                        "1",
                        "Tablet",
                        "Oral",
                        "Once daily"
                ),
                new DrugOrderData.Dispensing(
                        "10",
                        "Tablet",
                        "0"
                ),
                "Pain"
        );
    }
}
