package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.AmendApplicationAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class AmendApplicationAboutToSubmitHandlerIntegrationTest extends BaseTest {

    @Autowired
    private ConsentOrderService consentOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmendApplicationAboutToSubmitHandler amendApplicationAboutToSubmitHandler;

    @Test
    public void shouldSetDefaultValues() {

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = amendApplicationAboutToSubmitHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.AMEND_APP_DETAILS)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L)
                .data(FinremCaseData.builder()
                    .consentOrder(TestSetUpUtils.caseDocument())
                    .build())
                .build())
            .build(), "");
        Assert.assertNotNull(response.getData().getLatestConsentOrder().getDocumentUrl());

    }
}

