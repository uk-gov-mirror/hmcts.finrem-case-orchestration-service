package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import lombok.extern.slf4j.Slf4j;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.GeneralEmailAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class GeneralEmailAboutToSubmitHandlerIntegrationTest extends BaseTest {

    @Autowired
    private GeneralEmailService generalEmailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GenericDocumentService genericDocumentService;

    @Autowired
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Autowired
    private GeneralEmailAboutToSubmitHandler generalEmailAboutToSubmitHandler;

    @Test
    public void GeneralEmailAboutToSubmitHandler_WhenHandle_thenSendEmail() throws Exception {

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = generalEmailAboutToSubmitHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.CREATE_GENERAL_EMAIL)
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .caseType(CaseType.CONSENTED).
                data(FinremCaseData.builder()
                    .generalEmailWrapper(GeneralEmailWrapper.builder()
                        .generalEmailRecipient("Test")
                        .generalEmailCreatedBy("Test")
                        .generalEmailBody("body")
                        .generalEmailUploadedDocument(CaseDocument.builder().build())
                        .build())
                    .build())
                .build())
            .build(), "");
        verify(notificationService).sendConsentGeneralEmail(any(FinremCaseDetails.class), anyString());
        verify(generalEmailService).storeGeneralEmail(any(FinremCaseDetails.class));

    }
}
