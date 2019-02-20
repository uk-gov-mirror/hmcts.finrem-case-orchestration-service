package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

@RunWith(SerenityRunner.class)
public class NotificationTests extends IntegrationTestBase {

    @Value("${cos.notification.judge-assign.api}")
    private String notifyAssignToJudge ;

    @Value("${cos.notification.consent-order-available.api}")
    private String consentOrderAvailable ;

    @Value("${cos.notification.consent-order-approved.api}")
    private String consentOrderMade;

    @Value("${cos.notification.consent-order-unapproved.api}")
    private String consentOrderNotApproved;

    @Value("${cos.notification.hwf-success.api}")
    private String hwfSuccessfulApiUri;


    @Test
    public void verifyNotifyAssignToJudgeTestIsOkay() {

        validatePostSuccessForNotification(notifyAssignToJudge, "json/ccd-request-with-solicitor-assignToJudge.json");

    }

    @Test
    public void verifyNotifyConsentOrderAvailableTestIsOkay() {

        validatePostSuccessForNotification(consentOrderAvailable, ".json");

    }

    @Test
    public void verifyNotifyConsentOrderMadeTestIsOkay() {

        validatePostSuccessForNotification(consentOrderMade, "ccd-request-with-solicitor-consentOrderMade.json");

    }

    @Test
    public void verifyNotifyConsentOrderNotApprovedTestIsOkay() {

        validatePostSuccessForNotification(consentOrderNotApproved,
                "ccd-request-with-solicitor-consentOrderNotApproved");

    }

    @Test
    public void verifyNotifyHwfSuccessfulTestIsOkay() {

        validatePostSuccessForNotification(hwfSuccessfulApiUri, "ccd-request-with-solicitor-hwfSuccessfulEmail.json");

    }

    private void validatePostSuccessForNotification(String url, String jsonFileName) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getNewHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then().assertThat().statusCode(204);
    }

}
