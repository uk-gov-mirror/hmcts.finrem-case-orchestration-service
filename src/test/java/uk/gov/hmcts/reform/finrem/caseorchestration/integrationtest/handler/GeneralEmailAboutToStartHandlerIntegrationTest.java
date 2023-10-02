package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.GeneralEmailAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class GeneralEmailAboutToStartHandlerIntegrationTest extends BaseTest {

    @Autowired
    private IdamService idamService;

    @Autowired
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeneralEmailAboutToStartHandler generalEmailAboutToStartHandler;

    @Test
    public void generalApplicationEventStartButNotAddedDetails_thenThrowErrorMessage() throws Exception {

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = generalEmailAboutToStartHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.CREATE_GENERAL_EMAIL)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L)
                .data(FinremCaseData.builder()
                    .generalEmailWrapper(GeneralEmailWrapper.builder()
                        .generalEmailRecipient("Test")
                        .generalEmailCreatedBy("Test")
                        .generalEmailBody("body")
                        .generalEmailUploadedDocument(CaseDocument.builder().build())
                        .build())
                    .build())
                .build())
            .build(), "");

        assertNull(response.getData().getGeneralEmailWrapper().getGeneralEmailBody());
        assertNull(response.getData().getGeneralEmailWrapper().getGeneralEmailRecipient());
        assertNull(response.getData().getGeneralEmailWrapper().getGeneralEmailUploadedDocument());
        assertEquals("UserName", response.getData().getGeneralEmailWrapper().getGeneralEmailCreatedBy());
    }
}
