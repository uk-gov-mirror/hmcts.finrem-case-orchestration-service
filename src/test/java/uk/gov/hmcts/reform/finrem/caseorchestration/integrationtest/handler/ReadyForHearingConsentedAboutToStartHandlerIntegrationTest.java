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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.ReadyForHearingConsentedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class ReadyForHearingConsentedAboutToStartHandlerIntegrationTest extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadyForHearingConsentedAboutToStartHandler readyForHearingConsentedAboutToStartHandler;

    @Test
    public void whenHearingNotListed_ThenShouldNotBeReadyForHearing() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = readyForHearingConsentedAboutToStartHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.READY_FOR_HEARING)
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .caseType(CaseType.CONSENTED)
                .data(new FinremCaseData()).build())
            .build(),"");
        assertEquals(List.of("There is no hearing on the case."), response.getErrors());
    }
}
