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
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.IssueApplicationConsentedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class IssueApplicationConsentedAboutToStartHandlerIntegrationTest extends BaseTest {

    @Autowired
    private OnStartDefaultValueService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IssueApplicationConsentedAboutToStartHandler issueApplicationConsentedAboutToStartHandler;

    @Test
    public void generateAppropriateCaseDocumentAndSetDefaultIfValueIsMissing() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = issueApplicationConsentedAboutToStartHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder()
                    .issueDate(LocalDate.now())
                .build())
                .build())
            .build(),"" );
        Assert.assertEquals(LocalDate.now(), response.getData().getIssueDate());
    }

    @Test
    public void generateAppropriateCaseDocumentAndDoNotSetDefaultIfValueIsMissing() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = issueApplicationConsentedAboutToStartHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder()
                    .issueDate(LocalDate.now())
                    .build())
                .build())
            .build(),"" );
        Assert.assertEquals(LocalDate.now(), response.getData().getIssueDate());
    }

}
